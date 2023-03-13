package tinput.services.individualFiles

import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import java.io.BufferedReader
import java.io.File
import java.util.HashMap

class QueryReader {

    fun readRegularPathQueryFile(file: String): QueryGraph {
        val queryGraph = QueryGraph()
        val queryNodes = HashMap<String, QueryNode>() // map containing the QueryNodes

        var source: QueryNode
        var target: QueryNode
        var node: QueryNode
        var edgeLabel: String
        var stringArray: Array<String>

        var readingNodes = false
        var readingEdges = false
        var currentLine: String


        val bufferedReader: BufferedReader = File(file).bufferedReader()

        while (true) {
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            // when we see "nodes", we will read nodes starting from the next line
            if (currentLine == "nodes") {
                readingNodes = true
                readingEdges = false
                // after setting the flags, we skip into the next line
                currentLine = bufferedReader.readLine()
            }

            if (currentLine == "edges") {
                readingNodes = false
                readingEdges = true

                // after setting the flags, we skip into the next line
                currentLine = bufferedReader.readLine()
            }


            // remove whitespace in current line
            currentLine = currentLine.replace("\\s".toRegex(), "")

            println(currentLine)

            if (readingNodes) {
                // add node from this line

                stringArray = currentLine.split(",").toTypedArray()

                node = QueryNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())
                queryNodes[stringArray[0]] = node
                queryGraph.addQueryNodes(node)

            }

            if (readingEdges) {
                // add edge from this line

                stringArray = currentLine.split(",").toTypedArray()

                // nodes have to be present, because they have been defined before reading any edges in the file
                source = queryNodes[stringArray[0]]!!
                target = queryNodes[stringArray[1]]!!

                edgeLabel = stringArray[2]

                queryGraph.addQueryEdge(source, target, edgeLabel)

            }
        }

        return queryGraph
    }
}