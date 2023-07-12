package tin.model.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import tin.model.queryResult.QueryResult
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * converts the Set<AnswerTriplet> to a json string for the db,
 * and returns it as a Set<AnswerTriplet> when retrieving it from the db
 */

@Converter
class AnswerSetConverter : AttributeConverter<Set<QueryResult.AnswerTriplet>, String> {
    override fun convertToDatabaseColumn(attribute: Set<QueryResult.AnswerTriplet>?): String {
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Set<QueryResult.AnswerTriplet> {
        val mapper = jacksonObjectMapper()
        return dbData?.let { mapper.readValue<Set<QueryResult.AnswerTriplet>>(it) }
            ?: run { HashSet<QueryResult.AnswerTriplet>() }
    }

}