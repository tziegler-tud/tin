package tinCORE.data.Task.DlTask

import tinCORE.data.Task.ComputationMode

enum class DlComputationMode : ComputationMode {
    entailment,
    costComputation,
    allIndividuals,
    allWithMaxCost,
    UNSET
}