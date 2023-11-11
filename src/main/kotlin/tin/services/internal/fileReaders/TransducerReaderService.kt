package tin.services.internal.fileReaders


import org.springframework.stereotype.Service
import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.query.QueryNode
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
    override var inputFileMaxLines : Int = systemConfigurationService.getTransducerSizeLimit();

    override fun processFile(file: File): FileReaderResult<TransducerGraph> {
        val transducerGraph = TransducerGraph()
        val transducerNodes = HashMap<String, TransducerNode>() // map containing the TransducerNodes

        var source: TransducerNode
        var target: TransducerNode
        var node: TransducerNode
        var incoming: String
        var outgoing: String
        var cost: Double
        var stringArray: Array<String>

        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String


        val bufferedReader: BufferedReader = file.bufferedReader()


        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*,((true)|(false)),((true)|(false))");

        // edge lines node, node, edgeLabel, edgeLabel, cost
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*,\\w(\\w|-\\w)*,\\w(\\w|-\\w)*\\??,\\w(\\w|-\\w)*\\??,\\d")

        var currentLineIndex: Int = 0;
        while (currentLineIndex < inputFileMaxLines) {
            currentLineIndex++;
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            // when we see "nodes", we will read nodes starting from the next line
            if (currentLine == "nodes") {
                currentlyReading = InputTypeEnum.NODES
                // after setting the flags, we skip into the next line
                currentLine = bufferedReader.readLine() ?: break;
            }

            if (currentLine == "edges") {
                currentlyReading = InputTypeEnum.EDGES

                // after setting the flags, we skip into the next line
                currentLine = bufferedReader.readLine() ?: break;
            }


            // remove whitespace in current line
            currentLine = currentLine.replace("\\s".toRegex(), "")

            when(currentlyReading){
                InputTypeEnum.NODES -> {

                    if(anyNodeRegex.matchEntire(currentLine)!== null) {
                        stringArray = currentLine.split(",").toTypedArray()

                        node = TransducerNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())
                        transducerNodes[stringArray[0]] = node
                        transducerGraph.addNodes(node)

                        //TODO: Check semantically, e.g. if there is at least one initial state and at least one reachable final state.
                    }
                    else {
                        this.error("Failed to read line as node: Invalid input format.", currentLineIndex, currentLine);
                        break;
                    }
                }

                InputTypeEnum.EDGES -> {
                    if(anyEdgeRegex.matchEntire(currentLine)!== null) {
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
                    else {
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, currentLine);
                        break;
                    }
                }

                else -> {
                    this.warn("Unhandled line.", currentLineIndex, currentLine)
                }
            }
        }

        if(currentLineIndex == inputFileMaxLines && bufferedReader.readLine() !== null){
            this.warn("Max input file size reached. Reader stopped before entire file was processed!", currentLineIndex, "");
        }
        return FileReaderResult<TransducerGraph>(transducerGraph, this.warnings, this.errors);

    }

    fun generateClassicAnswersTransducer(alphabet: Alphabet): TransducerGraph {

        val transducerGraph = TransducerGraph()
        val source = TransducerNode("t0", isInitialState = true, isFinalState = true)

        for (word in alphabet.getAlphabet()) {
            // for each word of the alphabet we add the edge (t0, t0, word, word, 0)
            transducerGraph.addEdge(source, source, word, word, 0.0)
        }

        return transducerGraph
    }

    fun generateEditDistanceTransducer(alphabet: Alphabet): TransducerGraph {
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