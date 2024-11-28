package tin.services.ontology.loopTable.LoopTableBuilder.ELH

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpCalculator

class ELSPLoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELExecutionContext
)
{
    private var table: ELSPLoopTable = ELSPLoopTable();
    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>()

    private val calculator = SpCalculator(ec, queryGraph, transducerGraph)

    private fun initializeTable(){
        //build all pairs for (s,t) € queryNodes x transducerNodes
        queryGraph.nodes.forEach { node ->
            transducerGraph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
            }
        }
    }


    fun calculateFullTable(spaTable: ELSPALoopTable): ELSPLoopTable {
        //iterate until max iterations are reached
        table = calculator.calculateAll(spaTable)
        return table;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }
}