package tinCORE.data.Task.DbTask

import tinCORE.data.Task.ComputationMode

enum class DbComputationMode : ComputationMode {
    Dijkstra,
    Threshold,
    TopK,
    UNSET
}