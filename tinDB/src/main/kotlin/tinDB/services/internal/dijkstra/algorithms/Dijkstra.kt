package tinDB.services.internal.dijkstra.algorithms

import tinDB.model.v1.utils.ProductAutomatonTuple
import tinDB.model.v1.productAutomaton.ProductAutomatonGraph
import tinDB.model.v1.productAutomaton.ProductAutomatonNode
import java.util.*
import kotlin.collections.HashSet

// note: we do not use the distance here since we store the weight in every ProductAutomatonSpecification.ProductAutomatonNode.
class Dijkstra(val productAutomatonGraph: ProductAutomatonGraph) {

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
     * Dijkstra algorithm
     * explanations taken from "Introduction to algorithms" (Cormen, Leiserson, Rivest, Stein)
     * page 595
     * comments refer to the pseudocode presented there
     *
     * @param sourceNode the source node
     */
    private fun singleSourceDijkstra(sourceNode: ProductAutomatonNode) {
        //System.out.println("algo dijkstra");


        // line 1
        DijkstraAlgorithmUtils.initialiseSingleSource(productAutomatonGraph, sourceNode, predecessor)
        // line 2
        setOfNodes.clear()
        // line 3
        // alternative: we only add the sourceNode and add further nodes down the road as needed.
        // this yields performance increases AND we have to add/re-add the nodes to the queue anyway to keep their cost updated.
        queue.add(sourceNode)

        // line 4
        // we need the second condition for a proper termination in possible infinite runs.
        // the condition is chosen dynamically, i.e. for larger input structures it iterates more often.
        // the infinite loop arises when we use "searchAll()" over an input that allows for loops with weight 0, thus dijkstra endlessly explores the "new" infinite path
        while (!queue.isEmpty()) {
            dijkstracounter++
            //System.out.println(dijkstracounter);

            // line 5
            val p = queue.poll()
            // line 6
            setOfNodes.add(p)
            // line 7
            for (edge in p.edges) {
                //setOfNodes.add(edge.target) // why this?
                // line 8
                DijkstraAlgorithmUtils.relax(p, edge.target, edge, predecessor)
                // now add the newly found node with its correct weight to the queue
                queue.add(edge.target)
            }
        }
    }

    fun processDijkstraOverAllInitialNodes(): HashMap<ProductAutomatonTuple, Double> {
        // for all initial nodes...
        for ((initialNodeCounter, initialNode) in productAutomatonGraph.initialNodes.withIndex()) {
            //println("number of initial nodes: " + productAutomatonGraph.initialNodes.size + " already visited: " + initialNodeCounter)
            //System.out.print("dijkstra for initial node: " );
            //initialNode.print();

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

