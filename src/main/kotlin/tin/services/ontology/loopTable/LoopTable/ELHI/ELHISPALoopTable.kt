package tin.services.ontology.loopTable.LoopTable.ELHI
import tin.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

class ELHISPALoopTable(
    override val map: HashMap<ELHISPALoopTableEntry, Int>
)
    : AbstractMutableLoopTable<ELHISPALoopTableEntry, MultiClassLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}