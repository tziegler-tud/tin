import com.fasterxml.jackson.annotation.JsonProperty

class OnotlogyTestPostData(
    @JsonProperty("filename") val filename: String,
    @JsonProperty("reasoner") val reasonerName: String,
)