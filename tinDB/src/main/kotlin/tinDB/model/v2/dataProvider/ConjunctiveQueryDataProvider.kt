package tinDB.model.v2.dataProvider

import tinDB.model.v2.ConjunctiveFormula
import tinDB.model.v2.ConjunctiveQueryGraphMap
import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.transducer.TransducerGraph


class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)