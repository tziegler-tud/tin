package tinLIB.services.internal.fileReaders

import tinLIB.model.v1.alphabet.Alphabet
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.model.v2.graph.Node
import tinLIB.services.internal.fileReaders.fileReaderResult.FileReaderResult
import java.io.BufferedReader
import java.io.File

class TransducerReaderServiceV2 (
    filePath: String,
    private val inputFileMaxLines: Int
) : FileReaderService<FileReaderResult<TransducerGraph>>(
        filePath
) {

    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<TransducerGraph> {
        val transducerGraph = TransducerGraph()
        val transducerNodes = HashMap<String, Node>() // map containing the TransducerNodes
        val alphabet = Alphabet()

        var source: Node
        var target: Node
        var node: Node
        var incoming: String
        var outgoing: String
        var cost: Int
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
//        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*[\\w-](\\w|-\\w)*\\??\\s*,\\s*[\\w-](\\w|-\\w)*\\??\\s*,\\s*\\d")
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*(inverse\\()?\\s*\\w(\\w|-\\w)*(\\s*\\)|\\?)?\\s*,\\s*(inverse\\()?\\s*\\w(\\w|-\\w)*(\\s*\\)|\\?)?\\s*,\\s*\\d+")

        var currentLineIndex: Int = 0
        while (currentLineIndex < inputFileMaxLines) {
            currentLineIndex++
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            //remove leading and trailing whitespaces and tab characters
            currentLine = currentLine.replace(Regex("^\\s*"), "")
            currentLine = currentLine.replace(Regex("\\s*$"), "")

            //ignore empty lines
            if(currentLine.isEmpty()) continue

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

                        node = Node(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())

                        val existingNode = transducerNodes[stringArray[0]]
                        if(existingNode != null){
                            //node identifier already taken. Check similarity.
                            if(node == existingNode) {
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
                        cost = stringArray[4].toInt()
                        /**
                         * @throws IllegalArgumentException
                         */
                        val addEdgeToAlphabet = fun (edgeLabel: String) {
                            if(Alphabet.isConceptAssertion(edgeLabel)){
                                //concept assertion read, extract concept name
                                try{
                                    val conceptLabel = Alphabet.conceptNameFromAssertion(edgeLabel)
                                    if(!alphabet.includes(conceptLabel)) alphabet.addConceptName(conceptLabel)
                                }
                                catch (e: IllegalArgumentException){
                                    throw IllegalArgumentException("Failed to read property name from edge label")
                                }
                            }
                            else {
                                //not a concept assertions
                                //check for valid role name
                                if(Alphabet.isValidRoleName(edgeLabel)){
                                    var normalizedEdgeLabel = edgeLabel;
                                    if(Alphabet.isInverseRoleName(edgeLabel)){
                                        //inverse role name found, handle accordingly
                                        normalizedEdgeLabel = Alphabet.transformToPositiveRoleName(edgeLabel);
                                    }
                                    else {
                                        //non-inverse (positive) role name found
                                    }
                                    if(!alphabet.includes(normalizedEdgeLabel)) alphabet.addRoleName(normalizedEdgeLabel)
                                }
                                else {
                                    //invalid role name found, throw error
                                    throw IllegalArgumentException("Failed to read line as edge: Invalid role name given.")
                                }
                            }
                        }

                        try{
                            addEdgeToAlphabet(incoming)
                            addEdgeToAlphabet(outgoing)
                        }
                        catch (e: IllegalArgumentException){
                            this.error(e.message?:"Failed to read property name from edge label", currentLineIndex, currentLine)
                        }
                        transducerGraph.addEdge(source, target, incoming, outgoing, cost)
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