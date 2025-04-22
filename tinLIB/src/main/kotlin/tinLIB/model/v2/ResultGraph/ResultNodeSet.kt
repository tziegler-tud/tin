package tinLIB.model.v2.ResultGraph

import tinLIB.model.v2.graph.NodeSet

open class ResultNodeSet<T: ResultNode> : NodeSet<T>()
{
//    override fun contains(element: T): Boolean {
//        return find{it == element as ResultNode} != null;
//    }

//    fun contains(element: ResultNode): Boolean {
//        return find{it == element} != null;
//    }

//    fun containsWithoutState(element: T): Boolean {
//        return find{element.equalsWithoutState(it)} != null;
//    }

//    override fun add(element: T) : Boolean {
//        if(!contains(element)){
//            return super.add(element as T);
//        }
//        return false;
//    }


//    fun forEach(action: (T)-> Unit ) {
//        super.forEach {
//            action(it)
//        }
//    }
//
//    override fun asList(): List<T> {
//        val list = mutableListOf<T>();
//        this.forEach { resultNode ->
//            list.add(resultNode);
//        }
//        return list;
//    }
}