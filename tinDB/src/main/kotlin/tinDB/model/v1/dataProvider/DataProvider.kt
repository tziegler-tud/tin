package tinDB.model.v1.dataProvider

import tinDB.model.alphabet.Alphabet
import tinDB.model.database.DatabaseGraph
import tinDB.model.v1.transducer.TransducerGraph

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null

}