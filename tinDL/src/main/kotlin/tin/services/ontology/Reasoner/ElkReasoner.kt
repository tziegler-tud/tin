package tin.services.ontology.Reasoner

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLNamedIndividual
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
class ElkReasoner(
    reasoner: OWLReasoner,
    expressionBuilder: DLExpressionBuilder
) : AbstractReasoner(reasoner, expressionBuilder) {
    val superClassCache: HashMap<DLExpression, NodeSet<OWLClass>> = hashMapOf()
    val equivalentClassCache: HashMap<DLExpression, Node<OWLClass>> = hashMapOf()
    val subClassCache: HashMap<DLExpression, HashSet<OWLClass>> = hashMapOf()
    val propertySubsumptionCache: HashMap<OWLPropertyExpression, List<OWLObjectPropertyExpression>> = hashMapOf()
    val entailmentCache: HashMap<Pair<DLExpression, DLExpression>, Boolean> = hashMapOf()

    var superClassCacheHitCounter = 0;
    var equivNodeCacheHitCounter = 0;
    var subClassCacheHitCounter = 0;
    var propertySubsumptionCacheHitCounter = 0;
    var entailmentCacheHitCounter = 0;
    var entailmentCacheMissCounter = 0;

    private var topClassNode : Node<OWLClass>? = null

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
        val cacheEntry = propertySubsumptionCache[superProperty];
        if(cacheEntry != null){
            propertySubsumptionCacheHitCounter++;
            return cacheEntry.contains(property)
        }
        val subclasses: MutableList<OWLObjectPropertyExpression> = mutableListOf(superProperty)
        val subs = reasoner.getSubObjectProperties(superProperty)
        subs.forEach{sub ->
            val s = sub.representativeElement
            subclasses.add(s)
        }
        propertySubsumptionCache[property] = subclasses
        return subclasses.contains(property);
    }

    override fun calculateSubClasses(expr: DLExpression, includeNothing: Boolean, includeEquivalent: Boolean): HashSet<OWLClass> {
        val cacheEntry = subClassCache[expr];
        if(cacheEntry != null){
            subClassCacheHitCounter++;
            return cacheEntry
        }
        val exp = expr.getClassExpression()
        var classes = reasoner.getSubClasses(exp, false)
        var classes2 = reasoner.getSubClasses(exp, true)
        //remove owl:Nothing
        if(!includeNothing && classes.isBottomSingleton) {
            classes = OWLClassNodeSet();
        }
        if(!includeNothing) {
            classes.removeAll { it.isBottomNode }
        }

        if(!includeNothing && classes2.isBottomSingleton) {
            classes2 = OWLClassNodeSet();
        }
        if(!includeNothing) {
            classes2.removeAll { it.isBottomNode }
        }
        val resultSet = classes.entities().toList().toHashSet()
        resultSet.addAll(classes2.entities().toList())
        if(includeEquivalent) {
            val equiv = reasoner.getEquivalentClasses(expr.getClassExpression())
            resultSet.addAll(equiv)
        }
        subClassCache[expr] = resultSet;
        return resultSet;
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

    override fun getTopClassNode(): Node<OWLClass> {
        if (topClassNode !== null) {
            return topClassNode!!;
        }
        topClassNode = reasoner.getTopClassNode();
        return topClassNode!!;
    }

}