package application;

import queryAnswering.statsTrackers.*;
import dataProvider.DataProvider;

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

    public void searchTopKAnswersOptimized(DataProvider dataProvider, int k) throws FileNotFoundException {
        StatsTrackerTopKOptimized statsTrackerTopKOptimized = new StatsTrackerTopKOptimized(dataProvider, k);
        statsTrackerTopKOptimized.runDijkstra();

    }

    public void searchThresholdAnswersOptimized(DataProvider dataProvider, Double threshold) throws FileNotFoundException {
        StatsTrackerThresholdOptimized statsTrackerThresholdOptimized = new StatsTrackerThresholdOptimized(dataProvider, threshold);
        statsTrackerThresholdOptimized.runDijkstra();
    }

    public void searchTopKAnswersNaive(DataProvider dataProvider, int k) throws FileNotFoundException {
        StatsTrackerTopKNaive statsTrackerTopKNaive = new StatsTrackerTopKNaive(dataProvider, k);
        statsTrackerTopKNaive.runDijkstra();
    }

    public void searchThresholdAnswersNaive(DataProvider dataProvider, Double threshold) throws FileNotFoundException {
        StatsTrackerThresholdNaive statsTrackerThresholdNaive = new StatsTrackerThresholdNaive(dataProvider, threshold);
        statsTrackerThresholdNaive.runDijkstra();
    }

}
