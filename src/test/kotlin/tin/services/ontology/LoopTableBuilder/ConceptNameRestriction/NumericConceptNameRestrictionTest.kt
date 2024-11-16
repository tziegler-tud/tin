package tin.services.ontology.LoopTableBuilder.ConceptNameRestriction


import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.model.OWLClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File

@SpringBootTest
@TestConfiguration
class NumericConceptNameRestrictionTest {    @Autowired
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
//        val exampleFile = readWithFileReaderService("univ-bench.owl.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testNumericConceptRestriction() {
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)

        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC);

        val parser = ec.parser;
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);
        val restrictionBuilder = ec.spaRestrictionBuilder;


        val query = readQueryWithFileReaderService("test1.txt")
        val transducer = readTransducerWithFileReaderService("test1.txt")

        val numericRestrictionBuilder: NumericRestrictionBuilder = NumericRestrictionBuilder(dlReasoner.getTopClassNode().representativeElement, dlReasoner.getBottomClassNode().representativeElement, manager.classes, ec.parser );

        val pasta = parser.getOWLClass("Pasta")!!;
        val bread = parser.getOWLClass("Bread")!!;
        val flour = parser.getOWLClass("Flour")!!;
        val egg = parser.getOWLClass("Egg")!!;
        val chicken = parser.getOWLClass("Chicken")!!;
        val bruschetta = parser.getOWLClass("Bruschetta")!!;
        val carbonara = parser.getOWLClass("Carbonara")!!;
        val vegan = parser.getOWLClass("Vegan")!!;
        val ingredients = parser.getOWLClass("Ingredients")!!;
        val meal = parser.getOWLClass("Meal")!!;
        val restaurant = parser.getOWLClass("Restaurant")!!;

        val classSet = setOf(pasta, bread, flour, egg, chicken, bruschetta, carbonara, vegan, ingredients, meal, restaurant)

//        val res1 = restrictionBuilder.createConceptNameRestriction("Pasta","Bread","Egg");
//        val numericRes1 = numericRestrictionBuilder.createConceptNameRestriction("Pasta","Bread","Egg");
//        assert(res1.asSet() == numericRes1.asSet());

        val allClassRestr = numericRestrictionBuilder.createConceptNameRestriction(classSet);


        //test basic classes
        for (owlClass in classSet) {
            var res = numericRestrictionBuilder.createConceptNameRestriction(owlClass)
            assert(res.containsElement(owlClass))
            assert(res.asSet() == setOf(owlClass));
            assert(res.asList() == listOf(owlClass));

            for (owlClass2 in classSet) {
                assert(res.containsElementFromSet(setOf(owlClass2, owlClass)))
                if(owlClass2 != owlClass) assert(!res.containsElementFromSet(setOf(owlClass2)))

                res.addElement(owlClass2)
                assert(res.containsElement(owlClass))
                assert(res.containsElement(owlClass2))
                assert(res.containsAllElementsFromSet(setOf(owlClass, owlClass2)))
                assert(numericRestrictionBuilder.createConceptNameRestriction(owlClass).isSubsetOf(res))
                assert(res.isSubsetOf(allClassRestr))
                assert(allClassRestr.isSupersetOf(res))
                assert(res.isSupersetOf(numericRestrictionBuilder.createConceptNameRestriction(owlClass)))
                assert(res.asSet() == setOf(owlClass, owlClass2));
                if(owlClass2 != owlClass) res.removeElement(owlClass2)
                assert(res.asSet() == setOf(owlClass));
            }
        }

        for (owlClass in classSet) {
            val seqRes = numericRestrictionBuilder.createConceptNameRestriction(owlClass)
            for (owlClass2 in classSet) {
                val res2 = NumericConceptNameRestriction(seqRes);
                res2.addElement(owlClass2)
                for (owlClass3 in classSet) {
                    val res3 = NumericConceptNameRestriction(res2);
                    res3.addElement(owlClass3)
                    for (owlClass4 in classSet) {
                        val res4 = NumericConceptNameRestriction(res3);
                        res4.addElement(owlClass4)
                        for (owlClass5 in classSet) {
                            val res5 = NumericConceptNameRestriction(res4);
                            res5.addElement(owlClass5)
                            for (owlClass6 in classSet) {
                                val res6 = NumericConceptNameRestriction(res5);
                                res6.addElement(owlClass6)
                                val set: Set<OWLClass> = setOf(owlClass, owlClass2, owlClass3, owlClass4, owlClass5, owlClass6)
                                val joinedRes = numericRestrictionBuilder.createConceptNameRestriction(owlClass, owlClass2, owlClass3, owlClass4, owlClass5, owlClass6);
                                val joinedRes2 = numericRestrictionBuilder.createConceptNameRestriction(set)
                                assert(seqRes.isSubsetOf(res6))
                                assert(res2.isSubsetOf(res6))
                                assert(res3.isSubsetOf(res6))
                                assert(res4.isSubsetOf(res6))
                                assert(res5.isSubsetOf(res6))
                                assert(res6.isSubsetOf(res6))

                                assert(res6 == joinedRes);
                                assert(res6 == joinedRes2);
                                assert(joinedRes == joinedRes2);
                                assert(res6.asSet() == set);
                                assert(joinedRes.asSet() == set);
                                assert(joinedRes2.asSet() == set);
                                assert(res6.containsAllElementsFromSet(set))
                                assert(joinedRes.containsAllElementsFromSet(set))
                                assert(joinedRes2.containsAllElementsFromSet(set))
                            }
                        }
                    }
                }
            }
        }
    }



}