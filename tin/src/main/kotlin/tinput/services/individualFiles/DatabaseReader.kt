package tinput.services.individualFiles

import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import java.io.BufferedReader
import java.io.File
import java.util.HashMap

class DatabaseReader {

    fun readDatabaseFile(file: String): DatabaseGraph {
        val databaseGraph = DatabaseGraph()
        val databaseNodes = HashMap<String, DatabaseNode>() // map containing the QueryNodes
        val alphabet = HashSet<String>()

        var source: DatabaseNode
        var target: DatabaseNode
        var node: DatabaseNode
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

                node = DatabaseNode(stringArray[0])
                databaseNodes[stringArray[0]] = node
                databaseGraph.addNodes(node)

            }

            if (readingEdges) {
                // add edge from this line

                stringArray = currentLine.split(",").toTypedArray()

                // nodes have to be present, because they have been defined before reading any edges in the file
                source = databaseNodes[stringArray[0]]!!
                target = databaseNodes[stringArray[1]]!!

                edgeLabel = stringArray[2]
                alphabet.add(edgeLabel)

                databaseGraph.addEdge(source, target, edgeLabel)

            }
        }

        databaseGraph.alphabet = alphabet
        return databaseGraph
    }
}