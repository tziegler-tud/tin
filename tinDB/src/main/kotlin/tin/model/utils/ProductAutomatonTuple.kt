package tin.model.utils

import tin.model.productAutomaton.ProductAutomatonNode

class ProductAutomatonTuple(
    var sourceProductAutomatonNode: ProductAutomatonNode?,
    var targetProductAutomatonNode: ProductAutomatonNode

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonTuple) return false

        return (sourceProductAutomatonNode == other.sourceProductAutomatonNode) &&
                (targetProductAutomatonNode == other.targetProductAutomatonNode)
    }

    override fun hashCode(): Int {
        var result = sourceProductAutomatonNode.hashCode()
        result = 31 * result + targetProductAutomatonNode.hashCode()
        return result
    }

}