package tinCORE.data.Task

interface TaskComputationConfiguration<T: ComputationMode> {
    val computationMode: T
    val individualNameA: String?
    val individualNameB: String?
    val maxCost: Int?
}