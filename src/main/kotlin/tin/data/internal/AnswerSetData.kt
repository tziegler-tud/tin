package tin.data.internal

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.utils.PairOfStrings

data class AnswerSetData @JsonCreator constructor(
    @JsonProperty("answerMap") val answerMap: HashMap<PairOfStrings, Double>
)