package tin.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.technical.internal.ComputationProperties

class ComputationPropertiesData(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("topKValue") val topKValue: Int?,
    @JsonProperty("thresholdValue") val thresholdValue: Double?,
    @JsonProperty("generateTransducer") val generateTransducer: Boolean,
    @JsonProperty("transducerGeneration") val transducerGeneration: ComputationProperties.TransducerGeneration?,
) {
    constructor(model: ComputationProperties): this(
        id = model.id,
        topKValue = model.topKValue,
        thresholdValue = model.thresholdValue,
        generateTransducer = model.generateTransducer,
        transducerGeneration = model.transducerGeneration,
    )
}