package tin.data.tintheweb.settings

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.v1.queryTask.ComputationProperties
import tin.model.v2.Tasks.ComputationMode
import tin.model.v2.Tasks.OntologyVariant

class ApiSettingsData(
    @JsonProperty("autoQueue") val autoQueue: Boolean?,
    )
{}

