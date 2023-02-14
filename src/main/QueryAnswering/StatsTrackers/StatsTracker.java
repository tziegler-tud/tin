package StatsTrackers;

import java.io.FileNotFoundException;

public interface StatsTracker {


    void runDijkstra() throws FileNotFoundException;

    void writeTimeToFile(long milli, long milliPreprocessing, long milliTotal);

    void printEndResult() throws FileNotFoundException;

    void writeResultToFile() throws FileNotFoundException; // old "writeToFile()"

}
