package tin.model.dataProvider

import tin.model.ConjunctiveFormula
import tin.model.ConjunctiveQueryGraphMap
import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.transducer.TransducerGraph

class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)