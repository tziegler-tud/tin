package tinDB.model.v1.dataProvider

import tinDB.model.ConjunctiveFormula
import tinDB.model.ConjunctiveQueryGraphMap
import tinDB.model.alphabet.Alphabet
import tinDB.model.database.DatabaseGraph
import tinDB.model.v1.transducer.TransducerGraph

class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)