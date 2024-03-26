package tin.services.internal.dijkstra.algorithms

import tin.model.utils.ProductAutomatonTuple
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode

import java.util.*


class DijkstraTopK(val productAutomatonGraph: ProductAutomatonGraph, private val k: Int) {

    // π[V] - predecessor of V: <V, predecessorOfV>
    private var predecessor: HashSet<ProductAutomatonTuple> = HashSet()

    // set S of nodes (vertices) whose final shortest-path weights from the source have already been determined.
    private var setOfNodes: HashSet<ProductAutomatonNode> = HashSet()

    // min-priority queue Q (note that by default a priority queue in java is a min queue!)
    private var queue: PriorityQueue<ProductAutomatonNode> = PriorityQueue()

    // answerSet
    var answerMap: HashMap<ProductAutomatonTuple, Double> = HashMap()

    private var dijkstracounter = 0



    private fun singleSourceDijkstra(sourceNode: ProductAutomatonNode) {
        var finalNodesFound = 0

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
            // line 6
            setOfNodes.add(p)
            // line 7
            for (edge in p.edges) {
                setOfNodes.add(edge.target)

                // first check if we've found an answer (edge.target.finalState == true), then
                // check if we've found k answers here. if so -> terminate
                if (edge.target.isFinalState) {
                    finalNodesFound++
                    if (finalNodesFound == k) {
                        return
                    }
                }

                // line 8
                DijkstraAlgorithmUtils.relax(p, edge.target, edge, predecessor)
                queue.add(edge.target)
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

