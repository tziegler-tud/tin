package tinDB.model.v1.queryResult.conjunctiveQueryResult

import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.tinDB.converter.VariableMappingConverter
import jakarta.persistence.*
import tinCORE.data.tinDB.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult

@Entity
class ConjunctiveQueryAnswerMapping(
    val cost: Double,

    @ManyToOne(cascade = [CascadeType.ALL])
    var conjunctiveQueryResult: ConjunctiveQueryResult?,

    @Convert(converter = VariableMappingConverter::class)
    val existentiallyQuantifiedVariablesMapping: HashMap<String, String?>, // variableName (key) -> variableAssignment (value),

    @Convert(converter = VariableMappingConverter::class)
    val answerVariablesMapping: HashMap<String, String?>, // variableName (key) -> variableAssignment (value),

    ) {

    @GeneratedValue
    @Id
    val id: Long = 0

/*    @Embeddable
    data class VariableMapping(
        val variableName: String,
        val variableAssignment: String,
    )*/
}

interface ConjunctiveQueryAnswerMappingRepository : JpaRepository<ConjunctiveQueryAnswerMapping, Long>