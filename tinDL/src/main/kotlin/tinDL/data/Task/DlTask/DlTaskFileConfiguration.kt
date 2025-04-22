package tinDL.data.Task.DlTask

class DlTaskFileConfiguration(
    val queryFileIdentifier: Long,
    val ontologyFileIdentifier: Long,
    val transducerMode: TransducerMode,
    val transducerGenerationMode: TransducerGenerationMode?,
    val transducerFileIdentifier: Long?,
) {


}