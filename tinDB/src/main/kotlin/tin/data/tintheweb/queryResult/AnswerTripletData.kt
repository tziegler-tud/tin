package tin.data.tintheweb.queryResult

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.queryResult.RegularPathQueryResult

data class AnswerTripletData @JsonCreator constructor(
    @JsonProperty("source") val source: String,
    @JsonProperty("target") val target: String,
    @JsonProperty("cost") val cost: Double,
) {
    constructor(model: RegularPathQueryResult.AnswerTriplet): this(
        source = model.source,
        target = model.target,
        cost = model.cost
    )
}
