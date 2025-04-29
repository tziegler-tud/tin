package tinLIB.services.ontology.ResultGraph

class ResultGraphBuilderStats(
    val nodes: Int,
    val edges: Int,
    val maxEdgeCost: Int,
    val minEdgeCost: Int,
    val unreachableNodesAmount: Int
)