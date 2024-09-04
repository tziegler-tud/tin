package tin.model.v1.dataProvider


import tin.model.v1.alphabet.Alphabet
import tin.model.v1.query.QueryGraph
import tin.model.v1.transducer.TransducerGraph
import tin.services.ontology.OntologyManager

// the dataProvider holds the three data graphs.
class OntologyQueryDataProvider(
    var queryGraph: QueryGraph,
    transducerGraph: TransducerGraph,
    ontologyManager: OntologyManager,
    val sourceVariableName: String? = null,
    val targetVariableName: String? = null,
    alphabet: Alphabet = Alphabet(),
)


