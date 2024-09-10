package tin.model.v2.transducer

import tin.model.v1.alphabet.Alphabet
import tin.model.v2.graph.EdgeLabel
import tin.model.v2.query.EdgeLabelProperty

class TransducerEdgeLabel(
    private val incoming: EdgeLabelProperty,
    private val outgoing: EdgeLabelProperty,
    private val cost: Int,
    ) : EdgeLabel
{
    override fun toString(): String {
        return "${incoming}|${outgoing}|${cost}";
    }

    override fun hashCode(): Int {
        var result = incoming.hashCode()
        result = 31 * result + outgoing.hashCode()
        result = 31 * result + cost.hashCode()
        return result;
    }
}