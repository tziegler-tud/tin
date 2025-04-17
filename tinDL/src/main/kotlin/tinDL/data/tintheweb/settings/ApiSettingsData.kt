package tinDL.data.tintheweb.settings

import com.fasterxml.jackson.annotation.JsonProperty


class ApiSettingsData(
    @JsonProperty("autoQueue") val autoQueue: Boolean?,
    )
{}

