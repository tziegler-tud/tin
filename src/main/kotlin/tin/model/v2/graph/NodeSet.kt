package tin.model.v2.graph

open class NodeSet : HashSet<Node>()
{
    fun contains(identifier: String): Boolean {
        return find{it.identifier == identifier} != null;
    }

    override fun contains(element: Node): Boolean {
        return find{it == element} != null;
    }

    override fun containsAll(elements: Collection<Node>): Boolean {
        for (element in elements){
            if(!contains(element)){ return false};
        }
        return true;
    }

    public fun get(identifier: String): Node?{
        return find{it.identifier == identifier};
    }

    override fun add(element: Node) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }
}