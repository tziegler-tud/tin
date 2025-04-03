package tinDL.services.evaluation.data

import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import javax.swing.plaf.DimensionUIResource
import kotlin.time.Duration

class LoopTableResult (
    private val identifier: Int,
    private val executionType: ExecutionContextType,
    private val disableCaching: Boolean = false,
    val totalTime: Duration = Duration.ZERO,
    val prewarmDuration: Duration = Duration.ZERO,
    val timePerIteration: Duration = Duration.ZERO,
    val maximumDepth: Int = 0,
    val maxRamUsageBytes: Int = 0,
)
{

}