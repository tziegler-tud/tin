package tin.services.ontology.loopTable.LoopTable.ELHI
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTable.LoopTableFragment
import tin.services.ontology.loopTable.LoopTable.SPALoopTableFragment
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import tin.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.ELHI.SPANumericLoopTableEntry

open class SPALoopTableFragmentCompressing(
    override val map: HashMap<SPANumericLoopTableEntry, Int>,
    val restrictionBuilder: NumericRestrictionBuilder

)
    : SPALoopTableFragment<SPANumericLoopTableEntry>() {

    constructor(restrictionBuilder: NumericRestrictionBuilder): this(HashMap(), restrictionBuilder);
    constructor(map: Map<SPANumericLoopTableEntry, Int>, restrictionBuilder: NumericRestrictionBuilder): this(HashMap(map), restrictionBuilder);
    override operator fun get(entry: SPANumericLoopTableEntry): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    fun getWithSourceAndTarget(source: Pair<Node, Node>, target: Pair<Node, Node>, limit: Int?) : SPALoopTableFragmentCompressing {
        return SPALoopTableFragmentCompressing(map.filter { it.key.source == source && it.key.target == target && if (limit == null) true else it.value < limit}, restrictionBuilder);
    }


    /**
     * returns an upper bound for the entry. If no other information is present, this can be seen as the actual value of the entry at the current state of the table
     */
    fun getClosestSubsetValue(entry: SPANumericLoopTableEntry) : Int? {
        val fragment = getWithSourceAndTarget(entry.source, entry.target, null);

        val restriction = entry.restriction;
        val size = restriction.getSize();

        //check if entry is present
        if(fragment.get(entry) !== null) return map[entry]!!;

        var targetSize = size;

        //gradually reduce size to find subsets in order of descending size
        for(i in 1 until size){
            val directSubsets = restrictionBuilder.getAllDirectSubsets(restriction)
            var minValue: Int? = null;
            val vals: MutableSet<Int> = mutableSetOf()
            for(subset in directSubsets){
                val subsetEntry = SPANumericLoopTableEntry(entry.source, entry.target, subset)
                val result = fragment.get(subsetEntry);
                if(result !== null) vals.add(result);
            }
            minValue = vals.minOrNull();
            if(minValue !== null) return minValue;
        }
        return null;
    }
}