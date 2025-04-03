package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry

interface ResultGraphBuilder {
    //construct GuA -> product graph consisting of nodes (q, t, Ind)
    fun constructRestrictedGraph() : ResultGraph

    fun constructResultGraph(spTable: AbstractMutableLoopTable<tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry, LoopTableEntryRestriction> ) : ResultGraph
}