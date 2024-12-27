package tin.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity
import tin.model.v1.queryTask.ComputationProperties
import tin.model.v2.Tasks.OntologyVariant
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration
import tin.services.Task.Task
import tin.services.Task.TaskStatus

class TaskConfigurationData(
    @JsonProperty("query") val queryFileIdentifier: Long?,
    @JsonProperty("transducer") val transducerFileIdentifier: Long?,
    @JsonProperty("ontology") val ontologyFileIdentifier: Long?,
    @JsonProperty("variant") val ontologyVariant: OntologyVariant,
    )

