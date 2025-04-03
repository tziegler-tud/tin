package tinDL.services.ontology.loopTable.LoopTable

interface LoopTable<entryType> {
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
}