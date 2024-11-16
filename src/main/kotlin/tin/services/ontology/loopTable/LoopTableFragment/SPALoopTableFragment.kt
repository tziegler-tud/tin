package tin.services.ontology.loopTable.LoopTableFragment
import tin.services.ontology.loopTable.LoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

open class SPALoopTableFragment<entryType: SPALoopTableEntry>(
    override val map: HashMap<entryType, Int>,
)
    : LoopTableFragment<entryType> {

    constructor(): this(HashMap());
    constructor(map: Map<entryType, Int>): this(HashMap(map));
    override fun get(entry: entryType): Int? {
        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return null for +inf weights
        return map[entry];
    }

    override fun set(entry: entryType, value: Int) {
        map[entry] = value;
    }

    override fun setIfLower(entry: entryType, value: Int) : Boolean {
        if(entry.hasEqualSourceAndTarget()) return false;
        if(map[entry] == null || value < map[entry]!!) {
            map[entry] = value
            return true;
        }
        return false
    }
}