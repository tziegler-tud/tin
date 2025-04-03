package tinDL.model.v2.graph

open class NodeSet : AbstractNodeSet<Node>()
{
    override fun contains(element: Node): Boolean {
        return find{it == element} != null;
    }

    override fun containsAll(elements: Collection<Node>): Boolean {
        for (element in elements){
            if(!contains(element)){ return false};
        }
        return true;
    }

    override fun add(element: Node) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }
}