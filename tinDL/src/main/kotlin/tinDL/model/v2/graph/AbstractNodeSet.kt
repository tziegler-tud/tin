package tinDL.model.v2.graph

open class AbstractNodeSet<T: Node> : HashSet<T>()
{
    open fun contains(identifier: String): Boolean {
        return find{it.identifier == identifier} != null;
    }

    override fun contains(element: T): Boolean {
        return find{it == element} != null;
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (element in elements){
            if(!contains(element)){ return false};
        }
        return true;
    }

    open fun get(identifier: String): T?{
        return find{it.identifier == identifier};
    }

    override fun add(element: T) : Boolean {
        if(!contains(element)){
            return super.add(element);
        }
        return false;
    }

    open fun asList() : List<T>{
        return toList();
    }
}