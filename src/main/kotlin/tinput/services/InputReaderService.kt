package tinput.services

import tin.data.input.QueryData
import tin.data.input.GenerateTransducer
import tin.model.tintheweb.DataProvider
import tin.model.transducer.TransducerGraph
import tinput.services.individualFiles.DatabaseReader
import tinput.services.individualFiles.QueryReader
import tinput.services.individualFiles.TransducerReader

class InputReaderService {

    fun importRegularPathQueryFromSeparateFiles(fileInputData: QueryData): DataProvider {
        val queryReader = QueryReader()
        val transducerReader = TransducerReader()
        val databaseReader = DatabaseReader()

        val queryGraph = queryReader.readRegularPathQueryFile(fileInputData.queryFile)
        val databaseGraph = databaseReader.readDatabaseFile(fileInputData.databaseFile)

        val transducerGraph: TransducerGraph
        val alphabet = queryGraph.alphabet.plus(databaseGraph.alphabet)

        transducerGraph = when (fileInputData.generateTransducer) {
            GenerateTransducer.NoGeneration -> transducerReader.readTransducerFile(fileInputData.transducerFile)
            GenerateTransducer.ClassicAnswers -> transducerReader.generateClassicAnswersTransducer(alphabet)
            GenerateTransducer.EditDistance -> transducerReader.generateEditDistanceTransducer(alphabet)
        }

        return DataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)
    }

}