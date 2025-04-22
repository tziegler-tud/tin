package tinLIB.model.v2.graph

open class NodeSet<T: Node> : AbstractNodeSet<T>()
{
    override fun contains(element: T): Boolean {
        return find{it == element} != null;
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (element in elements){
            if(!contains(element)){ return false};
        }
        return true;
    }

    fun containsWithoutState(element: T): Boolean {
        return find{element.equalsWithoutState(it)} != null;
    }

    override fun add(element: T) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }
}