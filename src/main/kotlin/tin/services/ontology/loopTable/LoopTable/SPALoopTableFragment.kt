package tin.services.ontology.loopTable.LoopTable
import tin.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry

open class SPALoopTableFragment<entryType: AbstractLoopTableEntry>(
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
}