package tinDL.model.v1.dataProvider

import tinDL.model.ConjunctiveFormula
import tinDL.model.ConjunctiveQueryGraphMap
import tinDL.model.v1.alphabet.Alphabet
import tinDL.model.v1.database.DatabaseGraph
import tinDL.model.v1.transducer.TransducerGraph

class ConjunctiveQueryDataProvider(
    var conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    alphabet: Alphabet = Alphabet()
): DataProvider(
    alphabet, transducerGraph, databaseGraph
)