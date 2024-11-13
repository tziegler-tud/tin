package tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.query.QueryGraph
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.loopTable.ELSPALoopTable
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.ELSPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SpaS2Calculator(
    private val ec: ELExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.spaRestrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;

    /**
     * optimised version to calculate S2 for all (s,t),(s',t'),M . About 6x faster than the original implementation
     */
    fun calculateAll(table: ELSPALoopTable) : ELSPALoopTable {
            queryGraph.nodes.forEach { querySource ->
                transducerGraph.nodes.forEach { transducerSource ->
                    queryGraph.nodes.forEach { queryTarget ->
                        transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->

                            if(querySource == queryTarget && transducerSource == transducerTarget) {
                                // (s,t),(s,t),M = 0 always
                                //we don't want to add these to the loop table
                                return@transducerTarget
                            }

                            //get all edges (s,_,s1) € QueryGraph
                            var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
                            if(candidateQueryEdges.isEmpty()) {
                                return@transducerTarget
                            }

                            var candidateQueryTransitions = candidateQueryEdges.map{it.label}

                            //get all edges (t,_,_,_,t1) € TransducerGraph
                            var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);
                            // keep only those that have matching u for some A? s.t. (s,u,s') € query and (t,u,A?,w.t') € trans
                            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) &&
                                        queryParser.getOWLClass(transEdge.label.outgoing) !== null
                            }
                            if(candidateTransducerEdges.isEmpty()) {
                                return@transducerTarget
                            }

                            val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }
                            //extract target class names from edges

                            ec.forEachConcept tailsets@{ restriction ->
                                val MClassExp = restrictionBuilder.asClassExpression(restriction);
                                val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                val entry = ELSPALoopTableEntry(
                                    Pair(querySource, transducerSource),
                                    Pair(queryTarget, transducerTarget),
                                    restriction
                                )

                                val costCutoff = table.get(entry) //0, int val or null
                                if (costCutoff == 0) return@tailsets //we cannot improve an entry with cost 0


                                // for each possible class name, check subsumption M <= A
                                val validEdges: MutableList<TransducerEdge> = mutableListOf()

                                sortedTransducerEdges.forEach edgeCheck@ { transducerEdge ->
                                    val inLabel = transducerEdge.label.incoming;
                                    val outLabel = transducerEdge.label.outgoing;
                                    // try to match edge label to concept assertion. outgoing label must be a concept assertion
                                    //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.
                                    var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;

                                    //create subsumption expression
                                    val classExp = expressionBuilder.createELHExpression(edgeClass);
                                    //check if entailed
                                    val isEntailed = dlReasoner.checkIsSubsumed(MExp, classExp);

                                    if(isEntailed) {
                                        validEdges.add(transducerEdge)
                                    };
                                }

                                if(validEdges.isEmpty()){
                                    return@tailsets
                                }
                                else {
                                    table.set(entry, validEdges.minOf { it.label.cost });
                                }
                            }
                        }
                    }
                }
            }
        return table;
    }
}