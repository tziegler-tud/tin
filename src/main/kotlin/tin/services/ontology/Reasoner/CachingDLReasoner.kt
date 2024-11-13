package tin.services.ontology.Reasoner

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLPropertyExpression
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import kotlin.streams.toList

/**
 * wrapper class to provide some high-level utility using the owl reasoner interface
 */
class CachingDLReasoner(
    val reasoner: OWLReasoner,
    private val expressionBuilder: DLExpressionBuilder
) : AbstractReasoner() {
    public val superClassCache: HashMap<DLExpression, NodeSet<OWLClass>> = hashMapOf()
    public val equivalentClassCache: HashMap<DLExpression, Node<OWLClass>> = hashMapOf()
    public val subClassCache: HashMap<DLExpression, HashSet<OWLClass>> = hashMapOf()
    public val propertySubsumptionCache: HashMap<Pair<OWLPropertyExpression, OWLPropertyExpression>, Boolean> = hashMapOf()
    public val entailmentCache: HashMap<Pair<DLExpression, DLExpression>, Boolean> = hashMapOf()

    public var superClassCacheHitCounter = 0;
    public var equivNodeCacheHitCounter = 0;
    public var subClassCacheHitCounter = 0;
    public var propertySubsumptionCacheHitCounter = 0;
    public var entailmentCacheHitCounter = 0;
    public var entailmentCacheMissCounter = 0;

    override fun checkIsSubsumed(expr: DLExpression, superExpression: DLExpression): Boolean {
        val cacheEntry = entailmentCache[Pair(expr, superExpression)];
        if(cacheEntry != null){
            entailmentCacheHitCounter++;
            return cacheEntry
        }
        entailmentCacheMissCounter++;
        val subex = expressionBuilder.createSubsumptionExpression(expr, superExpression);
        val isEntailed = reasoner.isEntailed(subex);
        entailmentCache[Pair(expr, superExpression)] = isEntailed;
        return isEntailed;
    }

    override fun checkPropertySubsumption(property: OWLObjectPropertyExpression, superProperty: OWLObjectPropertyExpression): Boolean {
        val cacheEntry = propertySubsumptionCache[Pair(property, superProperty)];
        if(cacheEntry != null){
            propertySubsumptionCacheHitCounter++;
            return cacheEntry
        }
        val subex = expressionBuilder.createPropertySubsumptionExpression(property, superProperty);
        val isEntailed = reasoner.isEntailed(subex);
        propertySubsumptionCache[Pair(property, superProperty)] = isEntailed;
        return isEntailed;
    }

    override fun calculateSuperProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSuperObjectProperties(property, false);
        return properties;
    }


    override fun calculateSuperPropertiesMock(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        return OWLObjectPropertyNodeSet(property)
    }

    override fun calculateSubProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSubObjectProperties(property, false);
        return properties;
    }

    override fun calculateSubClasses(expr: DLExpression, includeNothing: Boolean): HashSet<OWLClass> {
        val cacheEntry = subClassCache[expr];
        if(cacheEntry != null){
            subClassCacheHitCounter++;
            return cacheEntry
        }
        val exp = expr.getClassExpression()
        var classes = reasoner.getSubClasses(exp, false)
        //remove owl:Nothing
        if(!includeNothing && classes.isBottomSingleton) {
            classes = OWLClassNodeSet();
        }
        if(!includeNothing) {
            classes.removeAll { it.isBottomNode }
        }

        val hashSet = classes.entities().toList().toHashSet()
        subClassCache[expr] = hashSet;
        return hashSet;
    }

    override fun getTopClassNode(): Node<OWLClass> {
        return reasoner.topClassNode;
    }

    override fun getBottomClassNode(): Node<OWLClass> {
        return reasoner.bottomClassNode;
    }

    override fun calculateSuperClasses(expr: DLExpression, includeEquivalent: Boolean): NodeSet<OWLClass> {
        val strictSuperClasses: NodeSet<OWLClass>;
        if(superClassCache.containsKey(expr)) {
            strictSuperClasses = superClassCache[expr]!!;
            superClassCacheHitCounter++;
        }
        else{
            strictSuperClasses = reasoner.getSuperClasses(expr.getClassExpression());
            superClassCache[expr] = strictSuperClasses;
        }
        if(includeEquivalent) {
            val equivNode: Node<OWLClass>
            if(equivalentClassCache.containsKey(expr)) {
                equivNode = equivalentClassCache[expr]!!;
                equivNodeCacheHitCounter++;
            }
            else{
                equivNode = reasoner.getEquivalentClasses(expr.getClassExpression());
                equivalentClassCache[expr] = equivNode;
            }
            val setWithEquiv = OWLClassNodeSet(strictSuperClasses.nodes)
            setWithEquiv.addNode(equivNode);
            return setWithEquiv;
        }
        return strictSuperClasses;
    }

    override fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass> {
        return reasoner.getEquivalentClasses(expr.getClassExpression());
    }

    override fun clearCache() {
        superClassCache.clear();
        equivalentClassCache.clear();
        subClassCache.clear();
        propertySubsumptionCache.clear();
        entailmentCache.clear();
    }

    override fun getStats(): Map<String, Int> {
        val map = mutableMapOf<String, Int>();
        map["superClassCache"] = superClassCache.size;
        map["equivalentClassCache"] = equivalentClassCache.size;
        map["subClassCache"] = subClassCache.size;
        map["propertySubsumptionCache"] = propertySubsumptionCache.size;
        map["entailmentCache"] = entailmentCache.size;

        map["superClassCacheHitCounter"] = superClassCacheHitCounter
        map["equivNodeCacheHitCounter"] = equivNodeCacheHitCounter
        map["subClassCacheHitCounter"] = subClassCacheHitCounter
        map["propertySubsumptionCacheHitCounter"] = propertySubsumptionCacheHitCounter
        map["entailmentCacheHitCounter"] = entailmentCacheHitCounter;
        map["entailmentCacheMissCounter"] = entailmentCacheMissCounter;

        return map;
    }

}