package tin.services.internal.fileReaders

import org.springframework.stereotype.Service
import tin.model.ConjunctTriplet
import tin.model.ConjunctiveFormula
import tin.model.alphabet.Alphabet
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.model.ConjunctiveQueryGraphMap
import tin.services.internal.fileReaders.fileReaderResult.ConjunctiveQueryFileReaderResult
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File
import java.lang.Exception
import java.util.HashMap

@Service
class ConjunctiveQueryReaderService(
    systemConfigurationService: SystemConfigurationService,

    ) : FileReaderService<ConjunctiveQueryFileReaderResult>(systemConfigurationService) {
    override var filePath = systemConfigurationService.getConjunctiveQueryPath()
    override var inputFileMaxLines: Int = systemConfigurationService.getQuerySizeLimit()

    override fun processFile(file: File, breakOnError: Boolean): ConjunctiveQueryFileReaderResult {
        val conjunctiveQueryGraphMap = ConjunctiveQueryGraphMap(mutableMapOf())
        var queryGraph = QueryGraph()
        val queryNodes = HashMap<String, QueryNode>() // map containing the QueryNodes
        var alphabet = Alphabet()
        var graphIdentifier: String? = null
        var formula: ConjunctiveFormula? = null

        var source: QueryNode
        var target: QueryNode
        var node: QueryNode
        var edgeLabel: String
        var stringArray: Array<String>

        var currentlyReading = InputTypeEnum.UNDEFINED
        var currentLine: String

        val bufferedReader: BufferedReader = file.bufferedReader()

        //regexp to validate and sanitize edge input
        // Hint: \\w(\\w|-\\w)* matches words that start with a character or underscore, and every - is followed by another character or underscore

        //trailing and leading whitespaces and tab characters are removed before processing!
        //node line
        val anyNodeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*((true)|(false))\\s*,\\s*((true)|(false))")

        // edge lines
        val anyEdgeRegex = Regex("\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\s*,\\s*\\w(\\w|-\\w)*\\??") // for roles

        // regex for graph identifier
        val graphIdentifierRegex = Regex("^[g|G]raph\\s(\\w+)")

        var currentLineIndex = 0
        while (currentLineIndex <= inputFileMaxLines) {
            currentLineIndex++
            // read current line; exit loop when at the end of the file
            currentLine = bufferedReader.readLine() ?: break

            //remove leading and trailing whitespaces and tab characters
            currentLine = currentLine.replace(Regex("^\\s*"), "")
            currentLine = currentLine.replace(Regex("\\s*$"), "")

            //lines starting with // are ignored
            if (commentLineRegex.matchEntire(currentLine) !== null) {
                continue
            }

            if (graphIdentifierRegex.matchEntire(currentLine) !== null) {
                currentlyReading = InputTypeEnum.GRAPH_IDENTIFIER
            }

            // when we see "nodes", we will read nodes starting from the next line
            if (currentLine == "nodes") {
                //only valid if we are currently reading a graph, i.e. there has been a line "graph R1" before
                currentlyReading = InputTypeEnum.NODES
                // after setting the flags, we skip into the next line
                continue
            }

            if (currentLine == "edges") {
                //only valid if we are currently reading a graph, i.e. there has been a line "graph R1" before
                currentlyReading = InputTypeEnum.EDGES

                // after setting the flags, we skip into the next line
                continue
            }

            if (currentLine == "formula") {
                currentlyReading = InputTypeEnum.CONJUNCTIVE_FORMULA
                continue
            }

            if (currentLine.contains("graph")) {
                currentlyReading = InputTypeEnum.GRAPH_IDENTIFIER
            }

            //save og line for debugging
            val originalLine = currentLine

            when (currentlyReading) {
                InputTypeEnum.NODES -> {
                    //insert into currently reading Graph --> this.currentlyReadingGraph
                    val nodeMatchResult = anyNodeRegex.matchEntire(currentLine)
                    if (nodeMatchResult !== null) {
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        node = QueryNode(stringArray[0], stringArray[1].toBoolean(), stringArray[2].toBoolean())

                        val existingNode = queryNodes[stringArray[0]]
                        if (existingNode != null) {
                            //node identifier already taken. Check similarity.
                            if (node.equalsWithoutEdges(existingNode)) {
                                this.warn("Duplicated node identifier.", currentLineIndex, originalLine)
                                continue
                            } else {
                                //identifier taken, but initialState or finalState differs. This is an error.
                                this.error(
                                    "Failed to read line as node: Non-repairable duplicated node identifier.",
                                    currentLineIndex,
                                    originalLine
                                )
                                continue
                            }
                        }
                        queryNodes[stringArray[0]] = node
                        queryGraph.addNodes(node)

                        //TODO: Check semantically, e.g. if there is at least one initial state and at least one reachable final state.
                    } else {
                        this.error("Failed to read line as node: Invalid input format.", currentLineIndex, currentLine)
                        if (breakOnError) break
                    }
                }

                InputTypeEnum.EDGES -> {
                    //check line against Regexp to check for valid input format.
                    val anyEdgeMatchResult = anyEdgeRegex.matchEntire(currentLine)
                    if (anyEdgeMatchResult !== null) {
                        //line is valid
                        currentLine = currentLine.replace("\\s".toRegex(), "")
                        stringArray = currentLine.split(",").toTypedArray()

                        // nodes have to be present, because they have been defined before reading any edges in the file
                        source = queryNodes[stringArray[0]]!!
                        target = queryNodes[stringArray[1]]!!

                        edgeLabel = stringArray[2]
                        queryGraph.addEdge(source, target, edgeLabel)

                        if (Alphabet.isConceptAssertion(edgeLabel)) {
                            //concept assertion read, extract concept name
                            try {
                                val conceptLabel = Alphabet.conceptNameFromAssertion(edgeLabel)
                                if (!alphabet.includes(conceptLabel)) alphabet.addConceptName(conceptLabel)
                            } catch (e: IllegalArgumentException) {
                                this.error(
                                    "Failed to read property name from edge label",
                                    currentLineIndex,
                                    currentLine
                                )
                                if (breakOnError) break
                            }
                        } else {
                            //not a concept assertions
                            if (!alphabet.includes(edgeLabel)) alphabet.addRoleName(edgeLabel)
                        }
                    } else {
                        //invalid line
                        this.error("Failed to read line as edge: Invalid input format.", currentLineIndex, currentLine)
                        if (breakOnError) break
                    }
                }

                InputTypeEnum.GRAPH_IDENTIFIER -> {
                    // first, check for previous graph identifier.
                    // if there is one, we have successfully read a graph and can add it to the list.
                    if (graphIdentifier != null) {
                        //zu viele Checks
                        //if (!queryGraph.isEmpty() && queryGraph.hasInitialNode() && queryGraph.hasFinalNode()) {
                        if (queryGraph.isValidGraph()) {
                            // graph is nonempty and graphIdentifier is not null -> we've read a populated graph
                            queryGraph.alphabet = alphabet
                            addGraphToMap(
                                conjunctiveQueryGraphMap,
                                graphIdentifier,
                                queryGraph,
                                currentLineIndex,
                                currentLine
                            )
                            /**
                             * here we do the big resets, since we've just added one whole queryGraph to the map.
                             */
                            queryGraph = QueryGraph()
                            queryNodes.clear()
                            alphabet = Alphabet()
                        } else if (queryGraph.isEmpty()) {
                            this.error(
                                "Failed to read graph: 'Graph ${graphIdentifier}' is empty.",
                                currentLineIndex,
                                currentLine
                            )
                        } else if (!queryGraph.hasFinalNode()) {
                            this.error(
                                "Failed to read graph: 'Graph ${graphIdentifier}' has no final node.",
                                currentLineIndex,
                                currentLine
                            )
                        } else if (!queryGraph.hasInitialNode()) {
                            this.error(
                                "Failed to read graph: 'Graph ${graphIdentifier}' has no initial node.",
                                currentLineIndex,
                                currentLine
                            )
                        }
                    }

                    // set the graphIdentifier to the new read identifier
                    graphIdentifier = graphIdentifierRegex.matchEntire(currentLine)?.groups?.get(1)?.value

                }

                InputTypeEnum.CONJUNCTIVE_FORMULA -> {
                    /**
                     * the formula is of the form
                     * q(x) = exists(y).phi(x,y), where
                     * q is just an identifier for the query (in fact this identifier is not processed at the moment),
                     * x is a tuple of variables, called answer variables,
                     * y is a tuple of variables, called existentially quantified variables,
                     * and phi(x,y) is a conjunction of atoms of the form R(t,t') where t,t' are either element of x or y, or database individuals (TODO: this is not implemented)
                     */

                    // step 1: handle the answer variables
                    // we split the string at the '=' and remove all whitespaces
                    val leftHandString = currentLine.substringBefore('=').replace(" ", "")
                    val leftHandRegex = Regex("""^(\w+)\(([^)]*)\)$""")

                    val leftHandMatch = leftHandRegex.matchEntire(leftHandString)
                    val queryIdentifier = leftHandMatch?.groups?.get(1)?.value
                    if (queryIdentifier == null) {
                        this.error(
                            "Failed to read line as formula: Invalid input format (query identifier not found).",
                            currentLineIndex,
                            currentLine
                        )
                    }

                    val answerVariables = leftHandMatch?.groups?.get(2)?.value?.split(",")?.toMutableSet()
                    if (answerVariables == null) {
                        this.error(
                            "Failed to read line as formula: Invalid input format (answer variables not found).",
                            currentLineIndex,
                            currentLine
                        )
                    }

                    // step 2: handle the formula after the '='
                    val rightHandString = currentLine.substringAfter('=')

                    /**
                     * this pattern splits the formula into its parts
                     * e.g. for the formula 'exists(x,y).phi(R1(x,z) and R2(y,z) and R3(z,z))'
                     * the regex matches are 'exists(x,y)', 'phi(R1(x,z)', 'R2(y,z)', 'R3(z,z))'
                     */
                    val formulaSplitPattern = Regex("""(?:\b[a-zA-Z]+(?:\d+)?\([^)]+\))""")
                    val patternMatches = formulaSplitPattern.findAll(rightHandString)

                    // storing the results in these variables
                    var existentiallyQuantifiedVariables: MutableSet<String> = mutableSetOf()
                    var greekLetter: String = ""
                    val alphabetOfAllMentionedVariables: MutableSet<String> = mutableSetOf()
                    val regularPathQuerySourceVariableAssignment: MutableMap<String, String> = mutableMapOf()
                    val regularPathQueryTargetVariableAssignment: MutableMap<String, String> = mutableMapOf()
                    var conjunctsTripletSet: Set<ConjunctTriplet> = mutableSetOf()

                    // iterate through each match and handle it accordingly
                    patternMatches.forEach {
                        // check for existential quantifier
                        if (it.value.contains("exists")) {

                            val existsRegex = Regex("exists\\(([^),]+(?:,[^),]+)*)\\)")
                            val match = existsRegex.matchEntire(it.value)

                            if (match?.groups?.size != 2) {
                                // group size has to be 2, else the format was violated
                                this.error(
                                    "Failed to read line as formula: Invalid input format (existentially quantified variables).",
                                    currentLineIndex,
                                    currentLine
                                )
                            } else {
                                // extract the variables
                                val variables = match.groups[1]?.value?.split(",")?.toMutableSet()
                                if (variables == null) {
                                    this.error(
                                        "Failed to read line as formula: No existentially quantified variables found.",
                                        currentLineIndex,
                                        currentLine
                                    )
                                } else {
                                    existentiallyQuantifiedVariables = variables
                                }
                            }

                        } else {
                            /**
                             * it could be of two other valid types now.
                             * 1. phi(R1(x,z) or 2. R1(x,z)
                             * in the first case we have to extract the greek letter and the variables
                             * in the second case we only have to extract the variables
                             *
                             * we split after the first bracket -> if the variables contain a bracket, we have type 1
                             * else we have type 2
                             */
                            var identifier = it.value.substringBefore('(')
                            var variables = it.value.substringAfter('(').dropLast(1)

                            if (variables.contains('(')) {
                                // type 1 found
                                greekLetter = identifier
                                identifier = variables.substringBefore('(')
                                variables = variables.substringAfter('(')
                            }

                            val sourceVariable = variables.substringBefore(',')
                            val targetVariable = variables.substringAfter(',')

                            regularPathQuerySourceVariableAssignment[identifier] = sourceVariable
                            regularPathQueryTargetVariableAssignment[identifier] = targetVariable

                            alphabetOfAllMentionedVariables.add(sourceVariable)
                            alphabetOfAllMentionedVariables.add(targetVariable)

                            conjunctsTripletSet =
                                conjunctsTripletSet + ConjunctTriplet(identifier, sourceVariable, targetVariable)

                        }
                    }


                    /**
                     * check for errors
                     * TODO: this section is not complete
                     */

                    if (existentiallyQuantifiedVariables.isEmpty()) {
                        this.error(
                            "Failed to read line as formula: No existentially quantified variables found.",
                            currentLineIndex,
                            currentLine
                        )
                    }

                    if (greekLetter.isEmpty()) {
                        this.error(
                            "Failed to read line as formula: No greek letter found.",
                            currentLineIndex,
                            currentLine
                        )
                    }


                    formula = ConjunctiveFormula(
                        existentiallyQuantifiedVariables = existentiallyQuantifiedVariables,
                        answerVariables = answerVariables!!, // we checked for null and write an error when this is the case
                        greekLetter = greekLetter,
                        regularPathQuerySourceVariableAssignment = regularPathQuerySourceVariableAssignment,
                        regularPathQueryTargetVariableAssignment = regularPathQueryTargetVariableAssignment,
                        conjunctsTripletSet = conjunctsTripletSet
                    )

                }

                else -> {
                    this.warn("Unhandled line.", currentLineIndex, currentLine)
                }
            }
        }

        if (queryGraph.isValidGraph() && graphIdentifier !== null) {
            //save remaining graph
            queryGraph.alphabet = alphabet
            addGraphToMap(conjunctiveQueryGraphMap, graphIdentifier, queryGraph, currentLineIndex, "EOF")
        }

        if (formula == null) {
            throw Exception("conjunctive formula could not be parsed.")
        } else {

            return ConjunctiveQueryFileReaderResult(conjunctiveQueryGraphMap, formula, warnings, errors)
        }
    }

    private fun addGraphToMap(
        conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
        graphIdentifier: String,
        queryGraph: QueryGraph,
        currentLineIndex: Int,
        currentLine: String
    ) {
        val overriddenKey = conjunctiveQueryGraphMap.addGraphToMap(graphIdentifier, queryGraph)
        if (overriddenKey) {
            this.warn("Graph ${graphIdentifier} has been overriden.", currentLineIndex, currentLine)
        }

    }
}