package tinDL.services.ontology.OntologyExecutionContext.ELHI

import org.semanticweb.owlapi.model.*
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.Reasoner.CachingDLReasoner
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.Reasoner.DLReasoner
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualRestrictionBuilder
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassRestrictionBuilderInterface
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import kotlin.math.pow

class ELHINumericExecutionContext(private val manager: OntologyManager) : ELHIExecutionContext {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    override val dlReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    override val resultGraphReasoner = dlReasoner
    override val expressionBuilder = manager.getExpressionBuilder();
    override val parser = manager.getQueryParser();
    override val shortFormProvider = manager.getShortFormProvider();
    override val manchesterShortFormProvider = manager.manchesterShortFormProvider;

    override val individuals = manager.individuals;

    val topClass = dlReasoner.getTopClassNode();
    val bottomClass = dlReasoner.getBottomClassNode();

    override val spaRestrictionBuilder = NumericRestrictionBuilder(topClass.representativeElement, bottomClass.representativeElement, classes, parser);
    override val spRestrictionBuilder = IndividualRestrictionBuilder(parser, shortFormProvider)

    val tailsetMaximum: ULong = pow(2, classes.size)- 1UL;

    override val tailsetSize = tailsetMaximum;

    override fun getManager() : OntologyManager {
        return manager;
    }

    override fun prepareForLoopTableConstruction(prewarmCaches: Boolean){
        if(prewarmCaches) {
            prewarmSubsumptionCache();
        }
    }

    override fun forEachTailset(action: (MultiClassLoopTableEntryRestriction) -> Unit) {
        for (i in 1UL..tailsetMaximum)
        {
            action(spaRestrictionBuilder.createConceptNameRestriction(i));
        }
    }

    override fun forEachTailsetDescending(action: (MultiClassLoopTableEntryRestriction) -> Unit) {
//        for (i in tailsetMaximum downTo 1UL)
//        {
//            action(spaRestrictionBuilder.createConceptNameRestriction(i));
//        }

        val maxOnes = tailsetMaximum.countOneBits() // Maximum number of 1s in binary representation up to n

        for (ones in maxOnes downTo 1) {
            for (i in 1UL..tailsetMaximum) {
                if (i.countOneBits() == ones) {
//                    println("Number: $i, Binary: ${i.toString(2)}, 1s: ${i.countOneBits()}")
                    action(spaRestrictionBuilder.createConceptNameRestriction(i));
                }
            }
        }
    }

    fun forEachTailsetDescendingNumeric(action: (NumericConceptNameRestriction) -> Unit) {
        for (i in tailsetMaximum downTo 1UL)
        {
            action(spaRestrictionBuilder.createConceptNameRestriction(i));
        }
    }

    override fun forEachIndividual(action: (OWLNamedIndividual) -> Unit) {
        for (individual in individuals) {
            action(individual);
        }
    }

    override fun prewarmSubsumptionCache(){
        println("ExecutionContext: Prewarming subsumption Cache...")
        //prewarm property subsumption cache
        for (i in 1UL..tailsetMaximum)
        {
            properties.forEach { property ->
                val restriction = spaRestrictionBuilder.createConceptNameRestriction(i)
                val classExp = spaRestrictionBuilder.asClassExpression(restriction)
                val rM1 = expressionBuilder.createExistentialRestriction(property, classExp)
                val rM1Exp = expressionBuilder.createELHIExpression(rM1);
                dlReasoner.calculateSubClasses(rM1Exp)
            }
        }
        println("ExecutionContext: Prewarming subsumption Cache finished. Cache size: ${dlReasoner.subClassCache.size}");
    }

    override fun getClasses(): Set<OWLClass> {
        return classes;
    }

    override fun getClassAmount(): Int {
        return classes.size;
    }

    override fun getClassNames(): HashSet<String> {
        return manager.getClassNames();
    }

    override fun getRoleNames(): HashSet<String> {
        return manager.getRoleNames();
    }

    override fun getRoles(): Set<OWLObjectProperty> {
        return manager.getRoles();
    }

    private fun pow(n: Int, exp: Int): ULong{
        return n.toDouble().pow(exp).toULong()
    }
}