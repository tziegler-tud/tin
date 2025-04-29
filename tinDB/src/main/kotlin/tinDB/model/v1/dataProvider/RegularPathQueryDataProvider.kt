package tinDB.model.v1.dataProvider


import tinDB.model.alphabet.Alphabet
import tinDB.model.database.DatabaseGraph
import tinDB.model.v1.query.QueryGraph
import tinDB.model.v1.transducer.TransducerGraph

// the dataProvider holds the three data graphs.
class RegularPathQueryDataProvider(
    var queryGraph: QueryGraph,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    val sourceVariableName: String? = null,
    val targetVariableName: String? = null,
    alphabet: Alphabet = Alphabet(),
    ) : DataProvider(
    alphabet, transducerGraph, databaseGraph
)


