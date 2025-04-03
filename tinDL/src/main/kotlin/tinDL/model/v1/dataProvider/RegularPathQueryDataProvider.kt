package tinDL.model.v1.dataProvider


import tinDL.model.v1.alphabet.Alphabet
import tinDL.model.v1.database.DatabaseGraph
import tinDL.model.v1.query.QueryGraph
import tinDL.model.v1.transducer.TransducerGraph

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


