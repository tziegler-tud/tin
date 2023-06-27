package tin.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.technical.internal.ComputationMode

class ComputationModeData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("computationMode") val computationModeEnum: ComputationMode.ComputationModeEnum,
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
) {
    constructor(model: ComputationMode) : this(
        id = model.id,
        computationModeEnum = model.computationModeEnum,
        computationProperties = ComputationPropertiesData(model.computationProperties),
    )
}