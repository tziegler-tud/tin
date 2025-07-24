package tinDB.model.v1.dataProvider


import tinDB.model.v1.database.DatabaseGraph
import tinDB.model.v1.query.QueryGraph
import tinDB.model.v1.transducer.TransducerGraph
import tinLIB.model.v2.alphabet.Alphabet

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


