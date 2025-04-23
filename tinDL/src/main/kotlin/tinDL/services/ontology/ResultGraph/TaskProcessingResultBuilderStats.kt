package tinDL.services.ontology.ResultGraph

class TaskProcessingResultBuilderStats(
    val nodes: Int,
    val edges: Int,
    val maxEdgeCost: Int,
    val minEdgeCost: Int,
    val unreachableNodesAmount: Int
)