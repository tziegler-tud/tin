package tin.model

import tin.model.database.DatabaseGraph
import tin.model.transducer.TransducerGraph

class ConjunctiveRPQDataProvider(
    var queryGraphMap: ConjunctiveQueryGraphMap,
    var transducerGraph: TransducerGraph,
    var databaseGraph: DatabaseGraph,
    var formula: ConjunctiveFormula
) {
}