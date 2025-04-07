package tinDL.services.ontology.loopTable.LoopTableBuilder.ELH

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpCalculator

class ELSPLoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELExecutionContext
) : ELLoopTableBuilder
{
    private var table: ELSPLoopTable = ELSPLoopTable();
    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>()

    private val calculator = SpCalculator(ec, queryGraph, transducerGraph)

    //stat tracking
    var statsTotalSize: Int = 0;
    val statsMaxPossibleSize: Int = (queryGraph.nodes.size * transducerGraph.nodes.size * ec.individuals.size);

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
        statsTotalSize = table.getSize();
        return table;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }

    fun getSize(): Int {
        return table.map.size;
    }
}