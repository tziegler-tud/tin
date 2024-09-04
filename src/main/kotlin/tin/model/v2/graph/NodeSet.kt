package tin.model.v2.graph

open class NodeSet : HashSet<Node>()
{
    override fun contains(element: Node): Boolean {
        return find{it.identifier === element.identifier} != null;
    }

    override fun containsAll(elements: Collection<Node>): Boolean {
        for (element in elements){
            if(!contains(element)){ return false};
        }
        return true;
    }

    public fun get(identifier: String): Node?{
        return find{it.identifier === identifier};
    }

    override fun add(element: Node) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }
}