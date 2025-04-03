package tinDL.services.ontology.OntologyExecutionContext.EL

import org.semanticweb.owlapi.model.*
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.Reasoner.CachingDLReasoner
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.Reasoner.ElkReasoner
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualRestrictionBuilder
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.*
import kotlin.math.pow

class ELSetExecutionContext(private val manager: OntologyManager) : ELExecutionContext {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    override val dlReasoner = ElkReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
//    override val resultGraphReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    override val resultGraphReasoner = dlReasoner
    override val expressionBuilder = manager.getExpressionBuilder();
    override val parser = manager.getQueryParser();
    override val shortFormProvider = manager.getShortFormProvider();
    override val manchesterShortFormProvider = manager.manchesterShortFormProvider;
    override val spaRestrictionBuilder = SingleClassRestrictionBuilder(parser);
    override val spRestrictionBuilder = SingleClassRestrictionBuilder(parser)

    override val individuals = manager.individuals;


    override var tailsetSize = classes.size;

    override fun forEachConcept(action: (SingleClassLoopTableEntryRestriction) -> Unit) {
        classes.forEach { owlClass ->
            action(spaRestrictionBuilder.createConceptNameRestriction(owlClass))
        }
    }

    override fun forEachIndividual(action: (OWLNamedIndividual) -> Unit) {
        for (individual in individuals) {
            action(individual);
        }
    }

    override fun prepareForLoopTableConstruction(prewarmCaches: Boolean){
        if(prewarmCaches) {
            prewarmSubsumptionCache();
        }
    }

    override fun prewarmSubsumptionCache(){
        println("ExecutionContext: Prewarming subsumption Cache...")
        //prewarm property subsumption cache
        classes.forEach { owlClass ->
            properties.forEach { property ->
                val restriction = spaRestrictionBuilder.createConceptNameRestriction(owlClass)
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

    override fun getManager() : OntologyManager {
        return manager;
    }

}