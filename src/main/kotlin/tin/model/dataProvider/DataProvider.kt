package tin.model.dataProvider


import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.query.QueryGraph
import tin.model.transducer.TransducerGraph
import java.util.HashSet

// the dataProvider holds the three data graphs.
class DataProvider(
        var queryGraph: QueryGraph,
        var transducerGraph: TransducerGraph,
        var databaseGraph: DatabaseGraph,
        var alphabet: Alphabet = Alphabet()
) {

    var dataSetIdentifier: String? = null

}