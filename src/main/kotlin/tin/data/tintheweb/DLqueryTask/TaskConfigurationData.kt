package tin.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.v2.Tasks.OntologyVariant

class TaskConfigurationData(
    @JsonProperty("query") val queryFileIdentifier: Long?,
    @JsonProperty("transducer") val transducerFileIdentifier: Long?,
    @JsonProperty("ontology") val ontologyFileIdentifier: Long?,
    @JsonProperty("variant") val ontologyVariant: OntologyVariant,
    )

