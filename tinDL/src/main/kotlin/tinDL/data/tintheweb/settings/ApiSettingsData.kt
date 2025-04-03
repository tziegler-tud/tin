package tinDL.data.tintheweb.settings

import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v1.queryTask.ComputationProperties
import tinDL.model.v2.Tasks.ComputationMode
import tinDL.model.v2.Tasks.OntologyVariant

class ApiSettingsData(
    @JsonProperty("autoQueue") val autoQueue: Boolean?,
    )
{}

