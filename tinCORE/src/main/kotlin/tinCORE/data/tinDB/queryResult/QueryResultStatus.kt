package tinCORE.data.tinDB.queryResult

enum class QueryResultStatus {
    NoError,
    QueryFileNotFound,
    TransducerFileNotFound,
    DatabaseFileNotFound,
    OntologyFileNotFound,
    ErrorInComputationMode,
    ErrorInComputationProperties
}
