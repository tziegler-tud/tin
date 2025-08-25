package tinCORE.data.tintheweb.DlQueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinCORE.data.Task.ComputationMode
import tinCORE.data.Task.DlTask.DlComputationMode
import tinCORE.data.Task.DlTask.OntologyVariant
import tinCORE.data.Task.TransducerGenerationMode
import tinCORE.data.Task.TransducerMode

class DlTaskConfigurationData(
    @JsonProperty("query") val queryFileIdentifier: Long,
    @JsonProperty("ontology") val ontologyFileIdentifier: Long,
    @JsonProperty("transducerMode") val transducerMode: TransducerMode,
    @JsonProperty("transducerGenerationMode") val transducerGenerationMode: TransducerGenerationMode?,
    @JsonProperty("transducerFile") val transducerFileIdentifier: Long?,
    @JsonProperty("variant") val ontologyVariant: OntologyVariant,
    @JsonProperty("computationMode") val computationMode: DlComputationMode,
    @JsonProperty("sourceIndividual") val sourceIndividual: String?,
    @JsonProperty("targetIndividual") val targetIndividual: String?,
    @JsonProperty("maxCost") val maxCost: Int?,
    @JsonProperty("queue") val addToQueue: Boolean = false,
    )

