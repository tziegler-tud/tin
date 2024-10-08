package tin.services.ontology.loopTable

import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

interface LoopTable<entryType: LoopTableEntry> {
    /*
    loop tables have the following properties:
     [(s,t),(s',t'), M]: Long for spa, M is a set of concept names
     [(s,t),(s',t'), a]: Long for sp. a is an A-Box individual

     We need:
     - efficient storage for tuples (s,t) (queryState x transducerState)
     - efficient querying of entries. All 3 keys are required for each lookup
     */

    val map: Map<entryType, Int>

    fun get(entry: entryType): Int?

    fun set(entry: entryType, value: Int)

}