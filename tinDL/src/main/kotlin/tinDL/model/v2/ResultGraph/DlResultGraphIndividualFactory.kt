package tinDL.model.v2.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.util.ShortFormProvider

class DlResultGraphIndividualFactory(
    private val shortFormProvider: ShortFormProvider
) {
    fun fromOWLNamedIndividual(owlNamedIndividual: OWLNamedIndividual): DlResultGraphIndividual {
        val shortForm = shortFormProvider.getShortForm(owlNamedIndividual);
        return DlResultGraphIndividual(owlNamedIndividual, shortForm)
    }
}