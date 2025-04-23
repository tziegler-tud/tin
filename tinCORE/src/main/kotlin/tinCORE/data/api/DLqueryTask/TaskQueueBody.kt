package tinCORE.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty

class TaskQueueBody(
    @JsonProperty("task") val taskId: Long
)