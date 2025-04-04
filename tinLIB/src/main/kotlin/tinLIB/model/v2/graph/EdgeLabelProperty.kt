package tinLIB.model.v2.graph

import tinLIB.model.v1.alphabet.Alphabet

class EdgeLabelProperty(
    private var label: String,
    private var inverse: Boolean = false,
    private var conceptAssertion: Boolean = false,
    private var empty: Boolean = false
)
{
    companion object {
        fun fromString(expression: String): EdgeLabelProperty {
            var label = "";
            var conceptAssertion = false;
            var inverse = false;
            var empty = true;

            if (Alphabet.isConceptAssertion(expression)){
                conceptAssertion = true;
                label = Alphabet.conceptNameFromAssertion(expression);
                empty = false;
            }
            else {
                if(Alphabet.isValidRoleName(expression)){
                    empty = false;
                    if(Alphabet.isInverseRoleName(expression)) {
                        inverse = true;
                        label = Alphabet.transformToPositiveRoleName(expression)
                    }
                    else {
                        label = expression;
                    }
                }
            }
            return EdgeLabelProperty(label, inverse, conceptAssertion, empty);
        }
    }

    fun isEmpty(): Boolean {
        return empty
    }

    fun isConceptAssertion(): Boolean {
        return conceptAssertion;
    }
    fun isInverse(): Boolean {
        return inverse;
    }

    fun getLabel(): String {
        return label;
    }

    fun getInverseAsNewProperty(): EdgeLabelProperty {
        if(isConceptAssertion()) EdgeLabelProperty(label, inverse = false, conceptAssertion = true, empty = empty);
        return EdgeLabelProperty(label, !inverse, conceptAssertion, empty);
    }

    override fun toString(): String {
        return if(inverse) {
            Alphabet.transformToInverseRoleName(label);
        } else {
            label;
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EdgeLabelProperty) return false
        return label == other.label
                && inverse == other.inverse
                && conceptAssertion == other.conceptAssertion
                && empty == other.empty;
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + inverse.hashCode()
        return result
    }
}