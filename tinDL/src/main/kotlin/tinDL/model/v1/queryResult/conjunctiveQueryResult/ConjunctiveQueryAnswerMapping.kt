package tinDL.model.v1.queryResult.conjunctiveQueryResult

import org.springframework.data.jpa.repository.JpaRepository
import tinDL.model.v1.converter.VariableMappingConverter
import jakarta.persistence.*

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