package tin.services.ontology.loopTable
import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SPALoopTable: LoopTable<SPALoopTableEntry> {

    override val map: HashMap<SPALoopTableEntry, Int> = HashMap();

    override fun get(entry: SPALoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun set(entry: SPALoopTableEntry, value: Int) {
        map[entry] = value;
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that have value BELOW (< ) the given limit
     * If null is given, returns all entries.
     */
    fun getWithCostLimit(limit: Int?) : Map<SPALoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }
}