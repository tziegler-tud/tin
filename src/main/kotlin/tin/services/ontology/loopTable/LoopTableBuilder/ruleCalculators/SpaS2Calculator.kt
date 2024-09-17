package tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import kotlin.math.min

class SpaS2Calculator(
    private val ec: OntologyExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.restrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;
    private val manchesterShortFormProvider = ec.manchesterShortFormProvider;

    /**
     * calculates the updated value for an entry spa[(s,t),(s',t'),M]
     */
    fun calculate(spaLoopTableEntry: SPALoopTableEntry, table: SPALoopTable): Int? {
        val source = spaLoopTableEntry.source;
        val target = spaLoopTableEntry.target;
        val M = spaLoopTableEntry.restriction;
        val s = source.first;
        val t = source.second;
        val se = target.first;
        val te = target.second;

        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null
        if (costCutoff == 0) return 0; //we cannot improve an entry with cost 0

        val MCLassExp = restrictionBuilder.asClassExpression(M);
        val MExp = expressionBuilder.createELHIExpression(MCLassExp);

        val superM = dlReasoner.calculateSuperClasses(MExp, true);

        //get all edges (s,_,s1) € QueryGraph
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(s,se);
        if(candidateQueryEdges.isEmpty()) {
            return costCutoff;
        }

        var candidateQueryTransitions = candidateQueryEdges.map{it.label}

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(t, te);
        if(candidateTransducerEdges.isEmpty()) {
            return costCutoff;
        }

        val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }

        //find the minimal cost of an edge (t,u,A?,w,t1) with:
        //  u in candidateQueryTransitions
        //  A in superM

        val validEdges: MutableList<TransducerEdge> = mutableListOf()
        sortedTransducerEdges.forEach edgeCheck@ { transducerEdge ->
            val inLabel = transducerEdge.label.incoming;
            val outLabel = transducerEdge.label.outgoing;

            // try to match edge label to concept assertion. outgoing label must be a concept assertion
            var edgeClass: OWLClass = queryParser.getOWLClass(outLabel) ?: return@edgeCheck;
            //if no property could be obtained, there is no way to continue - this also means our transducer uses class names which are not part of our ontology.


            val isValidQueryTransition = candidateQueryTransitions.contains(QueryEdgeLabel(inLabel))
                    && (superM.containsEntity(edgeClass));
            if(isValidQueryTransition) {
                validEdges.add(transducerEdge)
            };
        }

        if(validEdges.isEmpty()){
            return costCutoff;
        }
        else {
            return validEdges.minOf { it.label.cost };
        }
    }
}