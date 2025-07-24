package tinCORE.data.tinDB.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * converts the Set<VariableMapping> to a json string for the db,
 * and returns it as a Set<VariableMapping> when retrieving it from the db
 */
@Converter
class VariableMappingConverter : AttributeConverter<HashMap<String, String?>, String> {

    override fun convertToDatabaseColumn(attribute: HashMap<String, String?>?): String? {
        return jacksonObjectMapper().writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): HashMap<String, String?>? {
        return jacksonObjectMapper().readValue(dbData, object : TypeReference<HashMap<String, String?>>() {})
    }
}