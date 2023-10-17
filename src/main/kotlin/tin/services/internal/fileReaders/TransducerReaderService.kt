package tin.services.internal.fileReaders


import org.springframework.stereotype.Service
import tin.model.database.DatabaseGraph
import tin.model.transducer.TransducerGraph
import tin.model.transducer.TransducerNode
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File

@Service
class TransducerReaderService (
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<TransducerGraph>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getTransducerPath();

    override fun processFile(file: File): TransducerGraph {
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


        val bufferedReader: BufferedReader = file.bufferedReader()

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
                if (incoming.isEmpty()) {
                    incoming = replaceEmptyStringWithInternalEpsilon()
                }
                outgoing = stringArray[3]
                if (outgoing.isEmpty()) {
                    outgoing = replaceEmptyStringWithInternalEpsilon()
                }
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

    /**
     * todo
     *  consider making the internal epsilon identifier a property the user can change in the frontend.
     *  (duplicate with ProductAutomatonService)
     */
    private fun replaceEmptyStringWithInternalEpsilon(): String {
        return "epsilon"
    }
}