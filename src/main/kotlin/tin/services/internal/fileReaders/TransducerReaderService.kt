package tin.services.internal.fileReaders


import org.springframework.stereotype.Service
import tin.model.v1.alphabet.Alphabet
import tin.model.v1.transducer.TransducerGraph
import tin.model.v1.transducer.TransducerNode
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File

@Service
class TransducerReaderService (
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<FileReaderResult<TransducerGraph>>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getTransducerPath()
    override var inputFileMaxLines : Int = systemConfigurationService.getTransducerSizeLimit()

    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<TransducerGraph> {
        val transducerGraph = TransducerGraph()
        val transducerNodes = HashMap<String, TransducerNode>() // map containing the TransducerNodes
        val alphabet = Alphabet()

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

        //trailing and leading whitespaces and tab characters are removed before processing!

        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*((true)|(false))\\s*,\\s*((true)|(false))")

        // edge lines node, node, edgeLabel, edgeLabel, cost
        // val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\??\\s*,\\s*\\w(\\w|-\\w)*\\??\\s*,\\s*\\d")

        // this should be the correct regex, that allows negative incoming and or outgoing labels; this is not tested extensively thus we keep the old regex as a quick backup
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*[\\w-](\\w|-\\w)*\\??\\s*,\\s*[\\w-](\\w|-\\w)*\\??\\s*,\\s*\\d")

        var currentLineIndex: Int = 0
        while (currentLineIndex < inputFileMaxLines) {
            currentLineIndex++
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            //remove leading and trailing whitespaces and tab characters
            currentLine = currentLine.replace(Regex("^\\s*"), "")
            currentLine = currentLine.replace(Regex("\\s*$"), "")

            //lines starting with // are ignored
            if(commentLineRegex.matchEntire(currentLine) !== null){
                continue
            }
            // when we see "nodes", we will read nodes starting from the next line
            if (currentLine == "nodes") {
                currentlyReading = InputTypeEnum.NODES
                // after setting the flags, we skip into the next line
                continue
            }

            if (currentLine == "edges") {
                currentlyReading = InputTypeEnum.EDGES

                // after setting the flags, we skip into the next line
                continue
            }

            //save og line for debugging
            val originalLine = currentLine

            when(currentlyReading){
                InputTypeEnum.NODES -> {
                    if(anyNodeRegex.matchEntire(currentLine)!== null) {
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        node = TransducerNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())

                        val existingNode = transducerNodes[stringArray[0]]
                        if(existingNode != null){
                            //node identifier already taken. Check similarity.
                            if(node.equalsWithoutEdges(existingNode)) {
                                this.warn("Duplicated node identifier.", currentLineIndex, originalLine)
                                continue
                            }
                            else {
                                //identifier taken, but initialState or finalState differs. This is an error.
                                this.error("Failed to read line as node: Non-repairable duplicated node identifier.", currentLineIndex, originalLine)
                                continue
                            }
                        }

                        transducerNodes[stringArray[0]] = node
                        transducerGraph.addNodes(node)

                        //TODO: Check semantically, e.g. if there is at least one initial state and at least one reachable final state.
                    }
                    else {
                        this.error("Failed to read line as node: Invalid input format.", currentLineIndex, originalLine)
                        if(breakOnError) break

                    }
                }

                InputTypeEnum.EDGES -> {
                    if(anyEdgeRegex.matchEntire(currentLine)!== null) {
                        currentLine = currentLine.replace("\\s".toRegex(), "")
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

                        /**
                         * @throws IllegalArgumentException
                         */
                        val addEdgeToAlphabet = fun (edgeLabel: String) {
                            if(Alphabet.isConceptAssertion(edgeLabel)){
                                //concept assertion read, extract concept name
                                val conceptLabel = Alphabet.conceptNameFromAssertion(edgeLabel)
                                if(!alphabet.includes(conceptLabel)) alphabet.addConceptName(conceptLabel)
                            }
                            else {
                                //not a concept assertions
                                if(!alphabet.includes(edgeLabel)) alphabet.addRoleName(edgeLabel)
                            }
                        }

                        try{
                            addEdgeToAlphabet(incoming)
                            addEdgeToAlphabet(outgoing)
                        }
                        catch (e: IllegalArgumentException){
                            this.error("Failed to read property name from edge label", currentLineIndex, currentLine)
                        }
                    }
                    else {
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, originalLine)
                        if(breakOnError) break
                    }
                }

                else -> {
                    this.warn("Unhandled line.", currentLineIndex, originalLine)
                }
            }
        }

        if(currentLineIndex == inputFileMaxLines && bufferedReader.readLine() !== null){
            this.warn("Max input file size reached. Reader stopped before entire file was processed!", currentLineIndex, "")
        }
        transducerGraph.alphabet = alphabet
        return FileReaderResult<TransducerGraph>(transducerGraph, this.warnings, this.errors)

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