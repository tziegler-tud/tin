package tin.services.internal.fileReaders

import org.springframework.stereotype.Service
import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import java.util.HashMap

@Service
class QueryReaderService (
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<QueryGraph>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getQueryPath();
    var inputFileMaxLines : Int = systemConfigurationService.getQuerySizeLimit()

    override fun processFile(file: File): FileReaderResult<QueryGraph> {
        val queryGraph = QueryGraph()
        val queryNodes = HashMap<String, QueryNode>() // map containing the QueryNodes
        val alphabet = Alphabet()

        var source: QueryNode
        var target: QueryNode
        var node: QueryNode
        var edgeLabel: String
        var stringArray: Array<String>

        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String


        val bufferedReader: BufferedReader = file.bufferedReader()

        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*,((true)|(false)),((true)|(false))");

        // edge lines
        val edgeWithConceptAssertionRegex = Regex("\\w(\\w|-\\w)*,\\w(\\w|-\\w)*,\\w(\\w|-\\w)*\\?") //for concept assertions
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*,\\w(\\w|-\\w)*,\\w(\\w|-\\w)*\\??") // for roles

        var currentLineIndex=0;
        while (currentLineIndex <= inputFileMaxLines) {
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
                    val nodeMatchResult = anyNodeRegex.matchEntire(currentLine);
                    if(nodeMatchResult !== null) {
                        stringArray = currentLine.split(",").toTypedArray()

                        node = QueryNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())
                        queryNodes[stringArray[0]] = node
                        queryGraph.addNodes(node)
                    }
                    else {
                        this.error("Failed to read line as node: Invalid input format.", currentLineIndex, currentLine);
                        break;
                    }
                }
                InputTypeEnum.EDGES -> {
                    //check line against Regexp to check for valid input format.
                    val anyEdgeMatchResult = anyEdgeRegex.matchEntire(currentLine);
                    if(anyEdgeMatchResult !== null){
                        //line is valid

                        stringArray = currentLine.split(",").toTypedArray()

                        // nodes have to be present, because they have been defined before reading any edges in the file
                        source = queryNodes[stringArray[0]]!!
                        target = queryNodes[stringArray[1]]!!

                        edgeLabel = stringArray[2]
                        queryGraph.addEdge(source, target, edgeLabel)


                        val conceptEdgeResult = edgeWithConceptAssertionRegex.matchEntire(currentLine);
                        if(conceptEdgeResult === null){
                            //not a concept assertions
                            alphabet.addRoleName(edgeLabel)
                        }
                        else {
                            //concept assertion read, extract concept name
                            try{
                                val conceptLabel = Alphabet.conceptNameFromAssertion(edgeLabel);
                                alphabet.addConceptName(edgeLabel)
                            }
                            catch (e: IllegalArgumentException){
                                this.warn("Failed to read property name from edge label", currentLineIndex, currentLine)
                            }
                        }
                    }
                    else {
                        //invalid line
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, currentLine);
                        break;
                    }
                }

                else -> {
                    this.warn("Unhandled line.", currentLineIndex, currentLine)
                }
            }
        }

        queryGraph.alphabet = alphabet
        return FileReaderResult<QueryGraph>(queryGraph, this.warnings, this.errors);
    }
}