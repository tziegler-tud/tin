package tin.services.ontology.OntologyExecutionContext

import org.semanticweb.owlapi.model.*
import tin.services.ontology.Reasoner.CachingDLReasoner
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.NumericRestrictionBuilder
import kotlin.math.pow

class ELHIExecutionContext(private val manager: OntologyManager) : ExecutionContext {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    override val dlReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    override val expressionBuilder = manager.getExpressionBuilder();
    override val parser = manager.getQueryParser();
    override val shortFormProvider = manager.getShortFormProvider();
    override val manchesterShortFormProvider = manager.manchesterShortFormProvider;
    override val restrictionBuilder = NumericRestrictionBuilder(classes, parser);

    val tailsetMaximum: ULong = pow(2, classes.size);

    override val tailsetSize = tailsetMaximum;

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false){
        if(prewarmCaches) {
            prewarmSubsumptionCache();
        }
    }

    override fun forEachTailset(action: (LoopTableEntryRestriction<OWLClass>) -> Unit) {
        for (i in 1UL..tailsetMaximum)
        {
            action(restrictionBuilder.createConceptNameRestriction(i));
        }
    }

    fun forEachTailsetDescending(action: (LoopTableEntryRestriction<OWLClass>) -> Unit) {
        for (i in tailsetMaximum downTo 1UL)
        {
            action(restrictionBuilder.createConceptNameRestriction(i));
        }
    }

    override fun prewarmSubsumptionCache(){
        println("ExecutionContext: Prewarming subsumption Cache...")
        //prewarm property subsumption cache
        for (i in 1UL..tailsetMaximum)
        {
            properties.forEach { property ->
                val restriction = restrictionBuilder.createConceptNameRestriction(i)
                val classExp = restrictionBuilder.asClassExpression(restriction)
                val rM1 = expressionBuilder.createExistentialRestriction(property, classExp)
                val rM1Exp = expressionBuilder.createELHIExpression(rM1);
                dlReasoner.calculateSubClasses(rM1Exp)
            }
        }
        println("ExecutionContext: Prewarming subsumption Cache finished. Cache size: ${dlReasoner.subClassCache.size}");
    }

    override fun getClasses(): Set<OWLEntity> {
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