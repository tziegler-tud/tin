package tinLIB.data.Task

class TaskFileConfiguration(
    val queryFileIdentifier: Long,
    val transducerMode: TransducerMode,
    val transducerGenerationMode: TransducerGenerationMode?,
    val transducerFileIdentifier: Long?,
)