package tin.services.ontology.loopTable
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableFragment.SPALoopTableFragment
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class ELHISPALoopTable(
    override val map: HashMap<ELHISPALoopTableEntry, Int>
)
    : LoopTable<ELHISPALoopTableEntry> {

    constructor(): this(HashMap());
    override fun get(entry: ELHISPALoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun set(entry: ELHISPALoopTableEntry, value: Int) {
        if(entry.hasEqualSourceAndTarget()) return;
        map[entry] = value;
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that have value BELOW (< ) the given limit
     * If null is given, returns all entries.
     */
    fun getWithCostLimit(limit: Int?) : Map<ELHISPALoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    fun getWithRestriction(restriction: ConceptNameRestriction, limit: Int? = null) : SPALoopTableFragment<ELHISPALoopTableEntry> {
        return SPALoopTableFragment(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int? = null) : SPALoopTableFragment<ELHISPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: ConceptNameRestriction) : SPALoopTableFragment<ELHISPALoopTableEntry> {
        return SPALoopTableFragment(map.filter { it.key.restriction == restriction && it.key.source == source });
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPALoopTable) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}