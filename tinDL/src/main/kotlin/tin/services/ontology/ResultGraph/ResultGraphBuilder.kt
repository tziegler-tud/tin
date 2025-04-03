package tin.services.ontology.ResultGraph

import tin.model.v2.ResultGraph.ResultGraph
import tin.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry

interface ResultGraphBuilder {
    //construct GuA -> product graph consisting of nodes (q, t, Ind)
    fun constructRestrictedGraph() : ResultGraph

    fun constructResultGraph(spTable: AbstractMutableLoopTable<AbstractLoopTableEntry, LoopTableEntryRestriction> ) : ResultGraph
}