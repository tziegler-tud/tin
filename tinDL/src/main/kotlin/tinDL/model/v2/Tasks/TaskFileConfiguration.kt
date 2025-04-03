package tinDL.model.v2.Tasks

class TaskFileConfiguration(
    val queryFileIdentifier: Long,
    val ontologyFileIdentifier: Long,
    val transducerMode: TransducerMode,
    val transducerGenerationMode: TransducerGenerationMode?,
    val transducerFileIdentifier: Long?,
) {


}