package tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import tin.model.v2.genericGraph.GenericEdge
import tin.model.v2.genericGraph.GenericEdgeLabel
import tin.model.v2.genericGraph.GenericEdgeSet
import tin.model.v2.genericGraph.GenericGraph
import tin.model.v2.graph.Graph
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import kotlin.math.exp
import kotlin.math.min

class SpaS3Calculator(
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

        val candidateMap = table.getWithRestriction(M, costCutoff)

        //throw away all entries that have neither source nor target matching
        val matchingCandidates = candidateMap.filterKeys { it.source == source || it.target == target }

        // for each (s'',t''), we have to:
        // - get table entries [(s,t),(s'',t''),M] and [(s'',t''),(se,te),M]

        var minValue = costCutoff;

        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                val interPair = Pair(queryNode, transducerNode);
                val entry1 = SPALoopTableEntry(source, interPair, M);
                var value1 : Int? = if (entry1.hasEqualSourceAndTarget()) 0 else matchingCandidates[entry1];

                val entry2 = SPALoopTableEntry(interPair, target, M);
                var value2 : Int? = if (entry2.hasEqualSourceAndTarget()) 0 else matchingCandidates[entry2];

                if(value1 != null && value2 != null){
                    val exprVal = value1 + value2;
                    if (minValue == null) minValue = exprVal;
                    else if(exprVal < minValue!!) minValue = exprVal;
                }
            }
        }

        return minValue;
    }

    //use Floyd Warshall to calculate all possible updates in one step
    fun calculateAll(M: ConceptNameRestriction, table: SPALoopTable): Map<SPALoopTableEntry, Int> {

        //prefilter table
        val candidateMap = table.getWithRestriction(M)
        //build graph structure
        val graph = GenericGraph();

        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                val node = Node(queryNode.toString() + transducerNode.toString());
                graph.addNode(node)
//                graph.addEdge(GenericEdge(node, node, GenericEdgeLabel(0)))  //do we even need them? They can never impact result
                // add edges < inf

            }
        }




    }

    private fun FloydWarshall(graph: GenericGraph) {
        var distance: HashMap<Pair<Node, Node>, Int> = HashMap();

        graph.edges.forEach {
            distance[Pair(it.source, it.target)] = it.label.label;
        }

        graph.nodes.forEach {
            distance[Pair(it, it)] = 0;
        }
        for (k in graph.nodes) {
            for (i in graph.nodes) {
                for (j in graph.nodes) {
                    if(distance[Pair(i,k)] != null && distance[Pair(k,j)] != null) {
                        val dist = distance[Pair(i,k)]!! + distance[Pair(k,j)]!!;
                        if(distance.contains(Pair(i,j))) {
                            if (distance[Pair(i,j)]!! > dist) {
                                distance[Pair(i,j)] = dist;
                            }
                        }
                    }
                }
            }
        }
    }
}