package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import java.math.BigInteger
import kotlin.math.pow

class NumericSetUtility(
    private val classIndexList: List<OWLClass>,
) {
    private val classReprList: MutableList<ULong> = mutableListOf();
    init {
        classIndexList.forEachIndexed { index, owlClass ->
            val repr = getClassAsSetRepresentation(owlClass);
            classReprList.add(index, repr);
        }
    }

    fun getClassAsSetRepresentation(owlClass: OWLClass) : ULong {
        val classIndex = classIndexList.indexOf(owlClass);
        val set = 1UL; // 0....001
        //perform left shift to move 1 to target position
        val result = set shl classIndex;
        return result;
    }

    fun getSetAsSetRepresentation(set: Set<OWLClass>) : ULong {
        var result = 0UL;
        set.forEach { owlClass ->
            val rep = getClassAsSetRepresentation(owlClass)
            result = result or rep;
        }
        return result;
    }

    fun addElement(base: ULong, element: ULong) : ULong {
        val result = base or element;
        return result;
    }

    fun addElement(base: ULong, element: OWLClass) : ULong {
        return addElement(base, getClassAsSetRepresentation(element));
    }

    fun removeElement(base: ULong, element: ULong) : ULong {
        val inv = element.inv();
        val result = base and inv;
        return result;
    }

    fun removeElement(base: ULong, element: OWLClass) : ULong {
        return removeElement(base, getClassAsSetRepresentation(element));
    }

    fun getClassSet(base: ULong) : Set<OWLClass> {
        val result: MutableSet<OWLClass> = mutableSetOf()
        classReprList.forEachIndexed { i, repr ->
            if(base and repr == repr) {
                result.add(classIndexList[i]);
            }
        }
        return result;
    }

    fun containsElement(base: ULong, element: ULong) : Boolean {
        return (base and element) == element;
    }

    fun containsElement(base: ULong, element: OWLClass) : Boolean {
        val index = classIndexList.indexOf(element);
        if(index == -1) {
            return false;
        }
        val repr = classReprList[index];
        return containsElement(base, repr);
    }


    fun pow(n: Long, exp: Int): Long{
        return BigInteger.valueOf(n).pow(exp).toLong()
    }

    fun pow(n: Int, exp: Int): Long{
        return n.toDouble().pow(exp).toLong()
    }

}