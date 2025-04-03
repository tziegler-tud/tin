package tinDL.services.ontology.loopTable.LoopTable.ELH
import tinDL.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ELSPLoopTable(
    override val map: HashMap<ELSPLoopTableEntry, Int>
)
    : AbstractMutableLoopTable<ELSPLoopTableEntry, SingleClassLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun get(entry: ELSPLoopTableEntry): Int? {
        return map[entry];
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELSPLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}