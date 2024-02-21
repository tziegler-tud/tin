package tin.model.queryResult

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.converter.VariableMappingConverter
import javax.persistence.*

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