package tinCORE.data.Task.DlTask

import jdk.jfr.Threshold
import tinCORE.data.Task.TaskComputationConfiguration

class DlTaskComputationConfiguration  (
    val ontologyVariant: OntologyVariant,
    override val computationMode: DlComputationMode,
    override val individualNameA: String? = null,
    override val individualNameB: String? = null,
    override val maxCost: Int?  = null,
) : TaskComputationConfiguration<DlComputationMode>