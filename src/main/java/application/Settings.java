package application;

public class Settings {

    public static String outputFileDirectory;

    public static int numberOfMaxNodesPossible;
    public static long restrictionToPreventInfiniteRuns;
    public static int numberOfActualNodes;
    public static int maxIterationStepsInDijkstraLoop;

    public static long preprocessingTime;
    public static long dijkstraProcessingTime;
    public static long combinedTime;
    public static long postprocessingTime;

    public static int numberOfAnswers;

    public static Double largestWeight;

    public static void setPreprocessingTime(long preprocessingTime) {
        Settings.preprocessingTime = preprocessingTime;
    }

    public static void setDijkstraProcessingTime(long dijkstraProcessingTime) {
        Settings.dijkstraProcessingTime = dijkstraProcessingTime;
    }

    public static void setPostprocessingTime(long postprocessingTime) {
        Settings.postprocessingTime = postprocessingTime;
    }

    public static void setCombinedTime(long combinedTime) {
        Settings.combinedTime = combinedTime;
    }

    public static void setNumberOfAnswers(int numberOfAnswers) {
        Settings.numberOfAnswers = numberOfAnswers;
    }

    public static void setNumberOfMaxNodesPossible(int numberOfMaxNodesPossible) {
        Settings.numberOfMaxNodesPossible = numberOfMaxNodesPossible;
        Settings.restrictionToPreventInfiniteRuns = (long) numberOfMaxNodesPossible * numberOfMaxNodesPossible * numberOfMaxNodesPossible;
    }

    public static long getRestrictionToPreventInfiniteRuns() {
        return restrictionToPreventInfiniteRuns;
    }

    public static void setNumberOfActualNodes(int numberOfActualNodes) {
        Settings.numberOfActualNodes = numberOfActualNodes;
    }

    public static void setLargestWeight(Double largestWeight) {
        Settings.largestWeight = largestWeight;
    }

    public static int getMaxIterationStepsInDijkstraLoop() {
        return maxIterationStepsInDijkstraLoop;
    }

    public static void setMaxIterationStepsInDijkstraLoop(int maxIterationStepsInDijkstraLoop) {
        Settings.maxIterationStepsInDijkstraLoop = maxIterationStepsInDijkstraLoop;
    }

    public static void setOutputFileDirectory(String outputFileDirectory) {
        Settings.outputFileDirectory = outputFileDirectory;
    }
}
