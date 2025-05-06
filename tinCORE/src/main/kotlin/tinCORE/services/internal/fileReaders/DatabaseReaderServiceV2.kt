package tinCORE.services.internal.fileReaders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinDB.model.v2.DatabaseGraph.DatabaseNode
import tinDB.model.v2.DatabaseGraph.DatabaseProperty
import tinLIB.model.v2.alphabet.Alphabet
import java.io.BufferedReader
import java.io.File
import java.util.HashMap
@Service
class DatabaseReaderServiceV2 @Autowired constructor(
        systemConfigurationService: SystemConfigurationService
) : FileReaderService<FileReaderResult<DatabaseGraph>>(
        systemConfigurationService
) {

    override var filePath = systemConfigurationService.getDatabasePath()
    override var inputFileMaxLines : Int = systemConfigurationService.getDatabaseSizeLimit()


    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<DatabaseGraph> {
        val databaseGraph = DatabaseGraph()
        val databaseNodes = HashMap<String, DatabaseNode>() // map containing the QueryNodes
        val alphabet = Alphabet()

        var source: DatabaseNode
        var target: DatabaseNode
        var node: DatabaseNode
        var edgeLabel: String
        var property: String
        var stringArray: Array<String>

        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String

        val bufferedReader: BufferedReader = file.bufferedReader()

        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //trailing and leading whitespaces and tab characters are removed before processing!

        //node line. Executed before whitespace removal!
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*")

        // edge lines. Executed before whitespace removal!
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*") // Important: no ? allowed

        // property lines. Executed before whitespace removal!
        val anyPropertyRegex = Regex("\\w(\\w|-\\w)*\\s*(,\\s*\\w(\\w|-\\w)*\\s*)+") // At least one prop

        var currentLineIndex: Int = 0
        while (currentLineIndex < inputFileMaxLines) {
            currentLineIndex++
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            //remove leading and trailing whitespaces and tab characters
            currentLine = currentLine.replace(Regex("^\\s*"), "")
            currentLine = currentLine.replace(Regex("\\s*$"), "")

            //empty lines and lines starting with // are ignored
            if(commentLineRegex.matchEntire(currentLine) !== null || currentLine.isEmpty()){
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

            if (currentLine == "properties") {
                currentlyReading = InputTypeEnum.PROPERTIES

                // after setting the flags, we skip into the next line
                continue
            }


            //save og line for debugging
            val originalLine = currentLine

            when(currentlyReading){
                InputTypeEnum.NODES -> {

                    if(anyNodeRegex.matchEntire(currentLine)!== null){
                        currentLine = currentLine.replace("\\s".toRegex(), "")

                        val identifier = currentLine

                        if(databaseNodes.contains(identifier)){
                            //node identifier already taken, create warning
                            this.warn("Duplicated node identifier.", currentLineIndex, originalLine)
                            continue
                        }
                        node = DatabaseNode(identifier)
                        databaseNodes[identifier] = node
                        databaseGraph.addNodes(node)
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
                        source = databaseNodes[stringArray[0]]!!
                        target = databaseNodes[stringArray[1]]!!

                        edgeLabel = stringArray[2]

                        databaseGraph.addEdge(source, target, edgeLabel)
                        alphabet.addRoleName(edgeLabel)
                    }
                    else {
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, originalLine)
                        if(breakOnError) break
                    }
                }

                InputTypeEnum.PROPERTIES -> {
                    if(anyPropertyRegex.matchEntire(currentLine)!== null) {
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        val propertyArray = stringArray.copyOf().drop(1)
                        val properties: HashSet<DatabaseProperty> = propertyArray.map { DatabaseProperty(it) }.toHashSet()

                        // nodes have to be present, because they have been defined before reading any edges in the file
                        val nodeOrNull = databaseGraph.getNode(stringArray[0])
                        if(nodeOrNull == null){
                            val msg = "Failed to read line as property: No node with identifier '" + stringArray[0] + "' found."
                            this.error(msg, currentLineIndex, originalLine)
                            if(breakOnError) break
                        }
                        else {
                            node = nodeOrNull
                            properties.forEach {
                                if(node.hasProperty(it)){
                                    this.warn("Redundant property assignment: Property '"+ it + "' already assigned to node '" + stringArray[0] + "'", currentLineIndex, originalLine)
                                }
                                node.addProperty(it)
                                alphabet.addConceptName(it.toString())
                            }
                        }
                    }
                    else {
                        this.error("Failed to read line as property: Invalid input format.", currentLineIndex, originalLine)
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

        databaseGraph.alphabet = alphabet

        //debug output
        //databaseGraph.printGraph();

        return FileReaderResult(databaseGraph, this.warnings, this.errors)

    }
}