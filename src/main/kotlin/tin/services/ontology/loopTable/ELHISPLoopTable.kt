package tin.services.ontology.loopTable
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableFragment.SPALoopTableFragment
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.sp.ELHISPLoopTableEntry

class ELHISPLoopTable(
    override val map: HashMap<ELHISPLoopTableEntry, Int>
)
    : LoopTable<ELHISPLoopTableEntry> {

    constructor(): this(HashMap());
    override fun get(entry: ELHISPLoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun set(entry: ELHISPLoopTableEntry, value: Int) {
        if(entry.hasEqualSourceAndTarget()) return;
        map[entry] = value;
    }

    override fun setIfLower(entry: ELHISPLoopTableEntry, value: Int) : Boolean {
        if(entry.hasEqualSourceAndTarget()) return false;
        if(map[entry] == null || value < map[entry]!!) {
            map[entry] = value
            return true;
        }
        return false
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that have value BELOW (< ) the given limit
     * If null is given, returns all entries.
     */
    fun getWithCostLimit(limit: Int?) : Map<ELHISPLoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPLoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}