import tin.data.input.FileInputData
import tinput.services.InputReaderService

fun main() {
    val inputReaderService = InputReaderService()
    val fileInputData = FileInputData("tin/src/main/resources/exampleQueryGraph.txt", "tin/src/main/resources/exampleTransducerGraph.txt", "tin/src/main/resources/exampleDatabaseGraph.txt", false)
    val dataProvider = inputReaderService.importRegularPathQueryFromSeparateFiles(fileInputData)
    println("node size: " + dataProvider.queryGraph.nodes.size)

}