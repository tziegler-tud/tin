import tin.data.input.FileInputData
import tinput.services.InputReaderService

fun main() {
    val inputReaderService = InputReaderService()
    val fileInputData = FileInputData("tin v2/src/main/resources/exampleQueryGraph.txt", null, "", false)
    val dataProvider = inputReaderService.importRegularPathQueryFromSeparateFiles(fileInputData)
    println("node size: " + dataProvider.queryGraph.nodes.size)

}