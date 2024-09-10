package tin

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tin.model.v1.alphabet.Alphabet
import tin.model.v1.database.DatabaseGraph
import tin.model.v1.database.DatabaseNode
import tin.services.internal.DijkstraQueryAnsweringServiceTest
import tin.services.internal.ProductAutomatonServiceTest

@Service
class TinTest {

    @Autowired
    lateinit var productAutomatonServiceTest: ProductAutomatonServiceTest

    @Autowired
    lateinit var dijkstraQueryAnsweringServiceTest: DijkstraQueryAnsweringServiceTest

    @Test
    fun testFileReaders(){

    }

    @Test
    fun testProductAutomatonBuilder() {

    }

    @Test
    fun testDijkstra() {
        // test 3 computation modes
    }

    @Test
    fun testAlphabet(){
        val a1 = Alphabet();
        val a2 = Alphabet();
        val a3 = Alphabet();

        a1.addRoleName("r")
        a1.addRoleName("s")
        a1.addConceptName("A")
        a1.addConceptName("B")

        a2.addRoleName("s")
        a2.addRoleName("r")
        a2.addConceptName("B")
        a2.addConceptName("A")

        a3.addRoleName("r");
        a3.addRoleName("t");
        a3.addConceptName("A");
        a3.addConceptName("C");

        val a4 = Alphabet(a1);

        assert(a1.includes("r"));
        assert(a1.includes("s"));
        assert(a1.includes("A?"));
        assert(a1.includes("B?"));

        assert(!a1.includes("t"))
        assert(!a1.includes("A")) //includes checks transformed concept names, not raw ones
        assert(!a1.includes("C?"))

        assert(a1.getAlphabet().contains("r"))
        assert(a1.getAlphabet().contains("A?"))
        assert(!a1.getAlphabet().contains("A"))

        assert(a1.equals(a2));
        assert(a1.equals(a4));
        assert(!a1.equals(a3));

        assert(a1.hashCode() == a2.hashCode())
        assert(a1.hashCode() != a3.hashCode())

        val valid1 = "validRoleName1"
        val inverseValid1 = "inverse(validRoleName1)";

        assert(Alphabet.isValidRoleName(valid1))
        assert(Alphabet.isValidRoleName(inverseValid1))
        assert(Alphabet.isValidRoleName("invalid_RoleName1"))

        assert(!Alphabet.isValidRoleName("invalidRoleName1?"))
        assert(!Alphabet.isValidRoleName("invers(validRoleName1)"))

        assert(!Alphabet.isInverseRoleName(valid1))
        assert(Alphabet.isInverseRoleName(inverseValid1))
        assert(Alphabet.transformToInverseRoleName(valid1) == inverseValid1)
        assert(Alphabet.transformToPositiveRoleName(inverseValid1) == valid1)
    }

    @Test
    fun testDatabaseGraph(){
        var graph = DatabaseGraph();
        val n1 = DatabaseNode("n1");
        val n2 = DatabaseNode("n2");
        graph.addNodes(n1, n2);
        graph.addEdge(n1, n2, "r");
        graph.addNodeProperty(n1, "A")
        graph.addNodeProperty(n2, "B")

        assert(graph.getNode(n1.identifier)!!.hasProperty("A"));
        assert(!graph.getNode(n1.identifier)!!.hasProperty("B"));
    }
}