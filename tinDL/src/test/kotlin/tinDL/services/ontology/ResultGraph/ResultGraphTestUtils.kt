package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ResultGraphTestUtils {

    fun buildComparisonGraphRestricted(ec: ExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ResultGraph {
        val comparisonGraph = ResultGraph()

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!

        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,beer))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,bruschetta))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,carbonara))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place1))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place2))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,r))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,veganPlace))
            }
        }

        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s1,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s1,t0,carbonara), 0)

        comparisonGraph.addEdge(ResultNode(s1,t0,place1), ResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,place1), ResultNode(s0,t0,carbonara), 0)

        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,veganPlace), ResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(ResultNode(s1,t0, veganPlace), ResultNode(s2,t1, veganPlace), 4)
        comparisonGraph.addEdge(ResultNode(s1,t0, bruschetta), ResultNode(s2,t1, bruschetta), 4)

        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,r), ResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s1,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,r), ResultNode(s0,t0,carbonara), 0)

        return comparisonGraph;
    }

    fun buildComparisonGraph(ec: ExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ResultGraph {
        val comparisonGraph = buildComparisonGraphRestricted(ec, queryGraph, transducerGraph);

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!

        comparisonGraph.addEdge(ResultNode(s0,t0,beer), ResultNode(s1,t0,beer), 4)
        comparisonGraph.addEdge(ResultNode(s0,t1,beer), ResultNode(s1,t1,beer), 7)
        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s1,t0,veganPlace), 13)

        return comparisonGraph;

    }

    fun generateTestTableELHI(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ELHISPLoopTable {
        val spTable = ELHISPLoopTable();

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val beerRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(beer)

        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val veganPlaceRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(veganPlace)

        val e1 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), beerRes);
        val e2 = IndividualLoopTableEntry(Pair(s0,t1), Pair(s1,t1), beerRes);
        val e3 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), veganPlaceRes);

        spTable.set(e1, 4)
        spTable.set(e2, 7)
        spTable.set(e3, 13)

        return spTable;
    }
}