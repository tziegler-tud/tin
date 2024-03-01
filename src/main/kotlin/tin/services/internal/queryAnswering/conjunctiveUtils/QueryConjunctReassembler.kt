package tin.services.internal.queryAnswering.conjunctiveUtils

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.ConjunctiveFormula
import tin.model.dataProvider.ConjunctiveQueryDataProvider
import tin.model.queryResult.QueryResultRepository
import tin.model.queryResult.RegularPathQueryResult
import tin.model.queryTask.QueryTask
import java.lang.Exception
import java.util.HashSet

/**
 * this class will handle the reassembly of the single conjunct query results in order to answer the conjunctive query
 *
 */
@Service
class QueryConjunctReassembler(
    private val queryResultRepository: QueryResultRepository

) {

    @Transactional
    fun reassemble(
        conjunctiveQueryDataProvider: ConjunctiveQueryDataProvider,
        queryTask: QueryTask
    ): HashSet<VariableMappingContainer> {

        // retrieve all the single query results
        // map together graph identifier with the answer set
        val singleResultsList = try {
            queryResultRepository.findAllByQueryTask(queryTask)
                .associateBy({ requireNotNull((it as RegularPathQueryResult).identifier) },
                    { (it as RegularPathQueryResult).answerSet })
        } catch (e: IllegalArgumentException) {
            // todo: more info on the concrete object that caused the error
            throw IllegalArgumentException("Error while reading a single query result. The identifier is null.")
        }

        // define prevVMCSet = {} + 1x 'empty' VMC
        val setOfAllVariables =
            conjunctiveQueryDataProvider.conjunctiveFormula.answerVariables + conjunctiveQueryDataProvider.conjunctiveFormula.existentiallyQuantifiedVariables
        var prevVMCSet = HashSet<VariableMappingContainer>().apply {
            add(buildEmptyVariableContainerForVariables(conjunctiveFormula = conjunctiveQueryDataProvider.conjunctiveFormula))
        }

        //define currentVMCSet = {}
        val currentVMCSet: HashSet<VariableMappingContainer> = HashSet()
        var currentSourceVariableName: String
        var currentTargetVariableName: String

        singleResultsList.forEach { (identifier, answerSet) ->
            // find the corresponding conjunct
            val tempTriplet =
                conjunctiveQueryDataProvider.conjunctiveFormula.conjunctsTripletSet.find { it.identifier == identifier }
                    ?: throw IllegalArgumentException("Error while reassembling the conjunctive query result. The identifier is not present in the conjunctsTripletSet.")
            // set the source and target variable name for this conjunct.
            currentSourceVariableName = tempTriplet.sourceVariable
            currentTargetVariableName = tempTriplet.targetVariable

            answerSet.forEach { answerTriplet ->
                prevVMCSet.forEach { vmcSet ->

                    // find out if we handle answer or existentially quantified variables
                    val sourceVariableIsAnswerVariable =
                        conjunctiveQueryDataProvider.conjunctiveFormula.answerVariables.contains(currentSourceVariableName)
                    val targetVariableIsAnswerVariable =
                        conjunctiveQueryDataProvider.conjunctiveFormula.answerVariables.contains(currentTargetVariableName)

                    val sourceVariableAssignment =
                        if (sourceVariableIsAnswerVariable) vmcSet.answerVariablesMapping[currentSourceVariableName] else vmcSet.existentiallyQuantifiedVariablesMapping[currentSourceVariableName]
                    val targetVariableAssignment =
                        if (targetVariableIsAnswerVariable) vmcSet.answerVariablesMapping[currentTargetVariableName] else vmcSet.existentiallyQuantifiedVariablesMapping[currentTargetVariableName]
                    // find "mismatches", i.e. at least one variable is not null and differs the answerTriplet.
                    if (sourceVariableAssignment != null && sourceVariableAssignment != answerTriplet.source
                        || targetVariableAssignment != null && targetVariableAssignment != answerTriplet.target
                    ) {
                        // We've encountered a mismatch, thus we do not add the prevVacSet to the currentVMCSet.
                    } else /*if (sourceVariableAssignment == null
                        || targetVariableAssignment == null
                        || (sourceVariableAssignment == answerTriplet.source && targetVariableAssignment == answerTriplet.target))*/{
                        // We've encountered a match, thus we add the prevVacSet to the currentVMCSet.
                        applyVariableAssignments(
                            fittingVMC = vmcSet,
                            sourceVariableIsAnswerVariable = sourceVariableIsAnswerVariable,
                            sourceVariableName = currentSourceVariableName,
                            sourceVariableAssignment = answerTriplet.source,
                            targetVariableIsAnswerVariable = targetVariableIsAnswerVariable,
                            targetVariableName = currentTargetVariableName,
                            targetVariableAssignment = answerTriplet.target,
                            cost = answerTriplet.cost
                        ).onSuccess {
                            currentVMCSet.add(it)
                        }
                    }
                }
            }

            // set the currentVMCSet as the new prevVMCSet
            prevVMCSet.clear()
            prevVMCSet.addAll(currentVMCSet)
            // reset the working set
            currentVMCSet.clear()
        }

        return prevVMCSet
    }

    private fun buildEmptyVariableContainerForVariables(conjunctiveFormula: ConjunctiveFormula): VariableMappingContainer {
        return VariableMappingContainer(
            cost = 0.0,
            existentiallyQuantifiedVariablesMapping = HashMap(conjunctiveFormula.existentiallyQuantifiedVariables.associateWith { null }),
            answerVariablesMapping = HashMap(conjunctiveFormula.answerVariables.associateWith { null })
        )
    }

    /**
     * This function applies the variable assignments to the given VariableAssignmentContainer and returns the new VMC including the added cost
     * @param fittingVMC the VMC to be used as a base for the new VMC
     * @param sourceVariableName the name of the source variable
     * @param sourceVariableAssignment the assignment of the source variable (from answerTriplet)
     * @param targetVariableName the name of the target variable
     * @param targetVariableAssignment the assignment of the target variable (from answerTriplet)
     * @param cost the cost that should be added to the VMC (from answerTriplet)
     * @return the new VMC including the added cost
     */
    private fun applyVariableAssignments(
        fittingVMC: VariableMappingContainer,
        sourceVariableIsAnswerVariable: Boolean,
        sourceVariableName: String,
        sourceVariableAssignment: String,
        targetVariableIsAnswerVariable: Boolean,
        targetVariableName: String,
        targetVariableAssignment: String,
        cost: Double
    ): Result<VariableMappingContainer> {


        /**
         * check if the source and target variable are already assigned to a different value
         * We intentionally make this check redundant (because we already checked that in the main function, from which we call this function).
         */

        if (sourceVariableIsAnswerVariable) {
            if (fittingVMC.answerVariablesMapping[sourceVariableName] != null && fittingVMC.answerVariablesMapping[sourceVariableName] != sourceVariableAssignment) {
                return Result.failure(Exception("Error while applying variable assignments. The source variable is already assigned to a different value."))
            }
        } else {
            if (fittingVMC.existentiallyQuantifiedVariablesMapping[sourceVariableName] != null && fittingVMC.existentiallyQuantifiedVariablesMapping[sourceVariableName] != sourceVariableAssignment) {
                return Result.failure(Exception("Error while applying variable assignments. The source variable is already assigned to a different value."))
            }
        }

        if (targetVariableIsAnswerVariable) {
            if (fittingVMC.answerVariablesMapping[targetVariableName] != null && fittingVMC.answerVariablesMapping[targetVariableName] != targetVariableAssignment) {
                return Result.failure(Exception("Error while applying variable assignments. The target variable is already assigned to a different value."))
            }
        } else {
            if (fittingVMC.existentiallyQuantifiedVariablesMapping[targetVariableName] != null && fittingVMC.existentiallyQuantifiedVariablesMapping[targetVariableName] != targetVariableAssignment) {
                return Result.failure(Exception("Error while applying variable assignments. The target variable is already assigned to a different value."))
            }

        }

        val tempVMC = VariableMappingContainer(
            cost = fittingVMC.cost,
            existentiallyQuantifiedVariablesMapping = HashMap(fittingVMC.existentiallyQuantifiedVariablesMapping),
            answerVariablesMapping = HashMap(fittingVMC.answerVariablesMapping)
        )


        /**
         * return the new VMC with the applied variable assignments and the added cost
         */
        return Result.success(tempVMC.apply {
            if (sourceVariableIsAnswerVariable) {
                answerVariablesMapping[sourceVariableName] = sourceVariableAssignment
            } else {
                existentiallyQuantifiedVariablesMapping[sourceVariableName] = sourceVariableAssignment
            }

            if (targetVariableIsAnswerVariable) {
                answerVariablesMapping[targetVariableName] = targetVariableAssignment
            } else {
                existentiallyQuantifiedVariablesMapping[targetVariableName] = targetVariableAssignment
            }

            this.cost += cost
        })
    }
}