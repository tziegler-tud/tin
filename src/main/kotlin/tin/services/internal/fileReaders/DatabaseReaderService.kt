package tin.services.internal.fileReaders

import org.springframework.stereotype.Service
import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import tin.model.query.QueryGraph
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import java.util.HashMap
@Service
class DatabaseReaderService(
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<DatabaseGraph>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getDatabasePath();
    override var inputFileMaxLines : Int = systemConfigurationService.getDatabaseSizeLimit()


    override fun processFile(file: File): FileReaderResult<DatabaseGraph> {
        val databaseGraph = DatabaseGraph()
        val databaseNodes = HashMap<String, DatabaseNode>() // map containing the QueryNodes
        val alphabet = Alphabet();

        var source: DatabaseNode
        var target: DatabaseNode
        var node: DatabaseNode
        var edgeLabel: String
        var property: String
        var stringArray: Array<String>

        var readingNodes = false
        var readingEdges = false
        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String

        val bufferedReader: BufferedReader = file.bufferedReader()

        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*");

        // edge lines
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*,\\w(\\w|-\\w)*,\\w(\\w|-\\w)*") // Important: no ? allowed

        // propertie lines
        val anyPropertyRegex = Regex("\\w(\\w|-\\w)*,\\w(\\w|-\\w)*(,\\w(\\w|-\\w)*)+") // At least one prop

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

            if (currentLine == "properties") {
                currentlyReading = InputTypeEnum.PROPERTIES

                // after setting the flags, we skip into the next line
                currentLine = bufferedReader.readLine() ?: break;
            }


            // remove whitespace in current line
            currentLine = currentLine.replace("\\s".toRegex(), "")

            when(currentlyReading){
                InputTypeEnum.NODES -> {

                    if(anyNodeRegex.matchEntire(currentLine)!== null){
                        stringArray = currentLine.split(",").toTypedArray()

                        node = DatabaseNode(stringArray[0])
                        databaseNodes[stringArray[0]] = node
                        databaseGraph.addNodes(node)
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
                        source = databaseNodes[stringArray[0]]!!
                        target = databaseNodes[stringArray[1]]!!

                        edgeLabel = stringArray[2]
                        alphabet.addRoleName(edgeLabel)

                        databaseGraph.addEdge(source, target, edgeLabel)
                    }
                    else {
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, currentLine);
                        break;
                    }
                }

                InputTypeEnum.PROPERTIES -> {
                    if(anyPropertyRegex.matchEntire(currentLine)!== null) {
                        stringArray = currentLine.split(",").toTypedArray()

                        val properties = stringArray.copyOf().drop(1);

                        // nodes have to be present, because they have been defined before reading any edges in the file
                        try {
                            node = databaseNodes[stringArray[0]]!!
                        } catch (e: Error) {
                            val msg = "Invalid input line: Trying to add properties to Node with identifier '" + stringArray[0] + "'. Reason: Node is not present in database graph!";
                            throw Error(msg); //TODO: set specific error type
                        }

                        properties.forEach {
                            databaseGraph.addNodeProperty(node, it);
                            alphabet.addConceptName(it)
                        }
                    }
                    else {
                        this.error("Failed to read line as property: Invalid input format.", currentLineIndex, currentLine);
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

        databaseGraph.alphabet = alphabet
        //debug output
        databaseGraph.printGraph();
        return FileReaderResult<DatabaseGraph>(databaseGraph, this.warnings, this.errors);

    }
}