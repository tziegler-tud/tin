package tinDL.model.v2.ResultGraph

import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinLIB.model.v2.ResultGraph.ResultGraphIndividual

class DlResultGraphIndividual(
    val owlIndividual: OWLNamedIndividual
)
    : ResultGraphIndividual(
        owlIndividual.iri.toString()
    )
{
    fun getIri() : IRI {
        return owlIndividual.iri
    }
}
