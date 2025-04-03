package tinDL.services.ontology.loopTable.LoopTable
import tinDL.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry

abstract class AbstractMutableLoopTable<T: tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry, in R: LoopTableEntryRestriction>(
    override val map: HashMap<T, Int>
)
    : MutableLoopTable<T> {

    constructor(): this(HashMap());

    fun getSize(): Int {
        return map.size;
    }

    override fun set(entry: T, value: Int) {
        if(entry.hasEqualSourceAndTarget()) return;
        map[entry] = value;
    }

    override fun setIfLower(entry: T, value: Int) : Boolean {
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
    fun getWithCostLimit(limit: Int?) : Map<T, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    fun getWithRestriction(restriction: R, limit: Int? = null) : SPALoopTableFragment<T> {
        return SPALoopTableFragment(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int? = null) : SPALoopTableFragment<T> {
        return SPALoopTableFragment(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit});
    }

    fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: R) : SPALoopTableFragment<T> {
        return SPALoopTableFragment(map.filter { it.key.restriction == restriction && it.key.source == source });
    }

    override fun equals(other: Any?) : Boolean {
        if(other !is AbstractMutableLoopTable<*,*>) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }
}