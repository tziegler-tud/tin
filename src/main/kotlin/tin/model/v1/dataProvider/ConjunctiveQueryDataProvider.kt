package tin.model.v1.dataProvider

import tin.model.ConjunctiveFormula
import tin.model.ConjunctiveQueryGraphMap
import tin.model.v1.alphabet.Alphabet
import tin.model.v1.database.DatabaseGraph
import tin.model.v1.transducer.TransducerGraph

class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)