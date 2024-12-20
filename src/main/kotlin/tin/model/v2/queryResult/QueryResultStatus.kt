package tin.model.v2.queryResult

enum class QueryResultStatus {
    NoError,
    QueryFileNotFound,
    TransducerFileNotFound,
    DatabaseFileNotFound,
    OntologyFileNotFound,
    ErrorInComputationMode,
    ErrorInComputationProperties
}
