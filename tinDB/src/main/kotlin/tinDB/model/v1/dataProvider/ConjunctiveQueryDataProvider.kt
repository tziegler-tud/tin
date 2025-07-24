package tinDB.model.v1.dataProvider

import tinDB.model.v2.ConjunctiveFormula
import tinDB.model.v2.ConjunctiveQueryGraphMap
import tinDB.model.v1.database.DatabaseGraph
import tinDB.model.v1.transducer.TransducerGraph
import tinLIB.model.v2.alphabet.Alphabet

class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)