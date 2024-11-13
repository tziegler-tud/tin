package tin.services.ontology.OntologyExecutionContext.ELHI

import org.semanticweb.owlapi.model.*
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.Reasoner.CachingDLReasoner
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tin.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualRestrictionBuilder
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassRestrictionBuilderInterface
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import kotlin.math.pow

class ELHINumericExecutionContext(private val manager: OntologyManager) : ELHIExecutionContext {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    override val dlReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    override val expressionBuilder = manager.getExpressionBuilder();
    override val parser = manager.getQueryParser();
    override val shortFormProvider = manager.getShortFormProvider();
    override val manchesterShortFormProvider = manager.manchesterShortFormProvider;
    override val spaRestrictionBuilder = NumericRestrictionBuilder(classes, parser);
    override val spRestrictionBuilder = IndividualRestrictionBuilder(parser, shortFormProvider)

    val tailsetMaximum: ULong = pow(2, classes.size)- 1UL;

    override val tailsetSize = tailsetMaximum;

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
        for (i in tailsetMaximum downTo 1UL)
        {
            action(spaRestrictionBuilder.createConceptNameRestriction(i));
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