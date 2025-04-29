package tinDB.model.v1.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import tinDB.model.v1.queryResult.RegularPathQueryResult


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