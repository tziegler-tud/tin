package tin.services.ontology.loopTable.LoopTable.ELHI
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTable.AbstractMutableLoopTable
import tin.services.ontology.loopTable.LoopTable.SPALoopTableFragment
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.ELHI.SPANumericLoopTableEntry

class ELHISPALoopTableCompressing(
    override val map: HashMap<SPANumericLoopTableEntry, Int>,
    val restrictionBuilder: NumericRestrictionBuilder
)
    : AbstractMutableLoopTable<SPANumericLoopTableEntry, NumericConceptNameRestriction>(map) {

    constructor(restrictionBuilder: NumericRestrictionBuilder): this(HashMap(), restrictionBuilder);

    override fun equals(other: Any?) : Boolean {
        if(other !is ELHISPALoopTableCompressing) return false;
        return map == other.map;
    }

    override fun hashCode() : Int {
        return map.hashCode()
    }

    override fun set(entry: SPANumericLoopTableEntry, value: Int) {
        if(entry.hasEqualSourceAndTarget()) return;
        val upper = getClosestSubsetValue(entry);
        if(upper == null || value < upper) map[entry] = value;
    }

    override fun get(entry: SPANumericLoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return getClosestSubsetValue(entry);
    }

    override fun setIfLower(entry: SPANumericLoopTableEntry, value: Int) : Boolean {
        if(entry.hasEqualSourceAndTarget()) return false;
        val upper = getClosestSubsetValue(entry);
        if(upper == null || value < upper) {
            map[entry] = value
            return true;
        }
        return false
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that have value BELOW (< ) the given limit
     * If null is given, returns all entries.
     */
    override fun getWithCostLimit(limit: Int?) : Map<SPANumericLoopTableEntry, Int> {
        if (limit == null) {return map}
        return map.filterValues { it < limit}
    }

    /**
     * returns a HashMap <spaLoopTableEntry, Int> containing all entries that use the given restriction.
     * If limit is given, only return entries with value BELOW (< ) the given limit.
     */
    override fun getWithRestriction(restriction: NumericConceptNameRestriction, limit: Int?) : SPALoopTableFragmentCompressing {
        return SPALoopTableFragmentCompressing(map.filter{it.key.restriction == restriction && if (limit == null) true else it.value < limit}, restrictionBuilder);
    }

    override fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int?) : SPALoopTableFragmentCompressing {
        return SPALoopTableFragmentCompressing(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit}, restrictionBuilder);
    }

    override fun getWithSourceAndRestriction(source: Pair<Node, Node>, restriction: NumericConceptNameRestriction) : SPALoopTableFragmentCompressing {
        return SPALoopTableFragmentCompressing(map.filter { it.key.restriction == restriction && it.key.source == source }, restrictionBuilder);
    }

    /**
     * returns an upper bound for the entry. If no other information is present, this can be seen as the actual value of the entry at the current state of the table
     */
    fun getClosestSubsetValue(entry: SPANumericLoopTableEntry) : Int? {
//        val fragment = getWithSourceAndTarget(entry.source, entry.target);

        val restriction = entry.restriction;
        val size = restriction.getSize();

        if( size==0 ) return null;

        //check if entry is present
        if(map[entry] !== null) return map[entry]!!;

        var targetSize = size;

        //gradually reduce size to find subsets in order of descending size
        val directSubsets = restrictionBuilder.getAllDirectSubsets(restriction)
        var minValue: Int? = null;
        val vals: MutableSet<Int> = mutableSetOf()
        for(subset in directSubsets){
            val subsetEntry = SPANumericLoopTableEntry(entry.source, entry.target, subset)
            val result = map[subsetEntry]
            if(result !== null) vals.add(result);
        }
        minValue = vals.minOrNull();
        if(minValue !== null) {
            return minValue
        };
        else {
            for(subset in directSubsets){
                val subsetEntry = SPANumericLoopTableEntry(entry.source, entry.target, subset)
                val value = getClosestSubsetValue(subsetEntry);
                if(value != null) vals.add(value);
            }
        }
        minValue = vals.minOrNull();
        if(minValue !== null) {
            return minValue
        };
        return null;
    }
}