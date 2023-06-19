package tin.data


class QueryResultsData(
    val preprocessing: Long,
    val mainprocessing: Long,
    val answerMap: HashMap<StringPairData, Double>
)

class StringPairData(
    val firstStringIdentifier: String,
    val secondStringIdentifier: String
)
