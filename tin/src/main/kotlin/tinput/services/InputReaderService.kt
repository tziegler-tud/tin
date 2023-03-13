package tinput.services

import tin.data.input.FileInputData
import tin.data.input.GenerateTransducer
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
        val databaseGraph = databaseReader.readDatabaseFile(fileInputData.databaseFile)

        val transducerGraph: TransducerGraph
        val alphabet = queryGraph.alphabet.plus(databaseGraph.alphabet)

        when (fileInputData.generateTransducer) {
            GenerateTransducer.noGeneration -> transducerGraph = transducerReader.readTransducerFile(fileInputData.transducerFile)
            GenerateTransducer.classicAnswers -> transducerGraph = transducerReader.generateClassicAnswersTransducer(alphabet)
            GenerateTransducer.editDistance -> transducerGraph = transducerReader.generateEditDistanceTransducer(alphabet)
        }

        return DataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)
    }

}