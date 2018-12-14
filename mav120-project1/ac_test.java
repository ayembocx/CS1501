import java.io.*;
import java.util.*;

public class ac_test {
    public static final double NANOSECS_PER_SEC = 1000000000.0;
    public static StringBuilder currentWord = new StringBuilder();
    public static DLB dictionary = new DLB();
    public static TrieST<Integer> history = new TrieST<Integer>();
    public static Scanner reader;
    public static final int NUM_SUGGESTIONS = 5;

    public static HashSet<String> words = new HashSet<>();

    private static void addDictionary() {
        System.out.println("Loading dictionary ...");
        try {
            File file = new File("dictionary.txt");
            Scanner scanner = new Scanner(file);
            Map<String, Integer> historyWordMap = history.toHashMap();
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(!historyWordMap.containsKey(line))
                    dictionary.add(line);
            }
            scanner.close();

        } catch(FileNotFoundException e){
            System.out.println("There was an error, the dictionary.txt file was not found");
        }
    }

    private static void loadUserHistory() {
        try {
            File file = new File("user_history.txt");
            Scanner scanner = new Scanner(file);
            System.out.println("Loading user history ...");

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int commaIndex = line.lastIndexOf(',');
                String word = line.substring(0, commaIndex);
                int freq = Integer.parseInt(line.substring(commaIndex+1));
                history.put(word, freq);
            }
            scanner.close();

        } catch(FileNotFoundException e){
            System.out.println("No user history found. Most frequent words will be saved to user_history.txt");
        }
    }

    public static char promptUser(String prompt) {
        System.out.print(prompt);
        return reader.next().trim().charAt(0);
    }


    public static String formatTime(long time) {
        return String.format("(%f s)", (double)time/NANOSECS_PER_SEC);
    }

    public static double averageSearchTime = 0;
    public static int searches = 0;
    public static void calcAverageTime(long time) {
        searches++;
        averageSearchTime = (averageSearchTime * (searches-1) + time) / searches;
    }


    public static void printResults(String[] results, long estimatedTime) {
        System.out.printf("\n%s\n", formatTime(estimatedTime));

        if(results[0] == null) {
            System.out.print("Cannot find word \"" + currentWord + "\". Type '$' to add to dictionary.");
        } else {
            printPredictionList(results);
        }

        System.out.print('\n');
    }

    private static void printPredictionList(String[] results) {
        System.out.print("Predictions:\n");
        for(int i=0; i<results.length; i++) {
            String result = results[i];
            if(result == null) break;
            System.out.print("(" + (i+1) + ") " + results[i] + "\t  ");
        }
    }


    public static void addWordToHistory(String word) {
        Integer freq = history.get(word);
        if(freq == null) freq = 0;
        history.put(word, freq+1);

    }

    public static char decideAction(char c, String[] results) {
        if("12345$".indexOf(c) > -1) {
            String word;
            if(c == '$' || results[0] == null) {
                word = currentWord.toString();
            } else {
                word = results[Character.getNumericValue(c)-1];
                String suffix = word.substring(currentWord.length());
                if(history.get(word) == null) {
                    // We want to delete a word out of dictionary if
                    // its not in the the user history
                    dictionary.deleteCurrentWordWithSuffix(suffix);
                }
            }
            System.out.print("\n\nWORD COMPLETE: " + word + "\n\n\n");
            addWordToHistory(word);
            return startNewWordSearch("Enter first character of next word: ");
        } else {
            currentWord.append(c);
        }
        return c;
    }

    public static char startNewWordSearch(String prompt) {
        currentWord = new StringBuilder();
        char c = promptUser(prompt);
        dictionary.startNewSearch();
        currentWord.append(c);
        return c;
    }

    public static String[] getPredictions(char c) {
        String[] results = new String[NUM_SUGGESTIONS];
        int i =0;

        List<String> historyResults = history.keysWithPrefix(currentWord.toString());
        int min = Math.min(NUM_SUGGESTIONS, historyResults.size());
        while(i < min) {
            results[i] = historyResults.get(i);
            i++;
        }

        ArrayList<String> dictionaryResultsList = dictionary.search(c, NUM_SUGGESTIONS-historyResults.size());
        if(i < NUM_SUGGESTIONS) {
            if(dictionaryResultsList != null) {
                String[] dictionaryResults = dictionaryResultsList.toArray(new String[]{});
                System.arraycopy(dictionaryResults,0,results,i,dictionaryResults.length);
            }
        }
        return results;
    }

    public static void startAutocompleteLoop() {
        char c = startNewWordSearch("Enter your first character: ");
        while(c != '!') {
            long startTime = System.nanoTime();
            String[] results = getPredictions(c);
            long estimatedTime = System.nanoTime() - startTime;
            calcAverageTime(estimatedTime);

            printResults(results, estimatedTime);

            c = promptUser("\nEnter next character: ");
            c = decideAction(c, results);
        }
    }

    public static void sayGoodBye() {
        System.out.println("\n\nAverage Time: " + formatTime((long)averageSearchTime));
        System.out.println("Bye!");
    }

    public static void saveWordHistory() {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("user_history.txt")));
            out.print(history.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Error in saving word history ... ");
        }
    }

    public static void main(String[] args) {
        loadUserHistory();
        addDictionary();

        reader = new Scanner(System.in);

        startAutocompleteLoop();

        saveWordHistory();
        sayGoodBye();

        reader.close();
    }
}
