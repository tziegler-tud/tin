package tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.Reasoner.SimpleDLReasoner
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

class SpaS2Calculator(
    private val ec: ELHIExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.spaRestrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;
    private val manager = ec.getManager()
    private val simpleReasoner = SimpleDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    private val manchesterShortFormProvider = ec.manchesterShortFormProvider;



    /**
     * optimised version to calculate S2 for all (s,t),(s',t'),M . Approx. 6x faster than the original implementation
     */
    fun calculateAll(table: ELHISPALoopTable) : ELHISPALoopTable {
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
                                    transEdge.label.outgoing.isConceptAssertion()
                        }
                        if(candidateTransducerEdges.isEmpty()) {
                            return@transducerTarget
                        }


                        candidateTransducerEdges.forEach edgeCheck@{ transducerEdge ->

                            val inLabel = transducerEdge.label.incoming;
                            val outLabel = transducerEdge.label.outgoing;
                            val cost = transducerEdge.label.cost
                            // try to match edge label to concept assertion. outgoing label must be a concept assertion
                            //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.
                            var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;

                            val edgeClassRestriction = ec.spaRestrictionBuilder.createConceptNameRestriction(edgeClass)

                            val eliminatedSets: MutableSet<MultiClassLoopTableEntryRestriction> = mutableSetOf();
                            val positiveSets: MutableSet<MultiClassLoopTableEntryRestriction> = mutableSetOf();

                            val basicSubsumers = dlReasoner.calculateSubClasses(expressionBuilder.createELHIExpression(edgeClass))
                            val basicSubsumerRestrictions = basicSubsumers.map { ec.spaRestrictionBuilder.createConceptNameRestriction(it) }

                            positiveSets.addAll(basicSubsumerRestrictions)

                            /**
                             * debug line
                             */
                            var tcCounter = 0UL;
                            var lastPercentVal = 0UL;
                            val total = ec.tailsetSize

                            ec.forEachTailsetDescending tailsets@{ tailset ->

                                /**
                                 * debug line
                                 */
                                tcCounter++;
                                val percent = (tcCounter /  ec.tailsetSize) * 100UL
//                                println("Calculating tailsets: $tcCounter / ${ec.tailsetSize}");

                                if(percent != lastPercentVal) {
                                    println("Calculating tailsets: $percent%");
                                    lastPercentVal = percent;
                                };

                                val entry = ELHISPALoopTableEntry(
                                    Pair(querySource, transducerSource),
                                    Pair(queryTarget, transducerTarget),
                                    tailset
                                )

                                //handle trivial cases A^B... c A ?
//                                if (tailset.containsElement(edgeClass)) {
                                if (tailset.isSupersetOf(edgeClassRestriction)) {
                                    table.setIfLower(entry, cost );
                                    return@tailsets
                                }

//                                //handle trivial cases B^C... c A ?  for some B c A
//                                if (tailset.containsElementFromSet(basicSubsumers)) {
//                                    table.setIfLower(entry, cost );
//                                    return@tailsets
//                                }

                                if(tailset.containsOnlyElementsFromOneOf(eliminatedSets)) {
                                    return@tailsets;
                                }

                                if(tailset.containsAllElementsFromOneOf(positiveSets)) {
                                    table.setIfLower(entry, cost );
                                    return@tailsets
                                }

                                val MClassExp = restrictionBuilder.asClassExpression(tailset);
                                val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                //create subsumption expression
                                val classExp = expressionBuilder.createELHIExpression(edgeClass);
                                //check if entailed
                                val isEntailed = simpleReasoner.checkIsSubsumed(MExp, classExp);
                                if (isEntailed) {
                                    table.setIfLower(entry, cost );
                                    //positiveSets.add(tailset)
                                    return@tailsets
                                }
                                else {
                                    eliminatedSets.add(tailset)
                                }
                            }
                        }
                    }
                }
            }
        }
        println("S2 Calculator finished!")

        return table;
    }
}