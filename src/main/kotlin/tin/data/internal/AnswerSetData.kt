package tin.data.internal

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class AnswerSetData @JsonCreator constructor(
    @JsonProperty("answerMap") val answerMap: HashMap<Pair<String, String>, Double>
)