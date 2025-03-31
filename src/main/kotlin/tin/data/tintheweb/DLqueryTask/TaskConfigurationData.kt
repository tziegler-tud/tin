package tin.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.v1.queryTask.ComputationProperties
import tin.model.v2.Tasks.ComputationMode
import tin.model.v2.Tasks.OntologyVariant
import tin.model.v2.Tasks.TransducerGenerationMode
import tin.model.v2.Tasks.TransducerMode

class TaskConfigurationData(
    @JsonProperty("query") val queryFileIdentifier: Long,
    @JsonProperty("ontology") val ontologyFileIdentifier: Long,
    @JsonProperty("transducerMode") val transducerMode: TransducerMode,
    @JsonProperty("transducerGenerationMode") val transducerGenerationMode: TransducerGenerationMode?,
    @JsonProperty("transducerFile") val transducerFileIdentifier: Long?,
    @JsonProperty("variant") val ontologyVariant: OntologyVariant,
    @JsonProperty("computationMode") val computationMode: ComputationMode,
    @JsonProperty("sourceIndividual") val sourceIndividual: String?,
    @JsonProperty("targetIndividual") val targetIndividual: String?,
    @JsonProperty("maxCost") val maxCost: Int?,
    @JsonProperty("queue") val addToQueue: Boolean = false,
    )

