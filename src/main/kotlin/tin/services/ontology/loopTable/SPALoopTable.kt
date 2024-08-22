package tin.services.ontology.loopTable
import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SPALoopTable: LoopTable<SPALoopTableEntry> {

    override val map: HashMap<SPALoopTableEntry, Long> = HashMap();

    override fun get(entry: SPALoopTableEntry): Long? {

        //[p,p,M] = 0
        if(entry.hasEqualSourceAndTarget()){
            return 0;
        }
        // return undefined for +inf weights
        return map[entry];
    }
    override fun set(entry: SPALoopTableEntry, value: Long) {
        map[entry] = value;
    }
}