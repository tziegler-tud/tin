package tinDB.model.v1.productAutomaton

enum class ProductAutomatonEdgeType {
    // type 1: incoming epsilon edges
    EpsilonIncomingPositiveOutgoing,  // epsilon incoming, positive outgoing
    EpsilonIncomingNegativeOutgoing,  // epsilon incoming, negative outgoing
    EpsilonIncomingEpsilonOutgoing,  // epsilon incoming, epsilon outgoing
    EpsilonIncomingPropertyOutgoing,    // epsilon incoming, property outgoing

    // type 2: incoming positive edges
    PositiveIncomingPositiveOutgoing,  // positive incoming, positive outgoing
    PositiveIncomingNegativeOutgoing,  // positive incoming, negative outgoing
    PositiveIncomingEpsilonOutgoing,  // positive incoming, epsilon outgoing
    PositiveIncomingPropertyOutgoing,   // positive incoming, property outgoing

    // type 3: incoming negative edges
    NegativeIncomingPositiveOutgoing,   // negative incoming, positive outgoing
    NegativeIncomingNegativeOutgoing,   // negative incoming, negative outgoing
    NegativeIncomingEpsilonOutgoing,    // negative incoming, epsilon outgoing
    NegativeIncomingPropertyOutgoing,   // negative incoming, property outgoing

    //type 4: incoming property assertion
    PropertyIncomingEpsilonOutgoing,    // property incoming, epsilon outgoing
    PropertyIncomingPositiveOutgoing,   // property incoming, positive outgoing
    PropertyIncomingNegativeOutgoing,   // property incoming, negative outgoing
    PropertyIncomingPropertyOutgoing,   // property incoming, property outgoing
}