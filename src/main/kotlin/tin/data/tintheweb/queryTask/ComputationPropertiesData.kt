package tin.data.tintheweb.queryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.queryTask.ComputationProperties

class ComputationPropertiesData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("computationModeEnum") val computationModeEnum: ComputationProperties.ComputationModeEnum,
    @JsonProperty("topKValue") val topKValue: Int?,
    @JsonProperty("thresholdValue") val thresholdValue: Double?,
    @JsonProperty("generateTransducer") val generateTransducer: Boolean,
    @JsonProperty("transducerGeneration") val transducerGeneration: ComputationProperties.TransducerGeneration?,
) {
    constructor(model: ComputationProperties): this(
        id = model.id,
        name = model.name,
        computationModeEnum = model.computationModeEnum,
        topKValue = model.topKValue,
        thresholdValue = model.thresholdValue,
        generateTransducer = model.generateTransducer,
        transducerGeneration = model.transducerGeneration,
    )
}