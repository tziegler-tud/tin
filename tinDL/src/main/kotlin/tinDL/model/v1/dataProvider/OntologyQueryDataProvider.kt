package tinDL.model.v1.dataProvider


import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyManager

// the dataProvider holds the three data graphs.
class OntologyQueryDataProvider(
    var queryGraph: QueryGraph,
    transducerGraph: TransducerGraph,
    ontologyManager: OntologyManager,
    val sourceVariableName: String? = null,
    val targetVariableName: String? = null,
    alphabet: Alphabet = Alphabet(),
)


