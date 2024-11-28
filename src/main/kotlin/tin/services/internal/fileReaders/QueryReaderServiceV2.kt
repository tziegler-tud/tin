package tin.services.internal.fileReaders

import org.springframework.stereotype.Service
import tin.model.v1.alphabet.Alphabet
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File
import java.util.HashMap

@Service
class QueryReaderServiceV2 (
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<FileReaderResult<QueryGraph>>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getQueryPath()
    override var inputFileMaxLines : Int = systemConfigurationService.getQuerySizeLimit()

    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<QueryGraph> {
        val queryGraph = QueryGraph()
        val queryNodes = HashMap<String, Node>() // map containing the QueryNodes
        val alphabet = Alphabet()

        var source: Node
        var target: Node
        var node: Node
        var edgeLabel: String
        var stringArray: Array<String>

        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String


        val bufferedReader: BufferedReader = file.bufferedReader()

        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //trailing and leading whitespaces and tab characters are removed before processing!
        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*((true)|(false))\\s*,\\s*((true)|(false))")

        // edge lines
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*(inverse\\()?\\s*\\w(\\w|-\\w)*(\\s*\\)|\\?)?") // for roles

        var currentLineIndex=0
        while (currentLineIndex <= inputFileMaxLines) {
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
                    val nodeMatchResult = anyNodeRegex.matchEntire(currentLine)
                    if(nodeMatchResult !== null) {
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        node = Node(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())

                        val existingNode = queryNodes[stringArray[0]]
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
                        queryNodes[stringArray[0]] = node
                        queryGraph.addNodes(node)

                        //TODO: Check semantically, e.g. if there is at least one initial state and at least one reachable final state.
                    }
                    else {
                        this.error("Failed to read line as node: Invalid input format.", currentLineIndex, currentLine)
                        if(breakOnError) break
                    }
                }
                InputTypeEnum.EDGES -> {
                    //check line against Regexp to check for valid input format.
                    val anyEdgeMatchResult = anyEdgeRegex.matchEntire(currentLine)
                    if(anyEdgeMatchResult !== null){
                        //line is valid
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        // nodes have to be present, because they have been defined before reading any edges in the file
                        source = queryNodes[stringArray[0]]!!
                        target = queryNodes[stringArray[1]]!!

                        edgeLabel = stringArray[2];

                        queryGraph.addEdge(source, target, edgeLabel)

                        if(Alphabet.isConceptAssertion(edgeLabel)){
                            //concept assertion read, extract concept name
                            try{
                                val conceptLabel = Alphabet.conceptNameFromAssertion(edgeLabel)
                                if(!alphabet.includes(conceptLabel)) alphabet.addConceptName(conceptLabel)
                            }
                            catch (e: IllegalArgumentException){
                                this.error("Failed to read property name from edge label", currentLineIndex, currentLine)
                                if(breakOnError) break
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
                                this.error("Failed to read line as edge: Invalid role name given.", currentLineIndex, currentLine)
                                if(breakOnError) break
                            }

                        }
                    }
                    else {
                        //invalid line
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, currentLine)
                        if(breakOnError) break
                    }
                }

                else -> {
                    this.warn("Unhandled line.", currentLineIndex, currentLine)
                }
            }
        }

        if(currentLineIndex == inputFileMaxLines && bufferedReader.readLine() !== null){
            this.warn("Max input file size reached. Reader stopped before entire file was processed!", currentLineIndex, "")
        }

        queryGraph.alphabet = alphabet
        return FileReaderResult<QueryGraph>(queryGraph, this.warnings, this.errors)
    }
}