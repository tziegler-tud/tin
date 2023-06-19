package tin.data

import tin.data.input.QueryData

class CombinedQueryData(
    val computationMode: ComputationMode,
    val computationProperties: ComputationPropertiesData,
    val queryData: QueryData,
)

enum class ComputationMode {
    Dijkstra,
    TopK,
    Threshold,
}

class ComputationPropertiesData(
    val topK: Int?,
    val threshold: Double?,
)