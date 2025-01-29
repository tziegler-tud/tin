package tin.services.ontology

import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLPropertyExpression
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.NodeSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.Reasoner.ElkReasoner
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.RestrictionBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File

@SpringBootTest
@TestConfiguration
class DLReasonerTest {
    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

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
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI);

        val dlReasoner = ec.dlReasoner;
        val expressionBuilder = ec.expressionBuilder;
        val restrictionBuilder = ec.spaRestrictionBuilder;
        val parser = ec.parser;

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
    fun testSubsumptionCheckTopElement(){
        val manager = loadExampleOntology("pizza2.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI);
        val expressionBuilder = ec.expressionBuilder;
        val restrictionBuilder = ec.spaRestrictionBuilder;
        val dlReasoner = ec.dlReasoner;


        val l1 = expressionBuilder.createELHExpressionFromString("Bread")
        val l2 = expressionBuilder.createELHExpressionFromString("Pasta")
        val l3 = expressionBuilder.createELHExpressionFromString("Bruschetta")
        val l4 = expressionBuilder.createELHExpressionFromString("Carbonara")
        val l5 = expressionBuilder.createELHExpressionFromString("Chicken")
        val l6 = expressionBuilder.createELHExpressionFromString("Egg")
        val l7 = expressionBuilder.createELHExpressionFromString("Flour")

        val n1 = expressionBuilder.createELHExpressionFromString("Gluten")
        val n2 = expressionBuilder.createELHExpressionFromString("Vegan")
        val n3 = expressionBuilder.createELHExpressionFromString("Restaurant")

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction = restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)
        val role = ec.parser.getOWLObjectProperty("contains")!!

        val M1ClassExp = restrictionBuilder.asClassExpression(owlTopClassRestriction);
        val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
        val rM1Exp = expressionBuilder.createELHIExpression(rM1);

        assert(dlReasoner.checkIsSubsumed(l1, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l2, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l3, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l4, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l5, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l6, rM1Exp))
        assert(dlReasoner.checkIsSubsumed(l7, rM1Exp))

        assert(!dlReasoner.checkIsSubsumed(n1, rM1Exp))
        assert(!dlReasoner.checkIsSubsumed(n2, rM1Exp))
        assert(!dlReasoner.checkIsSubsumed(n3, rM1Exp))
    }

    @Test
    fun testSuperPropertiesCalculation(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

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
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

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
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

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
    fun testSubPropertiesCalculationELK(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.ELK)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = ElkReasoner(reasoner, expressionBuilder);

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

        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.SAME_INDIVIDUAL)
        assert(dlReasoner.checkPropertySubsumption(r1,r))
        assert(dlReasoner.checkPropertySubsumption(r1,r1))

    }

    @Test
    fun testTopConceptExpressions(){
        val manager = loadExampleOntology("propertyTest2.rdf");
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val restrictionBuilder = RestrictionBuilder(manager.getQueryParser(), manager.getShortFormProvider())
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

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

    @Test
    fun testCalculateClassSubsumees() {
        val manager = loadExampleOntology("pizza2_test.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI)
        val dlReasoner = ec.dlReasoner;
        val expressionBuilder = ec.expressionBuilder
        val restrictionBuilder = ec.spaRestrictionBuilder
        val queryParser = ec.parser

        val parser = manager.getQueryParser();

        val role = queryParser.getOWLObjectProperty("contains")!!

        val res1 = restrictionBuilder.createConceptNameRestriction("Pasta","Bread","Egg");
        val class1 = restrictionBuilder.asClassExpression(res1);
        val r1Exp = expressionBuilder.createExistentialRestriction(role, class1)
        val atomicSubClasses = dlReasoner.calculateSubClasses(expressionBuilder.createELHIExpression(r1Exp))
        assert(atomicSubClasses.isEmpty());

        val res2 = restrictionBuilder.createConceptNameRestriction("Flour","Egg");
        val class2 = restrictionBuilder.asClassExpression(res2);
        val r2Exp = expressionBuilder.createExistentialRestriction(role, class2)
        val atomicSubClasses2 = dlReasoner.calculateSubClasses(expressionBuilder.createELHIExpression(r2Exp))
        assert(atomicSubClasses2.size == 1)
        assert(atomicSubClasses2.contains(queryParser.getOWLClass("Pasta")))
    }

    @Test
    fun testCachePrewarming() {
        val manager = loadExampleOntology("pizza2_test.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI, true);

        val dlReasoner = ec.dlReasoner;
        val stats = dlReasoner.getStats()

        //cache size should be |roles| x |tailsets|
        assert(stats["subClassCache"]!! == ec.getRoles().size * ec.tailsetSize.toInt());
        //cache hits should be 0
        assert(stats["subClassCacheHitCounter"]!! == 0);
    }

    @Test
    fun testGetIndividualClasses() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
        val dlReasoner = ec.dlReasoner;

        ec.forEachIndividual { individual ->
            val classes = dlReasoner.getClasses(individual)
            var stringNames: MutableSet<String> = mutableSetOf()
            val combinedRestriction = ec.spaRestrictionBuilder.createConceptNameRestriction(setOf());

            classes.forEach { className ->
                className.entities().forEach{
                    combinedRestriction.addElement(it);
                    stringNames.add(ec.shortFormProvider.getShortForm(it));
                }
            }
            println("Individual ${ec.shortFormProvider.getShortForm(individual)}: " + stringNames)

            //verify
            ec.getClasses().forEach { owlClass ->
                val restriction = ec.spaRestrictionBuilder.createConceptNameRestriction(combinedRestriction.asSet())
                if(restriction.containsElement(owlClass)) return@forEach
                restriction.addElement(owlClass);
                val isSubsumed  = dlReasoner.checkIndividualEntailment(individual, ec.expressionBuilder.createELHIExpression(ec.spaRestrictionBuilder.asClassExpression(restriction)))
                assert(!isSubsumed);
            }
        }
    }

    @Test
    fun testGetConnectedIndividuals() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
        val dlReasoner = ec.dlReasoner;

        val resultMap: MutableMap<Pair<OWLNamedIndividual, OWLObjectProperty>, NodeSet<OWLNamedIndividual> > = hashMapOf();

        ec.forEachIndividual { individual ->
            ec.getRoles().forEach { owlObjectProperty ->
                val result = dlReasoner.getConnectedIndividuals(owlObjectProperty, individual);
                val pair = Pair(individual, owlObjectProperty);
                resultMap[pair] = result
            }
        }

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!

        //verify
        assert(resultMap[Pair(place1, serves_drink)]!!.containsEntity(beer))
        assert(resultMap[Pair(place1, serves_drink)]!!.isSingleton)

        assert(resultMap[Pair(place1, serves_meal)]!!.containsEntity(carbonara) )
        assert(resultMap[Pair(place1, serves_meal)]!!.isSingleton)

        assert(resultMap[Pair(place1, serves)]!!.containsEntity(beer) )
        assert(resultMap[Pair(place1, serves)]!!.containsEntity(carbonara) )

        assert(resultMap[Pair(r, serves)]!!.containsEntity(bruschetta) )
        assert(resultMap[Pair(r, serves)]!!.containsEntity(carbonara) )

        assert(resultMap[Pair(veganPlace, serves)]!!.containsEntity(bruschetta) )


    }
}