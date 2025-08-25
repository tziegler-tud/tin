package tinCORE.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty

class DlTaskQueueBody(
    @JsonProperty("task") val taskId: Long
)