package tinDB.model.v1.dataProvider

import tinDB.model.v1.database.DatabaseGraph
import tinDB.model.v1.transducer.TransducerGraph
import tinLIB.model.v2.alphabet.Alphabet

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null

}