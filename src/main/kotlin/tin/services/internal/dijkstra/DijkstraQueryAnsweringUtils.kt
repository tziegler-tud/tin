package tin.services.internal.dijkstra

import org.springframework.stereotype.Service
import tin.model.queryResult.RegularPathQueryResult
import tin.model.utils.ProductAutomatonTuple

@Service
class DijkstraQueryAnsweringUtils {

    /**
     * transforms internal answerMap containing a (source, target) ProductAutomatonTuple as key, and a Double as value (cost of reaching target from source)
     * into a set of AnswerTriplets (source.name, target.name, double); omitting the technical ProductAutomatonNodes
     * after finishing the query we do not care about technical details, we simply want the (human-readable) results.
     */
    fun makeAnswerMapReadable(
        answerMap: HashMap<ProductAutomatonTuple, Double>
    ): Set<RegularPathQueryResult.AnswerTriplet> {
        return HashSet<RegularPathQueryResult.AnswerTriplet>().apply {
            answerMap.forEach { (key, value) ->
                val source = key.sourceProductAutomatonNode!!.identifier.third.identifier
                val target = key.targetProductAutomatonNode.identifier.third.identifier
                add(RegularPathQueryResult.AnswerTriplet(source, target, value))
            }
        }
    }
}