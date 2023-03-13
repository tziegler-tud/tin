package tin.data.input

class FileInputData(
        val queryFile: String,
        val transducerFile: String,
        val databaseFile: String,
        val generateTransducer: GenerateTransducer,
)

enum class GenerateTransducer {
    classicAnswers,
    editDistance,
    noGeneration
}