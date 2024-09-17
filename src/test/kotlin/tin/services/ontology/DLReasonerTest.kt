package tin.services.ontology

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.math.exp

@SpringBootTest
@TestConfiguration
class DLReasonerTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadExampleOntology(testOntologyFileName: String) : OntologyManager {
        val exampleFile = readWithFileReaderService(testOntologyFileName).get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testSubsumptionCheck(){
        val manager = loadExampleOntology("pizza2.rdf");
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);


        val l1 = expressionBuilder.createELHExpressionFromString("Bread")
        val l2 = expressionBuilder.createELHExpressionFromString("Pasta")
        val r1 = expressionBuilder.createELHExpressionFromString("contains some Flour");
        val r2 = expressionBuilder.createELHExpressionFromString("contains some Chicken");

        assert(dlReasoner.checkIsSubsumed(l1, r1));
        assert(dlReasoner.checkIsSubsumed(l2, r1));
        assert(!dlReasoner.checkIsSubsumed(l1, r2));
        assert(!dlReasoner.checkIsSubsumed(l2, r2));
    }

    @Test
    fun testSuperPropertiesCalculation(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val parser = manager.getQueryParser();


        val r = parser.getOWLObjectProperty("r")!!;
        val r1 = parser.getOWLObjectProperty("r1")!!;
        val r11 = parser.getOWLObjectProperty("r11")!!;
        val r111 = parser.getOWLObjectProperty("r111")!!;

        val superProperties = dlReasoner.calculateSuperProperties(r111);

        val r1111 = parser.getOWLObjectProperty("r1111")!!;
        val s = parser.getOWLObjectProperty("s")!!;
        val t = parser.getOWLObjectProperty("t")!!;

        assert(superProperties.count() == 4);
        assert(superProperties.containsEntity(r))
        assert(superProperties.containsEntity(r1))
        assert(superProperties.containsEntity(r11))
        assert(!superProperties.containsEntity(r111))
        assert(!superProperties.containsEntity(r1111))
        assert(!superProperties.containsEntity(s))
        assert(!superProperties.containsEntity(t))

        val abc = parser.getOWLObjectProperty("rst")!!;
        val superABC = dlReasoner.calculateSuperProperties(abc);
        assert(superABC.count() == 4);
        assert(superABC.containsEntity(r));
        assert(superABC.containsEntity(s));
        assert(superABC.containsEntity(t));
    }

    @Test
    fun testInverseSuperPropertiesCalculation(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val parser = manager.getQueryParser();


        // we have:
        // r1 <= r
        // u1 <= u
        // u <= r(-)
        // u1 <= r1(-)

        val r = parser.getOWLObjectProperty("r")!!;
        val r1 = parser.getOWLObjectProperty("r1")!!;
        val u = parser.getOWLObjectProperty("u")!!;
        val u1 = parser.getOWLObjectProperty("u1")!!;

        val superPropertiesR1 = dlReasoner.calculateSuperProperties(r1);

        val s = parser.getOWLObjectProperty("s")!!;
        val t = parser.getOWLObjectProperty("t")!!;

        assert(superPropertiesR1.count() == 2);
        assert(superPropertiesR1.containsEntity(r))

        val invSuperPropertiesR1 = dlReasoner.calculateSuperProperties(r1.getInverseProperty());

        //expect:
        // r1(-) <= r(-)

        assert(invSuperPropertiesR1.containsEntity(r.inverseProperty))
        assert(!invSuperPropertiesR1.containsEntity(r))
        assert(!invSuperPropertiesR1.containsEntity(t))


        //expect:
        // u1 <= u
        // u1 <= r1(-)
        // u1 <= r(-) because r1 <= r
        val superPropertiesU1 = dlReasoner.calculateSuperProperties(u1);
        assert(superPropertiesU1.containsEntity(u))
        assert(superPropertiesU1.containsEntity(r1.inverseProperty))
        assert(superPropertiesU1.containsEntity(r.inverseProperty))

        //expect:
        // u1(-) <= u(-)
        // u1(-) <= r1
        // u1(-) <= r because r1 <= r
        val invSuperPropertiesU1 = dlReasoner.calculateSuperProperties(u1.getInverseProperty());
        assert(invSuperPropertiesU1.containsEntity(u.inverseProperty))
        assert(invSuperPropertiesU1.containsEntity(r1))
        assert(invSuperPropertiesU1.containsEntity(r))

    }

    @Test
    fun testSubPropertiesCalculation(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val parser = manager.getQueryParser();


        val r = parser.getOWLObjectProperty("r")!!;
        val r1 = parser.getOWLObjectProperty("r1")!!;
        val r11 = parser.getOWLObjectProperty("r11")!!;
        val r12 = parser.getOWLObjectProperty("r12")!!;
        val r111 = parser.getOWLObjectProperty("r111")!!;
        val r1111 = parser.getOWLObjectProperty("r1111")!!;
        val r112 = parser.getOWLObjectProperty("r112")!!;
        val s = parser.getOWLObjectProperty("s")!!;
        val t = parser.getOWLObjectProperty("t")!!;
        val u = parser.getOWLObjectProperty("u")!!;
        val u1 = parser.getOWLObjectProperty("u1")!!;


        val subPropertiesA11 = dlReasoner.calculateSubProperties(r11);

        assert(subPropertiesA11.count() == 4);
        assert(subPropertiesA11.containsEntity(r111))
        assert(subPropertiesA11.containsEntity(r112))
        assert(subPropertiesA11.containsEntity(r1111))
        assert(!subPropertiesA11.containsEntity(r))
        assert(!subPropertiesA11.containsEntity(r1))
        assert(!subPropertiesA11.containsEntity(r11))
        assert(!subPropertiesA11.containsEntity(s))
        assert(!subPropertiesA11.containsEntity(t))

        val rst = parser.getOWLObjectProperty("rst")!!;
        val subRST = dlReasoner.calculateSubProperties(rst);
        assert(subRST.count() == 1);
        assert(subRST.isBottomSingleton());

        val subA = dlReasoner.calculateSubProperties(r);
        assert(subA.containsEntity(r1));
        assert(subA.containsEntity(rst));

        val subR1 = dlReasoner.calculateSubProperties(r1);
        assert(subR1.containsEntity(r11));
        assert(subR1.containsEntity(r12));
        assert(subR1.containsEntity(r111));
        assert(subR1.containsEntity(r112));
        assert(subR1.containsEntity(r1111));
        assert(subR1.containsEntity(u1.inverseProperty));
    }

    @Test
    fun testTopConceptExpressions(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val restrictionBuilder = manager.getRestrictionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val parser = manager.getQueryParser();

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction = restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        assert(topClassNode.isTopNode);

        val r = parser.getOWLObjectProperty("r")!!;
        val someRExpression = expressionBuilder.createExistentialRestriction(r, topClassNode.representativeElement)
        val someR = dlReasoner.reasoner.getSubClasses(someRExpression);

        val A = parser.getOWLClass("A")!!;
        val D = parser.getOWLClass("D")!!;

        assert(someR.containsEntity(A))
        assert(someR.containsEntity(D))
    }



}