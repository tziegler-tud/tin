//package tin.services.ontology.loopTable.loopTableEntry
//
//import tin.model.v2.graph.Node
//import tin.services.ontology.loopTable.LoopTableEntryRestriction.AboxIndividualRestriction
//
//class SPLoopTableEntry(
//    override val source: Pair<Node, Node>,
//    override val target: Pair<Node, Node>,
//    override val restriction: AboxIndividualRestriction,
//) : LoopTableEntry {
//
//    override fun hasEqualSourceAndTarget(): Boolean {
//        return (source.first == target.first && source.second == target.second)
//    }
//}