package tin.data.input

class QueryData(
        val queryFile: String,
        val transducerFile: String,
        val databaseFile: String,
        val generateTransducer: GenerateTransducer,
)

enum class GenerateTransducer {
    ClassicAnswers,
    EditDistance,
    NoGeneration
}