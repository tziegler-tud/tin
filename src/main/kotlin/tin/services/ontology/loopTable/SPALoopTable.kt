package tin.services.ontology.loopTable
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableFragment.SPALoopTableFragment
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

open class SPALoopTable(
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
        if(entry.hasEqualSourceAndTarget()) return;
        map[entry] = value;
    }

    override fun setIfLower(entry: SPALoopTableEntry, value: Int) : Boolean {
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
    fun getWithCostLimit(limit: Int?) : Map<SPALoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    fun getWithRestriction(restriction: ConceptNameRestriction, limit: Int? = null) : SPALoopTableFragment<SPALoopTableEntry> {
        return SPALoopTableFragment(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int? = null) : SPALoopTableFragment<SPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: ConceptNameRestriction) : SPALoopTableFragment<SPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.restriction == restriction && it.key.source == source });
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is SPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}