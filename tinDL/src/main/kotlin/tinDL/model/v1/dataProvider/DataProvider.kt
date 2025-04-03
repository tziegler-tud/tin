package tinDL.model.v1.dataProvider

import tinDL.model.v1.alphabet.Alphabet
import tinDL.model.v1.database.DatabaseGraph
import tinDL.model.v1.transducer.TransducerGraph

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null
}