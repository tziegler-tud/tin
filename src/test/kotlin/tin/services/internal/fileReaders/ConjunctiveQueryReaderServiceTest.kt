package tin.services.internal.fileReaders

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.ConjunctiveFormula
import tin.model.ConjunctiveQueryGraphMap
import tin.model.query.QueryEdge
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.services.internal.fileReaders.fileReaderResult.ConjunctiveQueryFileReaderResult
import tin.services.technical.SystemConfigurationService

@SpringBootTest
@TestConfiguration
class ConjunctiveQueryReaderServiceTest {

    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    private fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : ConjunctiveQueryFileReaderResult {
        val fileReaderService: ConjunctiveQueryReaderService = ConjunctiveQueryReaderService(systemConfigurationService)
        val testFilePath = systemConfigurationService.getConjunctiveQueryPath()
        return fileReaderService.read(testFilePath, fileName, breakOnError)
    }

    private fun constructComparisonResult(): ConjunctiveQueryFileReaderResult {
        val graphMap: ConjunctiveQueryGraphMap = ConjunctiveQueryGraphMap(mutableMapOf())
        var formula: ConjunctiveFormula

        //building graph R1
        var s0 = QueryNode("s0", true, false)
        var s1 = QueryNode("s1", false, true)
        var e0 = QueryEdge(s0, s1, "has-part")
        var e1 = QueryEdge(s1, s1, "has-part")

        val graphR1 = QueryGraph()

        graphR1.addNodes(s0, s1)
        graphR1.addEdge(e0.source, e0.target, e0.label)
        graphR1.addEdge(e1.source, e1.target, e1.label)
        graphR1.alphabet.addRoleName("has-part")

        //building graph R2
        s0 = QueryNode("s0", true, true)
        e0 = QueryEdge(s0, s0, "has-part")

        val graphR2 = QueryGraph()

        graphR2.addNodes(s0)
        graphR2.addEdge(e0.source, e0.target, e0.label)
        graphR2.alphabet.addRoleName("has-part")

        //building graph R3
        s0 = QueryNode("s0", true, false)
        s1 = QueryNode("s1", false, true)
        e0 = QueryEdge(s0, s1, "Motor?")

        val graphR3 = QueryGraph()
        graphR3.addNodes(s0, s1)
        graphR3.addEdge(e0.source, e0.target, e0.label)
        graphR3.alphabet.addConceptName("Motor")

        graphMap.addGraphToMap("R1", graphR1)
        graphMap.addGraphToMap("R2", graphR2)
        graphMap.addGraphToMap("R3", graphR3)

        //building formula
        val existentiallyQuantifiedVariables: MutableSet<String> = mutableSetOf("x", "y")
        val helperVariables: MutableSet<String> = mutableSetOf("z")
        val greekLetter: String = "phi"
        val regularPathQuerySourceVariableAssignment: MutableMap<String, String> =
            mutableMapOf("R1" to "x", "R2" to "y", "R3" to "z")
        val regularPathQueryTargetVariableAssignment: MutableMap<String, String> =
            mutableMapOf("R1" to "z", "R2" to "z", "R3" to "z")
        formula = ConjunctiveFormula(
            existentiallyQuantifiedVariables,
            helperVariables,
            greekLetter,
            regularPathQuerySourceVariableAssignment,
            regularPathQueryTargetVariableAssignment
        )
        return ConjunctiveQueryFileReaderResult(graphMap, formula, mutableListOf(), mutableListOf())
    }

    @Test
    fun readSaneFile() {
        val testFileName = "test_query_sane.txt"
        val result = readWithFileReaderService(testFileName)
        val comparisonResult = constructComparisonResult()

        //expect no warnings, no errors, and a correct database graph
        assert(result.warnings.isEmpty())
        assert(result.errors.isEmpty())
        assert(result == comparisonResult)

    }
}