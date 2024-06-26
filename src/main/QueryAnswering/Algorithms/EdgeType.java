package Algorithms;


public enum EdgeType {

    // type 1: incoming epsilon edges
    epsilonIncomingPositiveOutgoing,    // epsilon incoming, positive outgoing
    epsilonIncomingNegativeOutgoing,    // epsilon incoming, negative outgoing
    epsilonIncomingEpsilonOutgoing,     // epsilon incoming, epsilon outgoing - not yet used

    // type 2: incoming positive edges
    positiveIncomingPositiveOutgoing,      // positive incoming, positive outgoing
    positiveIncomingNegativeOutgoing,      // positive incoming, negative outgoing
    positiveIncomingEpsilonOutgoing,       // positive incoming, epsilon outgoing

    // type 3: incoming negative edges
    negativeIncomingPositiveOutgoing,       // negative incoming, positive outgoing
    negativeIncomingNegativeOutgoing,      // negative incoming, negative outgoing
    negativeIncomingEpsilonOutgoing,       // negative incoming, epsilon outgoing
}
