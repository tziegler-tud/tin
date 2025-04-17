package tinDL.services.ontology.loopTable.loopTableEntry

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.util.ShortFormProvider
import tinLIB.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface LoopTableEntry {
    val source: Pair<Node, Node>;
    val target: Pair<Node, Node>
    val restriction: LoopTableEntryRestriction

    fun hasEqualSourceAndTarget() : Boolean


    fun transformToString(shortFormProvider: ShortFormProvider): String
}