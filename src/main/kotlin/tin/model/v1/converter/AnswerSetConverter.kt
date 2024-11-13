package tin.model.v1.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import tin.model.v1.queryResult.RegularPathQueryResult
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * converts the Set<AnswerTriplet> to a json string for the db,
 * and returns it as a Set<AnswerTriplet> when retrieving it from the db
 */

@Converter
class AnswerSetConverter : AttributeConverter<Set<RegularPathQueryResult.AnswerTriplet>, String> {
    override fun convertToDatabaseColumn(attribute: Set<RegularPathQueryResult.AnswerTriplet>?): String {
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Set<RegularPathQueryResult.AnswerTriplet> {
        val mapper = jacksonObjectMapper()
        return dbData?.let { mapper.readValue<Set<RegularPathQueryResult.AnswerTriplet>>(it) }
            ?: run { HashSet<RegularPathQueryResult.AnswerTriplet>() }
    }

}