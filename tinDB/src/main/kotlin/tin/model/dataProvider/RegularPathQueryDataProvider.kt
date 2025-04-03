package tin.model.dataProvider


import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.query.QueryGraph
import tin.model.transducer.TransducerGraph

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


