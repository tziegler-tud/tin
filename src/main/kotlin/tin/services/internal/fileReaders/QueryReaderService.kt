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

    override fun processFile(file: File): QueryGraph {
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


            // remove whitespace in current line
            currentLine = currentLine.replace("\\s".toRegex(), "")

            when(currentlyReading){
                InputTypeEnum.NODES -> {
                    stringArray = currentLine.split(",").toTypedArray()

                    node = QueryNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())
                    queryNodes[stringArray[0]] = node
                    queryGraph.addNodes(node)
                }

                InputTypeEnum.EDGES -> {
                    stringArray = currentLine.split(",").toTypedArray()

                    // nodes have to be present, because they have been defined before reading any edges in the file
                    source = queryNodes[stringArray[0]]!!
                    target = queryNodes[stringArray[1]]!!

                    edgeLabel = stringArray[2]
                    alphabet.addRoleName(edgeLabel)

                    queryGraph.addEdge(source, target, edgeLabel)
                }

                else -> {

                }
            }
        }

        queryGraph.alphabet = alphabet
        return queryGraph
    }
}