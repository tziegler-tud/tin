package tin.services.ontology.OntologyExecutionContext

import org.semanticweb.owlapi.model.*
import tin.services.ontology.Reasoner.CachingDLReasoner
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericRestrictionBuilder
import kotlin.math.pow

class OntologyExecutionContextNumeric(private val manager: OntologyManager) {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;
    private var properties = manager.properties;
    val dlReasoner = CachingDLReasoner(manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT), manager.getExpressionBuilder())
    val expressionBuilder = manager.getExpressionBuilder();
    val parser = manager.getQueryParser();
    val shortFormProvider = manager.getShortFormProvider();
    val manchesterShortFormProvider = manager.manchesterShortFormProvider;
    val restrictionBuilder = NumericRestrictionBuilder(classes, parser);


    var tailsets: HashSet<HashSet<String>>? = hashSetOf();
    var tailsetsAsClasses: HashSet<HashSet<OWLClass>> = hashSetOf();

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false){
        //create a new instance
        tailsets = computeTailSets();
        tailsetsAsClasses = computeTailSetsAsOWLClass();
        //nothing to do for now

        if(prewarmCaches) {
            prewarmSubsumptionCache();
        }
    }

    fun prewarmSubsumptionCache(){
        println("ExecutionContext: Prewarming subsumption Cache...")
        //prewarm property subsumption cache
        tailsetsAsClasses.forEach { tailset ->
            properties.forEach { property ->
                val restriction = restrictionBuilder.createConceptNameRestriction(tailset)
                val classExp = restrictionBuilder.asClassExpression(restriction)
                val rM1 = expressionBuilder.createExistentialRestriction(property, classExp)
                val rM1Exp = expressionBuilder.createELHIExpression(rM1);
                dlReasoner.calculateSubClasses(rM1Exp)
            }
        }
        println("ExecutionContext: Prewarming subsumption Cache finished. Cache size: ${dlReasoner.subClassCache.size}");
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

    fun getClasses(): Set<OWLEntity> {
        return classes;
    }

    fun getClassAmount(): Int {
        return classes.size;
    }

    fun getClassNames(): HashSet<String> {
        return manager.getClassNames();
    }

    fun getRoleNames(): HashSet<String> {
        return manager.getRoleNames();
    }

    fun getRoles(): Set<OWLObjectProperty> {
        return manager.getRoles();
    }


}