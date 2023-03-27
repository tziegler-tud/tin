import org.springframework.stereotype.Service
import tin.data.input.FileInputData
import tin.data.input.GenerateTransducer
import tin.model.database.DatabaseGraphFile
import tin.model.database.DatabaseGraphFileRepository
import tinput.services.InputReaderService

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val inputReaderService = InputReaderService()
            val fileInputData = FileInputData("tin/src/main/resources/exampleQueryGraph.txt", "tin/src/main/resources/exampleTransducerGraph.txt", "tin/src/main/resources/exampleDatabaseGraph.txt", GenerateTransducer.classicAnswers)
            val dataProvider = inputReaderService.importRegularPathQueryFromSeparateFiles(fileInputData)

            // println("query node size: " + dataProvider.queryGraph.nodes.size)
            // println("transducer node size: " + dataProvider.transducerGraph.nodes.size)
            // println("database node size: " + dataProvider.databaseGraph.nodes.size)
        }
    }
}