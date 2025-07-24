package tinCORE.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinCORE.data.Task.ComputationMode
import tinCORE.data.Task.DlTask.OntologyVariant
import tinCORE.data.Task.TransducerGenerationMode
import tinCORE.data.Task.TransducerMode

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

