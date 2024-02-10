package tin.services.internal.utils

import tin.model.alphabet.Alphabet
import tin.model.transducer.TransducerGraph
import tin.model.transducer.TransducerNode
import java.util.HashMap

class TransducerFactory {

    companion object {
        fun generateClassicAnswersTransducer(alphabet: Alphabet): TransducerGraph {

            val transducerGraph = TransducerGraph()
            val source = TransducerNode("t0", isInitialState = true, isFinalState = true)

            for (word in alphabet.getAlphabet()) {
                // for each word of the alphabet we add the edge (t0, t0, word, word, 0)
                transducerGraph.addEdge(source, source, word, word, 0.0)
            }

            return transducerGraph
        }

        fun generateEditDistanceTransducer(joinedAlphabet: Alphabet): TransducerGraph {
            return TODO()
        }

        fun generateEditDistanceTransducer(queryAlphabet: Alphabet, databaseAlphabet: Alphabet): TransducerGraph {
            val t = TransducerGraph();
            val node = TransducerNode("t0", true, true)
            t.addNodes(node)
            //generate edit distance between all property names in query and database
            //as we assume some similarities between the alphabets, we cache our results locally and try to reuse them

            val queryConcepts = queryAlphabet.getConceptNames();
            val queryRoles = queryAlphabet.getRoleNames();
            val dbConcepts = databaseAlphabet.getConceptNames();
            val dbRoles = databaseAlphabet.getRoleNames();


            var conceptCache = hashMapOf<Pair<String, String>, Int>();
            var roleCache = hashMapOf<Pair<String, String>, Int>();

            //create m:n table
            for (concept in queryConcepts) {
                for (databaseProperty in dbConcepts) {
                    var dist = conceptCache[Pair(concept, databaseProperty)]
                    if (dist === null) {
                        //calculate edit distance between concept names
                        dist = calculateEditDistance(concept, databaseProperty);
                        //add to cache and mirror.
                        conceptCache[Pair(concept, databaseProperty)] = dist;
                        conceptCache[Pair(databaseProperty, concept)] = dist;
                    }
                    //add transducer edge
                    t.addEdge(node, node, concept, databaseProperty, dist.toDouble())
                }
            }

            for (queryRole in queryRoles) {
                for (databaseRole in dbRoles) {
                    var dist = roleCache[Pair(queryRole, databaseRole)]
                    if (dist === null) {
                        //calculate edit distance between concept names
                        dist = calculateEditDistance(queryRole, databaseRole);
                        //add to cache and mirror.
                        roleCache[Pair(queryRole, databaseRole)] = dist;
                        roleCache[Pair(databaseRole, queryRole)] = dist;
                    }
                    //add transducer edge
                    t.addEdge(node, node, queryRole, databaseRole, dist.toDouble())
                }
            }

            return t;
        };

        private fun calculateEditDistance(lhs: String, rhs: String): Int {
            //implements wagner-fischer algorithm
            if(lhs == rhs) { return 0 }
            if(lhs.isEmpty()) { return rhs.length }
            if(rhs.isEmpty()) { return lhs.length }

            val lhsLength = lhs.length + 1
            val rhsLength = rhs.length + 1

            //initialize cost array with [0,1,2,..,lhsLength]
            var cost = Array(lhsLength) { it }
            var newCost = Array(lhsLength) { 0 }

            for (i in 1 until rhsLength) {
                newCost[0] = i

                for (j in 1 until lhsLength) {
                    val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1

                    newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
                }

                val swap = cost
                cost = newCost
                newCost = swap
            }
            return cost[lhsLength - 1]
        }
    }


}