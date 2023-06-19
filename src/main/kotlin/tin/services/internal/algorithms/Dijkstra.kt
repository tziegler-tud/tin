package tin.services.internal.algorithms

import tin.model.utils.ProductAutomatonTuple
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode
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
        DijkstraUtils.initialiseSingleSource(productAutomatonGraph, sourceNode, predecessor)
        // line 2
        setOfNodes.clear()
        // line 3
        queue.addAll(productAutomatonGraph.nodes)
        // todo: queue.remove(sourceNode)
        //       queue.add(sourceNode)
        //       to start with the source node??
        // System.out.println(queue);

        // line 4
        // we need the second condition for a proper termination in possible infinite runs.
        // the condition is chosen dynamically, i.e. for larger input structures it iterates more often.
        // the infinite loop arises when we use "searchAll()" over an input that allows for loops with weight 0, thus dijkstra endlessly explores the "new" infinite path
        while (!queue.isEmpty()) {
            dijkstracounter++
            //System.out.println(dijkstracounter);

            // line 5
            val p = queue.poll()
            // todo: if p has larger weight: return "threshold is reached"
            // line 6
            setOfNodes.add(p)
            // line 7
            for (edge in p.edges) {
                setOfNodes.add(edge.target)
                // line 8
                DijkstraUtils.relax(p, edge.target, edge, predecessor)
            }
        }
    }

    fun processDijkstraOverAllInitialNodes(): HashMap<ProductAutomatonTuple, Double> {


        // for all initial nodes...
        for ((initialNodeCounter, initialNode) in productAutomatonGraph.initialNodes.withIndex()) {
            println("number of initial nodes: " + productAutomatonGraph.initialNodes.size + " already visited: " + initialNodeCounter)
            //System.out.print("dijkstra for initial node: " );
            //initialNode.print();

            // clean up from previous runs...
            predecessor.clear()
            queue.clear()

            // run single-source dijkstra
            singleSourceDijkstra(initialNode)
            // put the new shortest-paths into the answer set
            answerMap.putAll(DijkstraUtils.retrieveResultForOneInitialNode(initialNode, setOfNodes))

        }
        return answerMap
    }
}

