package tinDB.model.v2.dataProvider

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.transducer.TransducerGraph

abstract class DataProvider (
    var alphabet: Alphabet = Alphabet(),
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
){
    var dataSetIdentifier: String? = null

}