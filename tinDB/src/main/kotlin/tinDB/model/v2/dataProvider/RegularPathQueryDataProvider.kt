package tinDB.model.v2.dataProvider

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph

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


