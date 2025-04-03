package tinDL.services.ontology.loopTable.LoopTable.ELH
import tinDL.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

class ELSPALoopTable(
    override val map: HashMap<ELSPALoopTableEntry, Int>
)
    : AbstractMutableLoopTable<ELSPALoopTableEntry, SingleClassLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun get(entry: ELSPALoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELSPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}