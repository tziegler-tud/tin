package Application;

import DataProvider.DataProvider;
import DataProvider.DataReader;

class main {

    /**
     * Application.main function which is executed.
     * We then delegate the task further down to the Application.SearchHandler.
     * If the user chooses a top k search, we also ask for the k value.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        //ascii_art();

        String userChoiceComputationMode = args[2];
        String userChoiceTransducerMode = args[1];
        String userChoiceFileInput = args[0];
        String userChoiceParameter = "";
        Settings.setMaxIterationStepsInDijkstraLoop(-1);

        if (args.length > 3) {
            userChoiceParameter = args[3];
        } else {
            userChoiceParameter = "0";
        }

        DataReader dataReader;
        SearchHandler searchHandler;

        if (userChoiceTransducerMode.equals("provided")) {
            System.out.println("provided");
            dataReader = new DataReader(userChoiceFileInput, false);

        } else if (userChoiceTransducerMode.equals("generate")) {
            System.out.println("generate");
            dataReader = new DataReader(userChoiceFileInput, true);
        } else { // error ...
            System.out.println("error");
            return;
        }

        searchHandler = new SearchHandler(dataReader.getDataProvider());
        System.out.println("start of data processing...");
        // processes the data
        dataReader.readFile();
        // testing if the parsing was successful and correct -> check "parsedInputData.txt"
        dataReader.printData();
        System.out.println("end of data processing...");

        DataProvider dataProvider = dataReader.getDataProvider();
        System.out.println("userChoice: " + userChoiceComputationMode);

        switch (userChoiceComputationMode) {
            case "classic":
                System.out.println("classic mode");
                searchHandler.searchAllAnswers(dataProvider);
                break;
            case "topK":
                System.out.println("topK mode");
                searchHandler.searchTopKAnswers(dataProvider, Integer.parseInt(userChoiceParameter));
                break;
            case "topKUO":
                System.out.println("topKUO mode");
                searchHandler.searchTopKAnswersUnOptimized(dataProvider, Integer.parseInt(userChoiceParameter));
                break;
            case "threshold":
                System.out.println("threshold mode");
                searchHandler.searchThresholdAnswers(dataProvider, Double.parseDouble(userChoiceParameter));
                break;
            case "thresholdUO":
                System.out.println("thresholdUO mode");
                searchHandler.searchThresholdAnswersUnOptimized(dataProvider, Double.parseDouble(userChoiceParameter));
                break;
            case "thresholdLW":
                System.out.println("thresholdLW mode");
                searchHandler.searchLargestWeight(dataProvider);
                break;
            default:
                System.out.println("invalid input. restart and enter a valid input. Check ReadMe for more info.");
                break;
        }

        boolean possibleInfiniteRun = Settings.getMaxIterationStepsInDijkstraLoop() == Settings.getRestrictionToPreventInfiniteRuns();
        //System.out.println("max iteration steps: " + Settings.getMaxIterationStepsInDijkstraLoop());
        //System.out.println("number of max nodes possible: " + Settings.getNumberOfMaxNodesPossible());
        //System.out.println("possible inf run: " + possibleInfiniteRun);

        if (!userChoiceComputationMode.equals("thresholdLW")) {
            String sb = Settings.getPreprocessingTime() + " " +
                    Settings.getDijkstraProcessingTime() + " " +
                    Settings.getPostprocessingTime() + " " +
                    Settings.getCombinedTime() + " " +
                    Settings.getNumberOfMaxNodesPossible() + " " +
                    Settings.getNumberOfActualNodes() + " " +
                    Settings.getNumberOfAnswers() + " " +
                    possibleInfiniteRun;
            System.out.print(sb);
        } else System.out.print(Settings.getLargestWeight());
    }

    public static void ascii_art() {
        System.out.println();
        System.out.println("┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐");
        System.out.println("|  _____  _                     __                |");
        System.out.println("| /__   \\| |__    ___   ___    / /   ___    __ _  |");
        System.out.println("|   / /\\/| '_ \\  / _ \\ / _ \\  / /   / _ \\  / _` | |");
        System.out.println("|  / /   | | | ||  __/| (_) |/ /___| (_) || (_| | |");
        System.out.println("|  \\/    |_| |_| \\___| \\___/ \\____/ \\___/  \\__, | |");
        System.out.println("|                                          |___/  |");
        System.out.println("└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘");
        System.out.println();

    }


}
