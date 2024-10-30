package tin.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

class IndividualRestrictionBuilder(
    private val queryParser: DLQueryParser,
    private val shortFormProvider: ShortFormProvider
) : RestrictionBuilderInterface<OWLIndividual> {


    fun createNamedIndividualRestriction(element: OWLIndividual): NamedIndividualRestriction {
        return NamedIndividualRestriction(element)
    }

    fun createAnonymousIndividualRestriction(element: OWLIndividual): AnonymousIndividualRestriction {
        return AnonymousIndividualRestriction(element)
    }

    override fun createRestriction(element: OWLIndividual): LoopTableEntryRestriction {
        return createRestriction(element);
    }
}