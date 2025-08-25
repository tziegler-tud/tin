package tinCORE.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty

data class DbTaskData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("query") val queryFileName: String,
    @JsonProperty("transducer") val transducerFileName: String , //only custom type; simple enum
    @JsonProperty("database") val databaseFileName: String
)
