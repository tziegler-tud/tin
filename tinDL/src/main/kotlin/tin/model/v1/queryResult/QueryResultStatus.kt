package tin.model.v1.queryResult

enum class QueryResultStatus {
    NoError,
    QueryFileNotFound,
    TransducerFileNotFound,
    DatabaseFileNotFound,
    OntologyFileNotFound,
    ErrorInComputationMode,
    ErrorInComputationProperties
}
