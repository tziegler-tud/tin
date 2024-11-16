package tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.query.QueryGraph
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.Reasoner.CachingDLReasoner
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.loopTable.ELHISPALoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

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
     * optimised version to calculate S2 for all (s,t),(s',t'),M . About 6x faster than the original implementation
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

                        val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }

                        ec.forEachTailsetDescending tailsets@{ tailset ->

                            val entry = ELHISPALoopTableEntry(
                                Pair(querySource, transducerSource),
                                Pair(queryTarget, transducerTarget),
                                tailset
                            )

                            sortedTransducerEdges.forEach edgeCheck@{ transducerEdge ->
                                val inLabel = transducerEdge.label.incoming;
                                val outLabel = transducerEdge.label.outgoing;
                                val cost = transducerEdge.label.cost
                                // try to match edge label to concept assertion. outgoing label must be a concept assertion
                                //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.
                                var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;

                                //handle trivial cases A^B... c A ?
                                if (tailset.containsElement(edgeClass)) {
                                    table.set(entry, cost );
                                    return@tailsets
                                }

                                val MClassExp = restrictionBuilder.asClassExpression(tailset);
                                val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                //create subsumption expression
                                val classExp = expressionBuilder.createELHIExpression(edgeClass);
                                //check if entailed
                                val isEntailed = dlReasoner.checkIsSubsumed(MExp, classExp);
                                if (isEntailed) {
                                    table.set(entry, cost );
                                    return@tailsets
                                };
                            }
                        }
                    }
                }
            }
        }
        println("S2 Calculator finished!")

        return table;
    }

//    /**
//     * optimised version to calculate S2 for all (s,t),(s',t'),M . About 6x faster than the original implementation
//     */
//    fun calculateAllOld(table: ELHISPALoopTable) : ELHISPALoopTable {
//        queryGraph.nodes.forEach { querySource ->
//            transducerGraph.nodes.forEach { transducerSource ->
//                queryGraph.nodes.forEach { queryTarget ->
//                    transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->
//
//                        if(querySource == queryTarget && transducerSource == transducerTarget) {
//                            // (s,t),(s,t),M = 0 always
//                            //we don't want to add these to the loop table
//                            return@transducerTarget
//                        }
//
//                        //get all edges (s,_,s1) € QueryGraph
//                        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
//                        if(candidateQueryEdges.isEmpty()) {
//                            return@transducerTarget
//                        }
//
//                        var candidateQueryTransitions = candidateQueryEdges.map{it.label}
//
//                        //get all edges (t,_,_,_,t1) € TransducerGraph
//                        var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);
//                        // keep only those that have matching u for some A? s.t. (s,u,s') € query and (t,u,A?,w.t') € trans
//                        candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
//                            candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) &&
//                                    queryParser.getOWLClass(transEdge.label.outgoing) !== null
//                        }
//                        if(candidateTransducerEdges.isEmpty()) {
//                            return@transducerTarget
//                        }
//
//                        val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }
//                        //extract target class names from edges
//
//                        var validClassNames = candidateTransducerEdges.map{it.label.outgoing}.distinct()
//
//                        ec.forEachTailset tailsets@{ tailset ->
//                            val restriction = tailset
//                            val MClassExp = restrictionBuilder.asClassExpression(restriction);
//                            val MExp = expressionBuilder.createELHIExpression(MClassExp);
//
//                            val entry = ELHISPALoopTableEntry(
//                                Pair(querySource, transducerSource),
//                                Pair(queryTarget, transducerTarget),
//                                restriction
//                            )
//
//                            val costCutoff = table.get(entry) //0, int val or null
//                            if (costCutoff == 0) return@tailsets //we cannot improve an entry with cost 0
//
//
//                            // for each possible class name, check subsumption M <= A
//                            val validEdges: MutableList<TransducerEdge> = mutableListOf()
//
//                            sortedTransducerEdges.forEach edgeCheck@ { transducerEdge ->
//                                val inLabel = transducerEdge.label.incoming;
//                                val outLabel = transducerEdge.label.outgoing;
//                                // try to match edge label to concept assertion. outgoing label must be a concept assertion
//                                //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.
//                                var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;
//
//                                //create subsumption expression
//                                val classExp = expressionBuilder.createELHIExpression(edgeClass);
//                                //check if entailed
//                                val isEntailed = dlReasoner.checkIsSubsumed(MExp, classExp);
//
//                                if(isEntailed) {
//                                    validEdges.add(transducerEdge)
//                                };
//                            }
//
//                            if(validEdges.isEmpty()){
//                                return@tailsets
//                            }
//                            else {
//                                table.set(entry, validEdges.minOf { it.label.cost });
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return table;
//    }

//    /**
//     * optimised version to calculate S2 for all (s,t),(s',t'),M . About 6x faster than the original implementation
//     */
//    fun calculateAllV3(table: ELHISPALoopTable) : ELHISPALoopTable {
//        queryGraph.nodes.forEach { querySource ->
//            transducerGraph.nodes.forEach { transducerSource ->
//                queryGraph.nodes.forEach { queryTarget ->
//                    transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->
//
//
//
//                        if(querySource == queryTarget && transducerSource == transducerTarget) {
//                            // (s,t),(s,t),M = 0 always
//                            //we don't want to add these to the loop table
//                            return@transducerTarget
//                        }
//
//                        //get all edges (s,_,s1) € QueryGraph
//                        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
//                        if(candidateQueryEdges.isEmpty()) {
//                            return@transducerTarget
//                        }
//
//                        var candidateQueryTransitions = candidateQueryEdges.map{it.label}
//
//                        //get all edges (t,_,_,_,t1) € TransducerGraph
//                        var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);
//                        // keep only those that have matching u for some A? s.t. (s,u,s') € query and (t,u,A?,w.t') € trans
//                        candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
//                            candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) &&
//                                    transEdge.label.outgoing.isConceptAssertion()
//                        }
//                        if(candidateTransducerEdges.isEmpty()) {
//                            return@transducerTarget
//                        }
//
//                        val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }
//
//                        var csCounter = 0;
//
//                        sortedTransducerEdges.forEach edgeCheck@{ transducerEdge ->
//                            val inLabel = transducerEdge.label.incoming;
//                            val outLabel = transducerEdge.label.outgoing;
//                            val cost = transducerEdge.label.cost
//                            // try to match edge label to concept assertion. outgoing label must be a concept assertion
//                            //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.
//                            var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;
//
//
//                            // M c A ?
//                            //fixed A, check all M
//
//                            val eliminatedSets : MutableSet<MultiClassLoopTableEntryRestriction> = mutableSetOf()
//
//                            ec.forEachTailsetDescending tailsets@{ tailset ->
//
//                                /**
//                                 * debug line
//                                 */
//                                csCounter++;
//                                println("S2 Calculator: Calculating tailset " + csCounter + "/ " + ec.tailsetSize);
//
//
//
//                                val entry = ELHISPALoopTableEntry(
//                                    Pair(querySource, transducerSource),
//                                    Pair(queryTarget, transducerTarget),
//                                    tailset
//                                )
//
//                                val costCutoff = table.get(entry) //0, int val or null
//                                if ((costCutoff != null) && (costCutoff <= cost)) return@tailsets //we cannot improve an entry with cost 0
//
//
//                                //handle trivial cases A^B... c A ?
//                                if (tailset.containsElement(edgeClass)) {
//                                    table.set(entry, cost );
//                                    return@tailsets
//                                }
//
////                                if(tailset.containsOnlyElementsFromOneOf(eliminatedSets)) {
////                                    //this is a subset of a set already eliminated, discard
////                                    return@tailsets;
////                                }
//
//
//                                val MClassExp = restrictionBuilder.asClassExpression(tailset);
//                                val MExp = expressionBuilder.createELHIExpression(MClassExp);
//
//                                //create subsumption expression
//                                val classExp = expressionBuilder.createELHIExpression(edgeClass);
//                                //check if entailed
//                                val isEntailed = dlReasoner.checkIsSubsumed(MExp, classExp);
//                                if (isEntailed) {
//                                    table.set(entry, cost );
//                                };
//                                else {
////                                    eliminatedSets.add(tailset);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return table;
//    }

    /**
     * calculates the updated value for an entry spa[(s,t),(s',t'),M]
     */
    fun calculate(spaLoopTableEntry: ELHISPALoopTableEntry, table: ELHISPALoopTable): Int? {
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


}