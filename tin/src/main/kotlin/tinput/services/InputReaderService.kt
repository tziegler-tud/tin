package tinput.services

import tin.data.input.FileInputData
import tin.model.DataProvider
import tin.model.transducer.TransducerGraph
import tinput.services.individualFiles.DatabaseReader
import tinput.services.individualFiles.QueryReader
import tinput.services.individualFiles.TransducerReader

class InputReaderService {

    fun importRegularPathQueryFromSeparateFiles(fileInputData: FileInputData): DataProvider {
        val queryReader = QueryReader()
        val transducerReader = TransducerReader()
        val databaseReader = DatabaseReader()

        val queryGraph = queryReader.readRegularPathQueryFile(fileInputData.queryFile)
        val transducerGraph = transducerReader.readTransducerFile(fileInputData.transducerFile)
        val databaseGraph = databaseReader.readDatabaseFile(fileInputData.databaseFile)

        return DataProvider(queryGraph, transducerGraph, databaseGraph)
    }

}