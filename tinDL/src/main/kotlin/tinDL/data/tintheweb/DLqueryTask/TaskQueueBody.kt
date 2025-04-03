package tinDL.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v2.Tasks.TransducerMode

class TaskQueueBody(
    @JsonProperty("task") val taskId: Long
)