package tinDL.services.ontology.loopTable.LoopTable.ELHI
import tinDL.services.ontology.loopTable.LoopTable.IndividualLoopTable
import tinDL.services.ontology.loopTable.LoopTable.LoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ELHISPLoopTable(
    override val map: HashMap<IndividualLoopTableEntry, Int>
)
    : IndividualLoopTable(map) {

    constructor(): this(HashMap());

    override fun get(entry: IndividualLoopTableEntry): Int? {
        return map[entry];
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}