package tin.services.ontology.loopTable.LoopTable.ELH
import tin.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry

class ELSPLoopTable(
    override val map: HashMap<ELSPLoopTableEntry, Int>
)
    : AbstractMutableLoopTable<ELSPLoopTableEntry, SingleClassLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun equals(other: Any?) : Boolean {
        if(other !is ELSPLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}