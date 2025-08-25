package tinCORE.data.tintheweb.DlQueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinCORE.data.Task.ComputationMode
import tinCORE.data.Task.DbTask.DbComputationMode
import tinCORE.data.Task.DlTask.OntologyVariant
import tinCORE.data.Task.TransducerGenerationMode
import tinCORE.data.Task.TransducerMode

class DbTaskConfigurationData(
    @JsonProperty("query") val queryFileIdentifier: Long,
    @JsonProperty("database") val databaseFileIdentifier: Long,
    @JsonProperty("transducerMode") val transducerMode: TransducerMode,
    @JsonProperty("transducerGenerationMode") val transducerGenerationMode: TransducerGenerationMode?,
    @JsonProperty("transducerFile") val transducerFileIdentifier: Long?,
    @JsonProperty("computationMode") val computationMode: DbComputationMode,
    @JsonProperty("sourceIndividual") val sourceIndividual: String?,
    @JsonProperty("targetIndividual") val targetIndividual: String?,
    @JsonProperty("maxCost") val maxCost: Int?,
    @JsonProperty("queue") val addToQueue: Boolean = false,
    )

