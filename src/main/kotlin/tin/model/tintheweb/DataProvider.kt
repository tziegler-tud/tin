package tin.model.tintheweb


import tin.model.database.DatabaseGraph
import tin.model.query.QueryGraph
import tin.model.transducer.TransducerGraph
import java.util.HashSet

// the dataProvider holds the three data graphs.
class DataProvider(
        var queryGraph: QueryGraph,
        var transducerGraph: TransducerGraph,
        var databaseGraph: DatabaseGraph,
        var alphabet: Set<String> = HashSet()
) {

    var dataSetIdentifier: String? = null

}