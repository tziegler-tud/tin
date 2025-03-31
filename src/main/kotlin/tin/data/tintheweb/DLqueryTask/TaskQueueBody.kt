package tin.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.v2.Tasks.TransducerMode

class TaskQueueBody(
    @JsonProperty("task") val taskId: Long
)