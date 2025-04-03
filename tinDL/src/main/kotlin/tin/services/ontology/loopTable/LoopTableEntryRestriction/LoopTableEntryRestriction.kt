package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.util.ShortFormProvider


interface LoopTableEntryRestriction {
    val value: Any

    fun isEmpty(): Boolean;

    fun transformToString(shortFormProvider: ShortFormProvider): String
}