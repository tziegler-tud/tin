package tinDB.model.v2.productAutomaton

import tinLIB.model.v2.graph.EdgeLabel
import tinLIB.model.v2.graph.EdgeSet
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge

class ProductAutomatonEdgeSet : EdgeSet<ProductAutomatonEdge>() {
    companion object {
        public fun fromList(list: List<ProductAutomatonEdge>) : ProductAutomatonEdgeSet {
            val set = ProductAutomatonEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<ProductAutomatonEdge> {
        return filter{it.source === source};
    }
    override fun filterForTarget(target: Node): List<ProductAutomatonEdge> {
        return filter{it.target === target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<ProductAutomatonEdge> {
        return filter{it.source === source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<ProductAutomatonEdge> {
        return filter{it.label === label};
    }
    override fun containsEdge(edge: ProductAutomatonEdge): Boolean {
        for (genericEdge in this) {
            if(genericEdge == edge) {
                return true
            };
            else {
                continue
            }
        }
        return false;
    }

}