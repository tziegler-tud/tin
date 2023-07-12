package tin.data.tintheweb.queryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.queryTask.ComputationMode
import tin.model.queryTask.ComputationProperties

class ComputationModeData(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("computationModeEnum") val computationModeEnum: ComputationMode.ComputationModeEnum,
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
) {
    constructor(model: ComputationMode) : this(
        id = model.id,
        computationModeEnum = model.computationModeEnum,
        computationProperties = ComputationPropertiesData(model.computationProperties),
    )
}