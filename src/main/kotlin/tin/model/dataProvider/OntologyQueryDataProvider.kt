package tin.model.dataProvider


import tin.model.alphabet.Alphabet
import tin.model.database.DatabaseGraph
import tin.model.query.QueryGraph
import tin.model.transducer.TransducerGraph
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


