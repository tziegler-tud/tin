package tin.model.productAutomaton

enum class ProductAutomatonEdgeType {
    // type 1: incoming epsilon edges
    EpsilonIncomingPositiveOutgoing,  // epsilon incoming, positive outgoing
    EpsilonIncomingNegativeOutgoing,  // epsilon incoming, negative outgoing
    EpsilonIncomingEpsilonOutgoing,  // epsilon incoming, epsilon outgoing

    // type 2: incoming positive edges
    PositiveIncomingPositiveOutgoing,  // positive incoming, positive outgoing
    PositiveIncomingNegativeOutgoing,  // positive incoming, negative outgoing
    PositiveIncomingEpsilonOutgoing,  // positive incoming, epsilon outgoing

    // type 3: incoming negative edges
    NegativeIncomingPositiveOutgoing,  // negative incoming, positive outgoing
    NegativeIncomingNegativeOutgoing,  // negative incoming, negative outgoing
    NegativeIncomingEpsilonOutgoing    // negative incoming, epsilon outgoing
}