package tinCORE.data.Task

interface TaskComputationConfiguration {
    val computationMode: ComputationMode
    val individualNameA: String?
    val individualNameB: String?
    val maxCost: Int?
}