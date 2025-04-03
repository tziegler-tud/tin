package Application;

import StatsTrackers.*;
import DataProvider.DataProvider;

import java.io.FileNotFoundException;

public class SearchHandler {

    public SearchHandler(DataProvider dataProvider) {

    }

    public void searchAllAnswers(DataProvider dataProvider) throws FileNotFoundException {
        StatsTrackerClassic statsTrackerClassic = new StatsTrackerClassic(dataProvider);
        statsTrackerClassic.runDijkstra();
    }

    public void searchLargestWeight(DataProvider dataProvider)  {
        StatsTrackerLargestWeight statsTrackerLargestWeight = new StatsTrackerLargestWeight(dataProvider);
        statsTrackerLargestWeight.runDijkstra();
    }

    public void searchTopKAnswers(DataProvider dataProvider, int k) throws FileNotFoundException {
        StatsTrackerTopK statsTrackerTopK = new StatsTrackerTopK(dataProvider, k);
        statsTrackerTopK.runDijkstra();

    }

    public void searchThresholdAnswers(DataProvider dataProvider, Double threshold) throws FileNotFoundException {
        StatsTrackerThreshold statsTrackerThreshold = new StatsTrackerThreshold(dataProvider, threshold);
        statsTrackerThreshold.runDijkstra();
    }

    public void searchTopKAnswersUnOptimized(DataProvider dataProvider, int k) throws FileNotFoundException {
        StatsTrackerTopKUnOptimized statsTrackerTopKUnOptimized = new StatsTrackerTopKUnOptimized(dataProvider, k);
        statsTrackerTopKUnOptimized.runDijkstra();
    }

    public void searchThresholdAnswersUnOptimized(DataProvider dataProvider, Double threshold) throws FileNotFoundException {
        StatsTrackerThresholdUnOptimized statsTrackerThresholdUnOptimized = new StatsTrackerThresholdUnOptimized(dataProvider, threshold);
        statsTrackerThresholdUnOptimized.runDijkstra();
    }

}
