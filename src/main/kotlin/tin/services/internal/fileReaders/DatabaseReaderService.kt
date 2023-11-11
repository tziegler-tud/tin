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

        while (true) {
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
                    stringArray = currentLine.split(",").toTypedArray()

                    node = DatabaseNode(stringArray[0])
                    databaseNodes[stringArray[0]] = node
                    databaseGraph.addNodes(node)
                }

                InputTypeEnum.EDGES -> {
                    stringArray = currentLine.split(",").toTypedArray()

                    // nodes have to be present, because they have been defined before reading any edges in the file
                    source = databaseNodes[stringArray[0]]!!
                    target = databaseNodes[stringArray[1]]!!

                    edgeLabel = stringArray[2]
                    alphabet.addRoleName(edgeLabel)

                    databaseGraph.addEdge(source, target, edgeLabel)
                }

                InputTypeEnum.PROPERTIES -> {
                    stringArray = currentLine.split(",").toTypedArray()

                    val properties = stringArray.copyOf().drop(1);

                    // nodes have to be present, because they have been defined before reading any edges in the file
                    try {
                        node = databaseNodes[stringArray[0]]!!
                    }
                    catch(e: Error){
                        val msg = "Invalid input line: Trying to add properties to Node with identifier '" + stringArray[0] + "'. Reason: Node is not present in database graph!";
                        throw Error(msg); //TODO: set specific error type
                    }

                    properties.forEach{
                        databaseGraph.addNodeProperty(node, it);
                        alphabet.addConceptName(it)
                    }
                }

                else -> {
                    //trying to read line without knowing in which part of the input file we are.
                    // Perhaps we encountered a blank line at the start of the document. Still, this is bad and should not happen.
                    println("WARNING: DatabaseReaderService: Expected a section identifier, but none was found. This is likely due to a malformed input file. Skipping this line..." );
                    break;
                }
            }
        }

        databaseGraph.alphabet = alphabet
        //debug output
        databaseGraph.printGraph();
        return FileReaderResult<DatabaseGraph>(databaseGraph, this.warnings, this.errors);

    }
}