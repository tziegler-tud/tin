package tin.services.ontology

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLPropertyExpression
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode
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
) {
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

    public fun checkIsSubsumed(expr: DLExpression, superExpression: DLExpression): Boolean {
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

    public fun checkPropertySubsumption(property: OWLObjectPropertyExpression, superProperty: OWLObjectPropertyExpression): Boolean {
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

    public fun calculateSuperProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSuperObjectProperties(property, false);
        return properties;
    }


    public fun calculateSuperPropertiesMock(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        return OWLObjectPropertyNodeSet(property)
    }

    public fun calculateSubProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSubObjectProperties(property, false);
        return properties;
    }

    public fun calculateSubClasses(expr: DLExpression): HashSet<OWLClass> {
        val cacheEntry = subClassCache[expr];
        if(cacheEntry != null){
            subClassCacheHitCounter++;
            return cacheEntry
        }
        var classes = reasoner.getSubClasses(expr.getClassExpression(), false)
        //remove owl:Nothing
        if(classes.isBottomSingleton) {
            classes = OWLClassNodeSet();
        }

        val hashSet = classes.entities().toList().toHashSet()
        subClassCache[expr] = hashSet;
        return hashSet;
    }

    fun getTopClassNode(): Node<OWLClass> {
        return reasoner.topClassNode;
    }

    fun getBottomClassNode(): Node<OWLClass> {
        return reasoner.bottomClassNode;
    }

    fun calculateSuperClasses(expr: DLExpression, includeEquivalent: Boolean): NodeSet<OWLClass> {
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

    fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass> {
        return reasoner.getEquivalentClasses(expr.getClassExpression());
    }

    fun clearCache() {
        superClassCache.clear();
        equivalentClassCache.clear();
        subClassCache.clear();
        propertySubsumptionCache.clear();
        entailmentCache.clear();
    }
}