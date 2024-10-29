package tin.services.ontology.loopTable
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableFragment.SPALoopTableFragment
import tin.services.ontology.loopTable.loopTableEntry.ELSPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class ELSPALoopTable(
    override val map: HashMap<ELSPALoopTableEntry, Int>
)
    : LoopTable<ELSPALoopTableEntry> {

    constructor(): this(HashMap());
    override fun get(entry: ELSPALoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun set(entry: ELSPALoopTableEntry, value: Int) {
        if(entry.hasEqualSourceAndTarget()) return;
        map[entry] = value;
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that have value BELOW (< ) the given limit
     * If null is given, returns all entries.
     */
    fun getWithCostLimit(limit: Int?) : Map<ELSPALoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    fun getWithRestriction(restriction: SingleClassLoopTableEntryRestriction, limit: Int? = null) : SPALoopTableFragment<ELSPALoopTableEntry> {
        return SPALoopTableFragment(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int? = null) : SPALoopTableFragment<ELSPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: SingleClassLoopTableEntryRestriction) : SPALoopTableFragment<ELSPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.restriction == restriction && it.key.source == source });
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELSPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}