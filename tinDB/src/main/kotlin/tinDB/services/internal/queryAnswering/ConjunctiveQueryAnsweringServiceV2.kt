package tinDB.services.internal.queryAnswering

import tinDB.data.internal.ConjunctiveComputationStatisticsData
import tinDB.model.v2.dataProvider.ConjunctiveQueryDataProvider
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider

import tinDB.model.v2.productAutomaton.ProductAutomatonGraph
import tinDB.model.v1.utils.ProductAutomatonTuple
import tinDB.model.v2.ConjunctiveFormula
import tinDB.model.v2.ConjunctiveQueryGraphMap
import tinDB.model.v2.DatabaseGraph.DatabaseGraph


import tinDB.services.internal.dijkstra.algorithms.Dijkstra
import tinDB.services.internal.dijkstra.algorithms.DijkstraThreshold
import tinDB.services.internal.queryAnswering.conjunctiveUtils.VariableMappingContainer

import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.model.v2.alphabet.Alphabet
import kotlin.system.measureTimeMillis

class ConjunctiveQueryAnsweringServiceV2(
    transducerGraph: TransducerGraph,
    databaseGraph: DatabaseGraph,
    conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    conjunctiveFormula: ConjunctiveFormula

) {

    private var dataProvider: ConjunctiveQueryDataProvider
    private var preProcessing: Long = 0;

    init {
        preProcessing = measureTimeMillis {
            dataProvider = buildDataProvider(
                transducerGraph,
                databaseGraph,
                conjunctiveQueryGraphMap,
                conjunctiveFormula
            )
        }
    }
    /**
     * reads the txt files and builds the data provider
     */
    private fun buildDataProvider(
        transducerGraph: TransducerGraph,
        databaseGraph: DatabaseGraph,
        conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
        conjunctiveFormula: ConjunctiveFormula): ConjunctiveQueryDataProvider {

        val alphabet = Alphabet()

        // add all query alphabets
        conjunctiveQueryGraphMap.getMap().forEach { (graphName, graph) ->
            alphabet.addAlphabet(graph.alphabet)
        }

        // add database alphabet
        alphabet.addAlphabet(databaseGraph.alphabet)

        return ConjunctiveQueryDataProvider(
            conjunctiveQueryGraphMap,
            conjunctiveFormula,
            transducerGraph,
            databaseGraph,
            alphabet
        )
    }

    fun calculateDijkstra(): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

        var totalRPQPreprocessingTime = 0L
        var totalRPQMainProcessingTime = 0L
        var totalRPQPostProcessingTime = 0L
        var totalRPQInternalPostProcessingTime = 0L

        var localPreprocessingTime: Long
        var localMainProcessingTime: Long
        var localPostProcessingTime: Long
        var localInternalPostProcessingTime: Long

        val regularPathQueryResultSet = HashSet<RegularPathQueryResult>()

        dataProvider.conjunctiveQueryGraphMap.getMap().forEach {

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassemble(dataProvider, queryTask).toSet()

        }

        val sortedVariableMappingsContainerSet: Set<VariableMappingContainer>

        val postProcessing = measureTimeMillis {
            sortedVariableMappingsContainerSet = variableMappingContainerSet.sortedBy { it.cost }.toSet()
        }

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val combinedRPQTimeInMs = totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = queryTask,
            computationStatistics = null,
            queryResultStatus = QueryResultStatus.NoError,
            variableMappings = emptySet(),
            regularPathQueryResults = regularPathQueryResultSet
        )

        /**
         * use the previously built ConjunctiveQueryResult object to build the ConjunctiveQueryAnswerMapping objects and store them in the ConjunctiveQueryResult
         */
        val res = conjunctiveQueryResult.apply {
            variableMappings = sortedVariableMappingsContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()
        }

        return Pair(
            res, ConjunctiveComputationStatisticsData(
                preProcessingTimeInMs = 0,
                mainProcessingTimeInMs = 0,
                postProcessingTimeInMs = postProcessing,
                combinedTimeInMs = 0,
                combinedRPQPreProcessingTimeInMs = totalRPQPreprocessingTime,
                combinedRPQMainProcessingTimeInMs = totalRPQMainProcessingTime,
                combinedRPQPostProcessingTimeInMs = totalRPQPostProcessingTime,
                combinedRPQInternalPostProcessingTimeInMs = totalRPQInternalPostProcessingTime,
                combinedRPQTimeInMs = combinedRPQTimeInMs,
                reassemblyTimeInMs = reassemblingTime
            )
        )
    }

    private fun calculateThreshold(
        threshold: Double
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {

        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

        var totalRPQPreprocessingTime = 0L
        var totalRPQMainProcessingTime = 0L
        var totalRPQPostProcessingTime = 0L
        var totalRPQInternalPostProcessingTime = 0L

        var localPreprocessingTime: Long
        var localMainProcessingTime: Long
        var localPostProcessingTime: Long
        var localInternalPostProcessingTime: Long

        val regularPathQueryResultSet = HashSet<RegularPathQueryResult>()

        dataProvider.conjunctiveQueryGraphMap.getMap().forEach {

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = DijkstraThreshold(productAutomatonGraph, queryTask.computationProperties.thresholdValue!!)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassembleThreshold(dataProvider, queryTask).toSet()

        }

        val sortedVariableMappingsContainerSet: Set<VariableMappingContainer>

        val postProcessing = measureTimeMillis {
            sortedVariableMappingsContainerSet = variableMappingContainerSet.sortedBy { it.cost }.toSet()
        }

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val combinedRPQTimeInMs = totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = queryTask,
            computationStatistics = null,
            queryResultStatus = QueryResultStatus.NoError,
            variableMappings = emptySet(),
            regularPathQueryResults = regularPathQueryResultSet
        )

        /**
         * use the previously built ConjunctiveQueryResult object to build the ConjunctiveQueryAnswerMapping objects and store them in the ConjunctiveQueryResult
         */
        val res = conjunctiveQueryResult.apply {
            variableMappings = sortedVariableMappingsContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()
        }

        return Pair(
            res, ConjunctiveComputationStatisticsData(
                preProcessingTimeInMs = 0,
                mainProcessingTimeInMs = 0,
                postProcessingTimeInMs = postProcessing,
                combinedTimeInMs = 0,
                combinedRPQPreProcessingTimeInMs = totalRPQPreprocessingTime,
                combinedRPQMainProcessingTimeInMs = totalRPQMainProcessingTime,
                combinedRPQPostProcessingTimeInMs = totalRPQPostProcessingTime,
                combinedRPQInternalPostProcessingTimeInMs = totalRPQInternalPostProcessingTime,
                combinedRPQTimeInMs = combinedRPQTimeInMs,
                reassemblyTimeInMs = reassemblingTime
            )
        )
    }

    private fun calculateTopK(
        amount: Int
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        /**
         * since we want to find the global optimum we must not use local optimization, thus we have to use "findAll" when calculating 2RPQs and sort/filter at the end.
         */

        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

        var totalRPQPreprocessingTime = 0L
        var totalRPQMainProcessingTime = 0L
        var totalRPQPostProcessingTime = 0L
        var totalRPQInternalPostProcessingTime = 0L

        var localPreprocessingTime: Long
        var localMainProcessingTime: Long
        var localPostProcessingTime: Long
        var localInternalPostProcessingTime: Long

        val regularPathQueryResultSet = HashSet<RegularPathQueryResult>()

        dataProvider.conjunctiveQueryGraphMap.getMap().forEach {

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassemble(dataProvider, queryTask).toSet()

        }

        val sortedVariableMappingsContainerSet: Set<VariableMappingContainer>

        val postProcessing = measureTimeMillis {
            sortedVariableMappingsContainerSet = variableMappingContainerSet.sortedBy { it.cost }.take(queryTask.computationProperties.topKValue!!).toSet()
        }

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val combinedRPQTimeInMs = totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = queryTask,
            computationStatistics = null,
            queryResultStatus = QueryResultStatus.NoError,
            variableMappings = emptySet(),
            regularPathQueryResults = regularPathQueryResultSet
        )

        /**
         * use the previously built ConjunctiveQueryResult object to build the ConjunctiveQueryAnswerMapping objects and store them in the ConjunctiveQueryResult
         */
        val res = conjunctiveQueryResult.apply {
            variableMappings = sortedVariableMappingsContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()
        }

        return Pair(
            res, ConjunctiveComputationStatisticsData(
                preProcessingTimeInMs = 0,
                mainProcessingTimeInMs = 0,
                postProcessingTimeInMs = postProcessing,
                combinedTimeInMs = 0,
                combinedRPQPreProcessingTimeInMs = totalRPQPreprocessingTime,
                combinedRPQMainProcessingTimeInMs = totalRPQMainProcessingTime,
                combinedRPQPostProcessingTimeInMs = totalRPQPostProcessingTime,
                combinedRPQInternalPostProcessingTimeInMs = totalRPQInternalPostProcessingTime,
                combinedRPQTimeInMs = combinedRPQTimeInMs,
                reassemblyTimeInMs = reassemblingTime
            )
        )
    }

    private fun buildDummyPairForErrorCase(queryTask: QueryTask): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData?> {
        return Pair(
            ConjunctiveQueryResult(
                queryTask,
                null,
                QueryResultStatus.ErrorInComputationMode,
                emptySet(),
                emptySet()
            ), null
        )
    }
}