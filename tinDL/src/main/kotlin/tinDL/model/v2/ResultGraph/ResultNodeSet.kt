package tinDL.model.v2.ResultGraph

import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.graph.NodeSet

class ResultNodeSet : NodeSet()
{
    override fun contains(element: Node): Boolean {
        return contains(element as ResultNode);
    }

    fun contains(element: ResultNode): Boolean {
        return find{it == element} != null;
    }

    fun containsWithoutState(element: ResultNode): Boolean {
        return find{element.equalsWithoutState(it)} != null;
    }

    override fun add(element: Node) : Boolean {
        if(!contains(element as ResultNode)){
            return super.add(element);
        }
        return false;
    }

    fun add(element: ResultNode) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }

    fun forEach(action: (ResultNode)-> Unit ) {
        super.forEach {
            action(it as ResultNode)
        }
    }

    override fun asList(): List<ResultNode> {
        val list = mutableListOf<ResultNode>();
        this.forEach { node ->
            list.add(node as ResultNode);
        }
        return list;
    }
}