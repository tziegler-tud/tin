package tin.model.v1.dataProvider

import tin.model.v1.alphabet.Alphabet
import tin.model.v1.database.DatabaseGraph
import tin.model.v1.transducer.TransducerGraph

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null
}