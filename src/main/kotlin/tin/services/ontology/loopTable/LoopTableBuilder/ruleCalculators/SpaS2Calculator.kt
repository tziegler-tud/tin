package tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import kotlin.math.min

class SpaS2Calculator(
    private val ec: OntologyExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.restrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;
    private val manchesterShortFormProvider = ec.manchesterShortFormProvider;
    private val tailsets = ec.tailsets!!;


    /**
     * calculates the updated value for an entry spa[(s,t),(s',t'),M]
     */
    fun calculate(spaLoopTableEntry: SPALoopTableEntry, table: SPALoopTable): Int? {
        val source = spaLoopTableEntry.source;
        val target = spaLoopTableEntry.target;
        val M = spaLoopTableEntry.restriction;
        val s = source.first;
        val t = source.second;
        val se = target.first;
        val te = target.second;

        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null
        if (costCutoff == 0) return 0; //we cannot improve an entry with cost 0

        val MCLassExp = restrictionBuilder.asClassExpression(M);
        val MExp = expressionBuilder.createELHIExpression(MCLassExp);

        val superM = dlReasoner.calculateSuperClasses(MExp, true);

        //get all edges (s,_,s1) € QueryGraph
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(s,se);
        if(candidateQueryEdges.isEmpty()) {
            return costCutoff;
        }

        var candidateQueryTransitions = candidateQueryEdges.map{it.label}

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(t, te);
        if(candidateTransducerEdges.isEmpty()) {
            return costCutoff;
        }

        val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }

        //find the minimal cost of an edge (t,u,A?,w,t1) with:
        //  u in candidateQueryTransitions
        //  A in superM

        val validEdges: MutableList<TransducerEdge> = mutableListOf()
        sortedTransducerEdges.forEach edgeCheck@ { transducerEdge ->
            val inLabel = transducerEdge.label.incoming;
            val outLabel = transducerEdge.label.outgoing;

            // try to match edge label to concept assertion. outgoing label must be a concept assertion
            var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;
            //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.


            val isValidQueryTransition = candidateQueryTransitions.contains(QueryEdgeLabel(inLabel))
                    && (superM.containsEntity(edgeClass));
            if(isValidQueryTransition) {
                validEdges.add(transducerEdge)
            };
        }

        if(validEdges.isEmpty()){
            return costCutoff;
        }
        else {
            return validEdges.minOf { it.label.cost };
        }
    }

    /**
     * optimised version to calculate S2 for all (s,t),(s',t'),M . About 6x faster than the original implementation
     */
    fun calculateAll(table: SPALoopTable) : SPALoopTable {
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

                            var validClassNames = candidateTransducerEdges.map{it.label.outgoing}.distinct()

                            tailsets.forEach tailsets@{ tailset ->
                                val restriction = restrictionBuilder.createConceptNameRestriction(tailset)
                                val MClassExp = restrictionBuilder.asClassExpression(restriction);
                                val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                val entry = SPALoopTableEntry(
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
                                    val classExp = expressionBuilder.createELHIExpression(edgeClass);
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