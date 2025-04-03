package tinDL.services.ontology.loopTable.LoopTable.ELHI
import tinDL.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

class ELHISPALoopTable(
    override val map: HashMap<ELHISPALoopTableEntry, Int>
)
    : AbstractMutableLoopTable<ELHISPALoopTableEntry, MultiClassLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun get(entry: ELHISPALoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}