package tinLIB.services.internal.utils

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerGraph
import kotlin.random.Random

class TransducerFactory {

    companion object {
        fun generateClassicAnswersTransducer(alphabet: Alphabet): TransducerGraph {

            val transducerGraph = TransducerGraph()
            val source = Node("t0", isInitialState = true, isFinalState = true)

            for (word in alphabet.getAlphabet()) {
                // for each word of the alphabet we add the edge (t0, t0, word, word, 0)
                transducerGraph.addEdge(source, source, word, word, 0)
            }

            return transducerGraph
        }

        fun generateEditDistanceTransducer(joinedAlphabet: Alphabet): TransducerGraph {
            return generateEditDistanceTransducer(joinedAlphabet, joinedAlphabet,)
        }

        fun generateEditDistanceTransducer(
            queryAlphabet: Alphabet,
            dataAlphabet: Alphabet,
            allowRoleToConcept: Boolean = false,
            allowConceptToRole: Boolean = false,
            allowInverseRoles: Boolean = false,
            useSimpleWeights: Boolean = false,
        ): TransducerGraph {

            val allowRoleToConceptAssertion =
                allowRoleToConcept; //adds edges that transform query roles names to concept assertions (PA construction step 11 + 12)
            val allowConceptAssertionToRole =
                allowConceptToRole; //adds edges that transform query concept assertions to role names (PA construction step 14 + 15)

            val t = TransducerGraph();
            val node = Node("t0", true, true)
            t.addNodes(node)
            //generate edit distance between all property names in query and database
            //as we assume some similarities between the alphabets, we cache our results locally and try to reuse them

            val queryConcepts = queryAlphabet.getConceptNames();
            val queryRoles = queryAlphabet.getRoleNames();
            val dataConcepts = dataAlphabet.getConceptNames();
            val dataRoles = dataAlphabet.getRoleNames();

            val simpleDist = 1

            var cache = hashMapOf<Pair<String, String>, Int>();

            /**
             * return cached value if present, else calculate and add to cache.
             */
            fun cacheOrCredit(a: String, b: String): Int {
                if(useSimpleWeights) return simpleDist
                var dist = cache[Pair(a, b)]
                if (dist === null) {
                    //calculate edit distance between concept names
                    dist = calculateEditDistance(a, b);
                    //add to cache and mirror.
                    cache[Pair(a, b)] = dist;
                    cache[Pair(b, a)] = dist;
                }
                return dist;
            };

            var i = 0;
            for (concept in queryConcepts) {
                i++;
                for (databaseProperty in dataConcepts) {
                    var dist = cacheOrCredit(concept, databaseProperty);
                    //add transducer edge
                    t.addEdge(node, node, concept + "?", databaseProperty + "?", dist)
                }
            }

            var j = 0;
            for (queryRole in queryRoles) {
                j++;
                for (databaseRole in dataRoles) {
                    var dist = cacheOrCredit(queryRole, databaseRole)

                    val isInverse = allowInverseRoles && Random.nextBoolean()
//                        val isInverse = false
                    var label = databaseRole
                    if (isInverse) label = "inverse($databaseRole)"
                    //add transducer edge
                    t.addEdge(node, node, queryRole, databaseRole, dist)
                    t.addEdge(node, node, "inverse($queryRole)", "inverse($databaseRole)", dist)
                }

                if (allowRoleToConceptAssertion) {
                    for (conceptAssertion in queryAlphabet.getTransformedConceptNames()) {
                        var dist = cacheOrCredit(queryRole, conceptAssertion);
                        //add transducer edge
                        t.addEdge(node, node, queryRole, conceptAssertion + "?", dist)
                    }
                }
            }

            if (allowConceptAssertionToRole) {
                for (conceptAssertion in queryAlphabet.getTransformedConceptNames()) {
                    for (databaseRole in dataRoles) {
                        val dist = cacheOrCredit(conceptAssertion, databaseRole)
                        //add transducer edge
                        t.addEdge(node, node, conceptAssertion + "?", databaseRole, dist)
                    }
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