package tin.services.ontology.loopTable.LoopTable.ELHI
import tin.services.ontology.loopTable.LoopTable.IndividualLoopTable
import tin.services.ontology.loopTable.LoopTable.LoopTable
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ELHISPLoopTable(
    override val map: HashMap<IndividualLoopTableEntry, Int>
)
    : IndividualLoopTable(map) {

    constructor(): this(HashMap());

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}