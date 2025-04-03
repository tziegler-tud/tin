package tinDL.services.Task.Benchmark

class TaskProcessingReasonerStats(
   reasonerStatMap: Map<String, Int>,
) {
    val superClassCacheSize = reasonerStatMap["superClassCache"]
    val equivalentClassCacheSize = reasonerStatMap["equivalentClassCache"]
    val subClassCacheSize = reasonerStatMap["subClassCache"];
    val propertySubsumptionCacheSize = reasonerStatMap["propertySubsumptionCache"]
    val entailmentCacheSize = reasonerStatMap["entailmentCache"]

    val superClassCacheHitCounter = reasonerStatMap["superClassCacheHitCounter"]
    val equivNodeCacheHitCounter = reasonerStatMap["equivNodeCacheHitCounter"]
    val subClassCacheHitCounter = reasonerStatMap["subClassCacheHitCounter"]
    val propertySubsumptionCacheHitCounter = reasonerStatMap["propertySubsumptionCacheHitCounter"]
    val entailmentCacheHitCounter = reasonerStatMap["entailmentCacheHitCounter"];
    val entailmentCacheMissCounter = reasonerStatMap["entailmentCacheMissCounter"];
}