package tinDB.services.internal.queryAnswering.conjunctiveUtils

data class VariableMappingContainer(
    var cost: Double,
    val existentiallyQuantifiedVariablesMapping: HashMap<String, String?>, // variableName (key) -> variableAssignment (value)
    val answerVariablesMapping: HashMap<String, String?>, // variableName (key) -> variableAssignment (value)
    //val variableAssignment: HashMap<String, String?> // variableName (key) -> variableAssignment (value)
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableMappingContainer) return false

        if (cost != other.cost) return false
        if (existentiallyQuantifiedVariablesMapping != other.existentiallyQuantifiedVariablesMapping) return false
        return answerVariablesMapping == other.answerVariablesMapping
    }

    override fun hashCode(): Int {
        var result = cost.hashCode()
        result = 31 * result + existentiallyQuantifiedVariablesMapping.hashCode()
        result = 31 * result + answerVariablesMapping.hashCode()
        return result
    }

    /**
     * overridden copy function to create a deep copy where the maps do not share the same reference.
     */
    fun copy(): VariableMappingContainer {
        return VariableMappingContainer(
            cost = this.cost,
            existentiallyQuantifiedVariablesMapping = HashMap(this.existentiallyQuantifiedVariablesMapping),
            answerVariablesMapping = HashMap(this.answerVariablesMapping)
        )
    }


}