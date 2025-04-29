package tinCORE.data.Task

open class TaskFileConfiguration(
    val queryFileIdentifier: Long,
    val dataSourceFileIdentifier: Long,
    val transducerMode: TransducerMode,
    val transducerGenerationMode: TransducerGenerationMode?,
    val transducerFileIdentifier: Long?,
)