package tin.model.v2.query

import tin.model.v1.alphabet.Alphabet

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
    override fun toString(): String {
        return label;
    }
}