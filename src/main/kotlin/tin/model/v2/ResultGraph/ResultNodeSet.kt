package tin.model.v2.ResultGraph

import tin.model.v2.graph.Node
import tin.model.v2.graph.NodeSet

class ResultNodeSet : NodeSet()
{
    override fun contains(element: Node): Boolean {
        val resultNodeElement = element.asResultNode() ?: return false
        return find{it == resultNodeElement} != null;
    }

    fun contains(element: ResultNode): Boolean {
        return find{it == element} != null;
    }

    override fun add(element: Node) : Boolean {
        val resultNodeElement = element.asResultNode() ?: return false
        if(!contains(resultNodeElement)){
            return super.add(resultNodeElement);
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
            action(it.asResultNode()!!)
        }
    }
}