package tin.services.ontology.loopTable
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableFilter.LoopTableFilter
import tin.services.ontology.loopTable.LoopTableFragment.SPALoopTableFragment
import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SPALoopTable(
    override val map: HashMap<SPALoopTableEntry, Int>
)
    : LoopTable<SPALoopTableEntry> {

    constructor(): this(HashMap());
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

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    fun getWithRestriction(restriction: ConceptNameRestriction, limit: Int? = null) : SPALoopTableFragment {
        return SPALoopTableFragment(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit}, restriction);
    }

    fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: ConceptNameRestriction) : SPALoopTableFragment {
        return SPALoopTableFragment(map.filter { it.key.restriction == restriction && it.key.source == source }, restriction);
    }
}