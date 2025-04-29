package tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators

import tinLIB.model.v2.genericGraph.*
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry

class SpaS3Calculator(
    private val ec: ELExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {


    //use Floyd Warshall to calculate all possible updates in one step
    fun calculateAll(restriction: SingleClassLoopTableEntryRestriction, table: ELSPALoopTable): Map<ELSPALoopTableEntry, Int> {

        //prefilter table
        val tableFragment = table.getWithRestriction(restriction)
        //build graph structure
        val graph = GenericGraph();

        val pairNodes: MutableList<PairNode> = mutableListOf();
        val updateMap: MutableMap<ELSPALoopTableEntry, Int> = mutableMapOf();

        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                val node = PairNode(queryNode, transducerNode);
                pairNodes.add(node); //add to stash for later
                graph.addNode(node)
                graph.addEdge(GenericEdge(node, node, GenericEdgeLabel(0)))
                // add edges < inf
            }
        }

        tableFragment.map.forEach { (spaEntry, value) ->
            val source = graph.getNode(spaEntry.source.first.toString() + spaEntry.source.second.toString());
            val target = graph.getNode(spaEntry.target.first.toString() + spaEntry.target.second.toString());

            if(source === null || target === null) return@forEach;
            graph.addEdge(GenericEdge(source, target, GenericEdgeLabel(value)))
        }

        val distance = floydWarshall(graph);

        distance.forEach { (nodePair, distance) ->
            // nodePair contains Pair<PairNode, PairNode>
            val sourceNode = nodePair.first;
            val targetNode = nodePair.second;
            // refine type to pairNode using stash
            val sourcePairNode = pairNodes.find { it.identifier == sourceNode.identifier }!!
            val targetPairNode = pairNodes.find { it.identifier == targetNode.identifier }!!

            //build SPALoopTableEntry
            val entry = ELSPALoopTableEntry(sourcePairNode.getQueryNode(), sourcePairNode.getTransducerNode(), targetPairNode.getQueryNode(), targetPairNode.getTransducerNode(), restriction);

            updateMap[entry] = distance;
        }
        return updateMap;
    }

    //use Floyd Warshall to calculate all possible updates in one step
    fun calculateAllV2(table: ELSPALoopTable): Map<ELSPALoopTableEntry, Int> {

        //build graph structure
        val graph = GenericGraph();

        val updateMap: MutableMap<ELSPALoopTableEntry, Int> = mutableMapOf();

        //build a graph for each M
        val graphMap: HashMap<SingleClassLoopTableEntryRestriction, GenericGraph> = hashMapOf()

        val pairNodes: MutableList<PairNode> = mutableListOf();


        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                val node = PairNode(queryNode, transducerNode);
                pairNodes.add(node); //add to stash for later
            }
        }

        table.map.forEach { (spaEntry, value) ->

            var graph: GenericGraph;
            if(graphMap.contains(spaEntry.restriction)){
                graph = graphMap[spaEntry.restriction]!!;
            }
            else {
                graph = GenericGraph();
                pairNodes.forEach { pairNode ->
                    graph.addNode(pairNode)
                    graph.addEdge(GenericEdge(pairNode, pairNode, GenericEdgeLabel(0)))
                    // add edges < inf
                }
                graphMap[spaEntry.restriction] = graph;
            }

            val source = graph.getNode(spaEntry.source.first.toString() + spaEntry.source.second.toString());
            val target = graph.getNode(spaEntry.target.first.toString() + spaEntry.target.second.toString());

            if(source === null || target === null) return@forEach;
            graph.addEdge(GenericEdge(source, target, GenericEdgeLabel(value)))
        }

        graphMap.forEach { (restriction, graph) ->
            val distance = floydWarshall(graph);

            distance.forEach { (nodePair, distance) ->
                // nodePair contains Pair<PairNode, PairNode>
                val sourceNode = nodePair.first;
                val targetNode = nodePair.second;
                // refine type to pairNode using stash
                val sourcePairNode = pairNodes.find { it.identifier == sourceNode.identifier }!!
                val targetPairNode = pairNodes.find { it.identifier == targetNode.identifier }!!

                //build SPALoopTableEntry
                val entry = ELSPALoopTableEntry(sourcePairNode.getQueryNode(), sourcePairNode.getTransducerNode(), targetPairNode.getQueryNode(), targetPairNode.getTransducerNode(), restriction);

                updateMap[entry] = distance;
            }
        }
        return updateMap;
    }

    private fun floydWarshall(graph: GenericGraph): HashMap<Pair<Node,Node>, Int> {
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
                            if (distance[Pair(i,j)]!! > dist) distance[Pair(i,j)] = dist;
                        }
                        else distance[Pair(i,j)] = dist;
                    }
                }
            }
        }

        return distance;
    }
}