package tinCORE.data.Task.DlTask.Benchmark

import kotlin.time.TimeSource

class TaskProcessingResultTimes(
    val spaStartTime: TimeSource.Monotonic.ValueTimeMark,
    val spaEndTime: TimeSource.Monotonic.ValueTimeMark,
    val spStartTime: TimeSource.Monotonic.ValueTimeMark,
    val spEndTime: TimeSource.Monotonic.ValueTimeMark,
    val resultGraphStartTime: TimeSource.Monotonic.ValueTimeMark,
    val resultGraphEndTime: TimeSource.Monotonic.ValueTimeMark,
    val solverStartTime: TimeSource.Monotonic.ValueTimeMark,
    val solverEndTime: TimeSource.Monotonic.ValueTimeMark,
) {
    val spaTime = spaEndTime - spaStartTime;
    val spTime = spEndTime - spStartTime;
    val resultGraphTime = resultGraphEndTime - resultGraphStartTime;
    val solverTime = solverEndTime - solverStartTime;

}