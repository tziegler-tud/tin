package tinput.services.individualFiles


import tin.model.transducer.TransducerGraph
import tin.model.transducer.TransducerNode
import java.io.BufferedReader
import java.io.File

class TransducerReader {

    fun readTransducerFile(file: String): TransducerGraph {
        val transducerGraph = TransducerGraph()
        val transducerNodes = HashMap<String, TransducerNode>() // map containing the TransducerNodes

        var source: TransducerNode
        var target: TransducerNode
        var node: TransducerNode
        var incoming: String
        var outgoing: String
        var cost: Double
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

                node = TransducerNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())
                transducerNodes[stringArray[0]] = node
                transducerGraph.addNodes(node)

            }

            if (readingEdges) {
                // add edge from this line

                stringArray = currentLine.split(",").toTypedArray()

                // nodes have to be present, because they have been defined before reading any edges in the file
                source = transducerNodes[stringArray[0]]!!
                target = transducerNodes[stringArray[1]]!!

                incoming = stringArray[2]
                outgoing = stringArray[3]
                cost = stringArray[4].toDouble()
                transducerGraph.addEdge(source, target, incoming, outgoing, cost)

            }
        }

        return transducerGraph
    }

    fun generateClassicAnswersTransducer(alphabet: Set<String>): TransducerGraph {

        val transducerGraph = TransducerGraph()
        val source = TransducerNode("t0", isInitialState = true, isFinalState = true)

        for (word in alphabet) {
            // for each word of the alphabet we add the edge (t0, t0, word, word, 0)
            transducerGraph.addEdge(source, source, word, word, 0.0)
        }

        return transducerGraph
    }

    fun generateEditDistanceTransducer(alphabet: Set<String>): TransducerGraph {
        return TODO()
    }
}