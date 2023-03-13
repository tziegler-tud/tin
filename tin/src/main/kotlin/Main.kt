import tin.data.input.FileInputData
import tin.data.input.GenerateTransducer
import tinput.services.InputReaderService

fun main() {
    val inputReaderService = InputReaderService()
    val fileInputData = FileInputData("tin/src/main/resources/exampleQueryGraph.txt", "tin/src/main/resources/exampleTransducerGraph.txt", "tin/src/main/resources/exampleDatabaseGraph.txt", GenerateTransducer.classicAnswers)
    val dataProvider = inputReaderService.importRegularPathQueryFromSeparateFiles(fileInputData)

    // println("query node size: " + dataProvider.queryGraph.nodes.size)
    // println("transducer node size: " + dataProvider.transducerGraph.nodes.size)
    // println("database node size: " + dataProvider.databaseGraph.nodes.size)

}