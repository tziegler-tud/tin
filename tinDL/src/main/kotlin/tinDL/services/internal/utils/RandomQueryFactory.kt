package tinDL.services.internal.utils

import tinLIB.model.v2.graph.Edge
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerEdge
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import kotlin.random.Random


class RandomQueryFactory {
    companion object {
        fun generateRandomQuery(states: Int, edgeCount: Int, maxLabelSize: Int ) : QueryGraph {
            val graph = QueryGraph();
            //add nodes
            for (i in 0 until states) {
                var initial = true;
                var final = true
                if(i==0) initial = true;
                if(i==states-1) final = true;
                graph.addNode(Node("s$i", initial, final))
            }

            graph.nodes.forEach{ node ->
                for (i in 0 until edgeCount) {
                    val targetIndex = Random.nextInt(0, graph.nodes.size)
                    val target = graph.nodes.elementAt(targetIndex);
                    val labelLength = Random.nextInt(1, maxLabelSize+1)
                    var label = getRandomString(labelLength)
                    val isConceptAssertion = Random.nextInt(0,3)
                    if(isConceptAssertion > 1) {
                        label += "?";
                    }
                    else{
                        val isInverse = Random.nextBoolean()
//                        val isInverse = false
                        if(isInverse) label = "inverse($label)"
                    }
                    graph.addEdge(QueryEdge(node, target, label))
                }
            }

            return graph;
        }

        fun generateQuery(states: Int, edgeCount: Int, ec: ExecutionContext) : QueryGraph {

            val classNames = ec.getClassNames();
            val roleNames = ec.getRoleNames();
            val graph = QueryGraph();
            //add nodes
            for (i in 0 until states) {
                var initial = true;
                var final = true
                if(i==0) initial = true;
                if(i==states-1) final = true;
                graph.addNode(Node("s$i", initial, final))
            }

            graph.nodes.forEach{ node ->
                for (i in 0 until edgeCount) {
                    val targetIndex = Random.nextInt(0, graph.nodes.size)
                    val target = graph.nodes.elementAt(targetIndex);
                    var label = ""

                    val isConceptAssertion = Random.nextInt(0,3)
                    if(isConceptAssertion > 1) {
                        val labelIndex = Random.nextInt(1, classNames.size)
                        label = classNames.elementAt(labelIndex);
                        label += "?";
                    }
                    else{
                        val labelIndex = Random.nextInt(1, roleNames.size)
                        label = roleNames.elementAt(labelIndex);
                        val isInverse = Random.nextBoolean()
//                        val isInverse = false
                        if(isInverse) label = "inverse($label)"
                    }
                    graph.addEdge(QueryEdge(node, target, label))
                }
            }

            return graph;
        }

        fun getRandomString(length: Int) : String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }
}