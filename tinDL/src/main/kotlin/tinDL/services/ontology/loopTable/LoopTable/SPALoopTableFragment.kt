package tinDL.services.ontology.loopTable.LoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry

open class SPALoopTableFragment<entryType: tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry>(
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