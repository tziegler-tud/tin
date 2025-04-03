package tinDL.services.internal.dijkstra.algorithms

import tinDL.model.v1.utils.ProductAutomatonTuple
import tinDL.model.v1.productAutomaton.ProductAutomatonGraph
import tinDL.model.v1.productAutomaton.ProductAutomatonNode

import java.util.*


class DijkstraThreshold(
    val productAutomatonGraph: ProductAutomatonGraph,
    private val threshold: Double
) {

    // π[V] - predecessor of V: <V, predecessorOfV>
    private var predecessor: HashSet<ProductAutomatonTuple> = HashSet()

    // set S of nodes (vertices) whose final shortest-path weights from the source have already been determined.
    private var setOfNodes: HashSet<ProductAutomatonNode> = HashSet()

    // min-priority queue Q (note that by default a priority queue in java is a min queue!)
    private var queue: PriorityQueue<ProductAutomatonNode> = PriorityQueue()

    // answerSet
    var answerMap: HashMap<ProductAutomatonTuple, Double> = HashMap()
    private var dijkstracounter = 0

    /**
     * this is now out of sync with the thesis algorithm. The thesis includes answers whose cost is equal to the threshold.
     * lines that need changes: line 5 and line 7 (if-statements)
     */
    private fun singleSourceDijkstra(sourceNode: ProductAutomatonNode) {


        // line 1
        DijkstraAlgorithmUtils.initialiseSingleSource(productAutomatonGraph, sourceNode, predecessor)
        // line 2
        setOfNodes.clear()
        // line 3
        // alternative: we only add the sourceNode and add further nodes down the road as needed.
        // this yields performance increases AND we have to add/re-add the nodes to the queue anyway to keep their cost updated.
        queue.add(sourceNode)

        // line 4
        while (!queue.isEmpty()) {
            dijkstracounter++
            // line 5
            val p = queue.poll()
            // check if the threshold is already reached and terminate if so.
            if (p.weight >= threshold) {
                continue
            }
            // line 6
            setOfNodes.add(p)
            // line 7
            for (edge in p.edges) {
                // will only continue if the threshold won't be reached.
                if (!(p.weight + edge.cost >= threshold)) {
                    setOfNodes.add(edge.target)
                    // line 8
                    DijkstraAlgorithmUtils.relax(p, edge.target, edge, predecessor)
                    queue.add(edge.target)
                }
            }
        }
    }

    fun processDijkstraOverAllInitialNodes(): HashMap<ProductAutomatonTuple, Double> {

        // for all initial nodes...
        for (initialNode in productAutomatonGraph.initialNodes) {

            // clean up from previous runs...
            predecessor.clear()
            queue.clear()

            // run single-source dijkstra
            singleSourceDijkstra(initialNode)
            // put the new shortest-paths into the answer set
            answerMap.putAll(DijkstraAlgorithmUtils.retrieveResultForOneInitialNode(initialNode, setOfNodes))


        }
        return answerMap
    }
}

