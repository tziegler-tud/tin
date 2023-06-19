package tin.utils

import java.util.*

// returns true if two edge sets are identical, else false
fun compareEdgeSets(thisEdgeList: LinkedList<*>, otherEdgeList: LinkedList<*>): Boolean {
    var pairsFound = 0
    for (thisEdge in thisEdgeList) {
        for (otherEdge in otherEdgeList) {
            if (thisEdge == otherEdge) {
                pairsFound++
                break
            }
        }
    }
    return pairsFound == thisEdgeList.size
}

fun compareNodeSets(thisNodeSet: Set<*>, otherNodeSet: Set<*>): Boolean {
    var pairsFound = 0
    for (thisNode in thisNodeSet) {
        for (otherNode in otherNodeSet) {
            if (thisNode != null) {
                if (thisNode == otherNode) {
                    pairsFound++
                    break
                }
            }
        }
    }
    return pairsFound == thisNodeSet.size
}
