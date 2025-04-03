package tinDL.services.ontology.OntologyExecutionContext.ELHI

import org.semanticweb.owlapi.model.*
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.Reasoner.CachingDLReasoner
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.RestrictionBuilder
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualRestrictionBuilder
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassRestrictionBuilderInterface
import kotlin.math.pow

class ELHISetExecutionContext(private val manager: OntologyManager) : ELHIExecutionContext {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    override val dlReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    override val resultGraphReasoner = dlReasoner
    override val expressionBuilder = manager.getExpressionBuilder();
    override val parser = manager.getQueryParser();
    override val shortFormProvider = manager.getShortFormProvider();
    override val manchesterShortFormProvider = manager.manchesterShortFormProvider;
    override val spaRestrictionBuilder = RestrictionBuilder(parser, shortFormProvider);
    override val spRestrictionBuilder = IndividualRestrictionBuilder(parser, shortFormProvider)

    override val individuals = manager.individuals;



    var tailsets: HashSet<HashSet<String>>? = hashSetOf();
    var tailsetsAsClasses: HashSet<HashSet<OWLClass>> = hashSetOf();

    override var tailsetSize = 0UL


    override fun forEachTailset(action: (MultiClassLoopTableEntryRestriction) -> Unit) {
        tailsetsAsClasses.forEach { tailset ->
            action(spaRestrictionBuilder.createConceptNameRestriction(tailset))
        }
    }

    override fun forEachTailsetDescending(action: (MultiClassLoopTableEntryRestriction) -> Unit) {
        forEachTailset(action)
    }

    override fun forEachIndividual(action: (OWLNamedIndividual) -> Unit) {
        for (individual in individuals) {
            action(individual);
        }
    }

    override fun prepareForLoopTableConstruction(prewarmCaches: Boolean){
        //create a new instance
        tailsets = computeTailSets();
        tailsetsAsClasses = computeTailSetsAsOWLClass();
        //nothing to do for now
        tailsetSize = tailsets!!.size.toULong();

        if(prewarmCaches) {
            prewarmSubsumptionCache();
        }
    }

    override fun prewarmSubsumptionCache(){
        println("ExecutionContext: Prewarming subsumption Cache...")
        //prewarm property subsumption cache
        tailsetsAsClasses.forEach { tailset ->
            properties.forEach { property ->
                val restriction = spaRestrictionBuilder.createConceptNameRestriction(tailset)
                val classExp = spaRestrictionBuilder.asClassExpression(restriction)
                val rM1 = expressionBuilder.createExistentialRestriction(property, classExp)
                val rM1Exp = expressionBuilder.createELHIExpression(rM1);
                dlReasoner.calculateSubClasses(rM1Exp)
            }
        }
        println("ExecutionContext: Prewarming subsumption Cache finished. Cache size: ${dlReasoner.subClassCache.size}");
    }



    private fun powerSet(originalSet: Set<OWLClass>): HashSet<HashSet<OWLClass>> {
        // Start with the empty set
        val powerSet = HashSet<HashSet<OWLClass>>()
        powerSet.add(HashSet())

        // Iterate over each element in the original set
        for (element in originalSet) {
            val newSubsets = HashSet<HashSet<OWLClass>>()

            // For each subset in the current powerSet, add the current element
            for (subset in powerSet) {
                val newSubset = HashSet(subset)
                newSubset.add(element)
                newSubsets.add(newSubset)
            }

            // Add the newly created subsets to the powerset
            powerSet.addAll(newSubsets)
        }
        return powerSet
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

    private fun computeTailSets(): HashSet<HashSet<String>>{
        //powerset of all concept names in the ontology
        val classes = manager.getClassNames();
        val amount = 2.0.pow(classes.size.toDouble()) +1;

        val powerset = powerSet(classes)
        //remove empty set
        powerset.remove(HashSet())
        return powerset;
    }

    private fun computeTailSetsAsOWLClass(): HashSet<HashSet<OWLClass>>{
        //powerset of all concept names in the ontology
        val classes = manager.classes;
        val amount = 2.0.pow(classes.size.toDouble()) +1;

        val powerset = powerSet(classes)
        //remove empty set
        powerset.remove(HashSet())
        return powerset;
    }

    private fun powerSet(originalSet: HashSet<String>): HashSet<HashSet<String>> {
        // Start with the empty set
        val powerSet = HashSet<HashSet<String>>()
        powerSet.add(HashSet())

        // Iterate over each element in the original set
        for (element in originalSet) {
            val newSubsets = HashSet<HashSet<String>>()

            // For each subset in the current powerSet, add the current element
            for (subset in powerSet) {
                val newSubset = HashSet(subset)
                newSubset.add(element)
                newSubsets.add(newSubset)
            }

            // Add the newly created subsets to the powerset
            powerSet.addAll(newSubsets)
        }
        return powerSet
    }

}