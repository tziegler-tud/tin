package tin.services.ontology.loopTable.LoopTableFragment
import tin.services.ontology.loopTable.LoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SPALoopTableFragment(
    override val map: HashMap<SPALoopTableEntry, Int>,
    val restriction: ConceptNameRestriction,
)
    : LoopTable<SPALoopTableEntry> {

    constructor(): this(HashMap(), ConceptNameRestriction());
    constructor(map: Map<SPALoopTableEntry, Int>, restriction: ConceptNameRestriction): this(HashMap(map), restriction);
    constructor(map: Map<SPALoopTableEntry, Int>): this(HashMap(map), ConceptNameRestriction());
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
}