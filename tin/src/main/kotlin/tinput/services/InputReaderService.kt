package tinput.services

import tin.data.input.FileInputData
import tin.model.DataProvider
import tin.model.database.DatabaseGraph
import tin.model.transducer.TransducerGraph
import tinput.services.individualFiles.QueryReader

class InputReaderService {

    fun importRegularPathQueryFromSeparateFiles(fileInputData: FileInputData): DataProvider {
        val queryReader = QueryReader()

        val queryGraph = queryReader.readRegularPathQueryFile(fileInputData.queryFile)
        val transducerGraph = TransducerGraph()
        val databaseGraph = DatabaseGraph()


        return DataProvider(queryGraph, transducerGraph, databaseGraph)
    }

}