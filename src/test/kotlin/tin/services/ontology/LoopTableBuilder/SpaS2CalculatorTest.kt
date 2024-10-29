package tin.services.ontology.LoopTableBuilder

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS2Calculator
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpaS2CalculatorTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readQueryWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readTransducerWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadExampleOntology() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza2.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testCalculation(){
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!

        val M = restrictionBuilder.createConceptNameRestriction("Egg")

        val entry = SPALoopTableEntry(Pair(s1,t0), Pair(s2,t0),M);
        val entry2 = SPALoopTableEntry(Pair(s2,t0), Pair(s3,t0),M);
        //create empty loop table
        val table: SPALoopTable = SPALoopTable();

        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val result = s2Calculator.calculate(entry, table);
        //use s1->Chicken?->s2 with t0-Chicken?|Egg?|2->t0
        assert(result == 2); // 2 + 2 + 3
        table.set(entry, result!!);

        assert(table.get(entry) == result)

        //calculate spa[(s2,t0),(s3,t0),{Egg}]
        val result2 = s2Calculator.calculate(entry2, table);
        //use s2->contains->s3 with t0-contains|Ingredient?|6->t0
        assert(result2 == 6); // 2 + 0 + 3
        table.set(entry2, result2!!);
        assert(table.get(entry2) == result2)
    }

    @Test
    fun testCalculationV2(){
        val exampleFile = readWithFileReaderService("pizza_small.rdf").get()
        val manager = OntologyManager(exampleFile);
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!

        val classNames = ec.getClassNames();

        val M = restrictionBuilder.createConceptNameRestriction("Egg")

        //create empty loop table
        val table: SPALoopTable = SPALoopTable();
        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val table2 = s2Calculator.calculateAll(table);
        //use s1->Chicken?->s2 with t0-Chicken?|Egg?|2->t0
        val filtered = table2.getWithRestriction(M);

        for (tailset in ec.tailsets!!) {
            if(dlReasoner.checkIsSubsumed(expressionBuilder.createELHIExpression(queryParser.fromClassNames(tailset)), expressionBuilder.createELHExpressionFromString("Egg"))) {
                assert(table2.get(SPALoopTableEntry(Pair(s1,t0), Pair(s2,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == 2)
            }
            else {
                if(dlReasoner.checkIsSubsumed(expressionBuilder.createELHIExpression(queryParser.fromClassNames(tailset)), expressionBuilder.createELHExpressionFromString("Ingredients"))) {
                    assert(table2.get(SPALoopTableEntry(Pair(s0,t0), Pair(s1,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == 6)
                    assert(table2.get(SPALoopTableEntry(Pair(s2,t0), Pair(s3,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == 6)
                }
                else {
                    assert(table2.get(SPALoopTableEntry(Pair(s0,t0), Pair(s1,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
                    assert(table2.get(SPALoopTableEntry(Pair(s1,t0), Pair(s2,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
                    assert(table2.get(SPALoopTableEntry(Pair(s2,t0), Pair(s3,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
                }
            }
            assert(table2.get(SPALoopTableEntry(Pair(s0,t0), Pair(s2,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
            assert(table2.get(SPALoopTableEntry(Pair(s0,t0), Pair(s3,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
            assert(table2.get(SPALoopTableEntry(Pair(s1,t0), Pair(s3,t0),restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset))) == null)
        }
    }

    @Test
    fun testCalculationAllEntries(){
        val manager = loadExampleOntology();
        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_2.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val dlReasoner = ec.dlReasoner;
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;

        val pairsAvailable = mutableSetOf<Pair<Node, Node>>();
        val tailsets = ec.tailsets!!;

        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        val table: SPALoopTable = SPALoopTable();

        val timeSource = TimeSource.Monotonic

        query.graph.nodes.forEach { node ->
            transducer.graph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
            }
        }

        val v1StartTime = timeSource.markNow()
        pairsAvailable.forEach{ source ->
            pairsAvailable.forEach target@{ target ->
                if(source.first == target.first && source.second == target.second) return@target;
                tailsets.forEach tailset@{ tailset ->
                    //foreach (p,q,M) do:
                    val restriction = restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset)
                    val entry = SPALoopTableEntry(source, target, restriction)
                    // dont add table entries (q,t)(q,t),_
                    val updatedValue = s2Calculator.calculate(entry, table)
                    if(updatedValue !== null) {
                        table.set(entry, updatedValue );
                    }
                }
            }
        };

        val v1EndTime = timeSource.markNow()
        val v1Time = v1EndTime - v1StartTime;
        println("V1 Time: " + v1Time)

    }

    @Test
    fun testCalculationAllEntriesCompare(){
        val manager = loadExampleOntology();
        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_3.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_3.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val dlReasoner = ec.dlReasoner;
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;

        val pairsAvailable = mutableSetOf<Pair<Node, Node>>();
        val tailsets = ec.tailsets!!;

        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        val table: SPALoopTable = SPALoopTable();
        var table2: SPALoopTable = SPALoopTable();

        val timeSource = TimeSource.Monotonic

        query.graph.nodes.forEach { node ->
            transducer.graph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
            }
        }

        val v1StartTime = timeSource.markNow()
        pairsAvailable.forEach{ source ->
            pairsAvailable.forEach target@{ target ->
                if(source.first == target.first && source.second == target.second) return@target;
                tailsets.forEach tailset@{ tailset ->
                    //foreach (p,q,M) do:
                    val restriction = restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset)
                    val entry = SPALoopTableEntry(source, target, restriction)
                    // dont add table entries (q,t)(q,t),_
                    val updatedValue = s2Calculator.calculate(entry, table)
                    if(updatedValue !== null) {
                        table.set(entry, updatedValue );
                    }
                }
            }
        };
        val v1EndTime = timeSource.markNow()
        dlReasoner.clearCache();

        /**
         * v3
         */
        val v2StartTime = timeSource.markNow()
        table2 = s2Calculator.calculateAll(table2)
        val v2EndTime = timeSource.markNow()

        val v1Time = v1EndTime - v1StartTime;
        val v2Time = v2EndTime - v2StartTime;

        println("V1 Time: " + v1Time)
        println("V2 Time: " + v2Time)

        assert(table == table2);


    }

}