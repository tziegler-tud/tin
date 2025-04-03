package tin.services.ontology.loopTable.LoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

abstract class IndividualLoopTable(
    override val map: HashMap<IndividualLoopTableEntry, Int>
)
    : AbstractMutableLoopTable<IndividualLoopTableEntry, IndividualLoopTableEntryRestriction>(map) {

    constructor(): this(HashMap());

    override fun equals(other: Any?) : Boolean {
        if(other !is IndividualLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}