package tinCORE.data.Task.DbTask

import tinCORE.data.Task.TaskComputationConfiguration

class DbTaskComputationConfiguration(
    override val computationMode: DbComputationMode,
    override val individualNameA: String? = null,
    override val individualNameB: String? = null,
    override val maxCost: Int?  = null,
    val topKValue: Int? = null,
) : TaskComputationConfiguration<DbComputationMode>{
}