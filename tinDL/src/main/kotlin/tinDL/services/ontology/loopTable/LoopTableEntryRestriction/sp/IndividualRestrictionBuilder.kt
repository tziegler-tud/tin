package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.util.ShortFormProvider
import tinDL.services.ontology.DLQueryParser
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

class IndividualRestrictionBuilder(
    private val queryParser: DLQueryParser,
    private val shortFormProvider: ShortFormProvider
) : RestrictionBuilderInterface {


    fun createNamedIndividualRestriction(element: OWLNamedIndividual): NamedIndividualRestriction {
        return NamedIndividualRestriction(element)
    }
}