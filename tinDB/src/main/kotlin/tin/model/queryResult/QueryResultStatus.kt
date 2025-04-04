package tin.model.queryResult

enum class QueryResultStatus {
    NoError,
    QueryFileNotFound,
    TransducerFileNotFound,
    DatabaseFileNotFound,
    OntologyFileNotFound,
    ErrorInComputationMode,
    ErrorInComputationProperties
}
