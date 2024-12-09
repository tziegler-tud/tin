package tin.services.ontology.ResultGraph

import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ResultGraphTestUtils {

    fun buildComparisonGraphRestricted(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ResultGraph {
        //build comparison graph
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
        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!


        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                var isInitialState = false;
                var isFinalState = false;
                if(queryNode == s0 && transducerNode == t0) isInitialState = true;
                if(queryNode == s2 && transducerNode == t1) isFinalState = true;
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,beer, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,bruschetta, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,carbonara, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place1, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place2, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,r, isInitialState, isFinalState))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,veganPlace,isInitialState, isFinalState))
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

    fun generateTestTable(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ELHISPLoopTable {
        val spTable = ELHISPLoopTable();

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val beerRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(beer)

        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val veganPlaceRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(veganPlace)

        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!

        val e1 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), beerRes);
        val e1Val = 4;

        val e2 = IndividualLoopTableEntry(Pair(s0,t1), Pair(s1,t1), beerRes);

        val e3 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), veganPlaceRes);

        spTable.set(e1, 4)
        spTable.set(e2, 7)
        spTable.set(e3, 13)

        return spTable;
    }
}