package tin.model.dataProvider

import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.transducer.TransducerGraph

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null
}