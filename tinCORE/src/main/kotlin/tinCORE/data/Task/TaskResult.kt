package tinCORE.data.Task


interface TaskResult {
    val id: Long
    val task: Task
    val source: String?
    val target: String?
    val sourceNode: String
    val targetNode: String
    val cost: Int?
}