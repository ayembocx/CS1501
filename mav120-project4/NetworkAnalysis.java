import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class NetworkAnalysis {
    public static ComputerNetwork g;
    public static Scanner scanner;
    public static DijkstraAllPairsSP shortestPaths;

    public static NetworkEdge createEdge(String[] edgeData) {
        int from = Integer.parseInt(edgeData[0]);
        int to = Integer.parseInt(edgeData[1]);
        String type = edgeData[2];
        int capacity = Integer.parseInt(edgeData[3]);
        int length = Integer.parseInt(edgeData[4]);
        return new NetworkEdge(from, to, capacity, length, type);
    }

    public static void loadFromFile(String filename) {
        System.out.println("Loading graph....");

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            int V = Integer.parseInt(in.readLine());
            g = new ComputerNetwork(V);
            String line;
            while ((line = in.readLine()) != null) {
                String[] edgeData = line.split(" ");
                NetworkEdge e = createEdge(edgeData);
                g.addEdge(e);
                String tmpVertex = edgeData[0];
                edgeData[0] = edgeData[1];
                edgeData[1] = tmpVertex;
                NetworkEdge otherE = createEdge(edgeData);
                g.addEdge(otherE);
                shortestPaths = new DijkstraAllPairsSP(g);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not load file.");
        } catch (IOException e) {
            System.out.println("Error processing file.");
        }
    }

    public static void showOptions() {
        System.out.println("\nType in a number for the action you wish to perform. ");
        System.out.println("\t1. Find lowest latency path ");
        System.out.println("\t2. Determine the if the graph is copper connected");
        System.out.println("\t3. Find max amount of data that can be transferred from one vertex to another");
        System.out.println("\t4. Determine if network remains connected if any two vertices fail");
        System.out.println("\t5. Exit");
    }

    public static int getUserOption() {
        System.out.print("Enter option: ");
        String opt = scanner.nextLine();

        char num = opt.charAt(0);
        if(opt.length() == 1 && "12345".indexOf(num) > -1) {
            return Character.getNumericValue(num);
        }

        return -1;
    }

    public static void performAction(int opt) {
        switch(opt) {
            case 1:
                promptLowestLatency();
                break;
            case 2:
                displayIfCopperConnected();
                break;
            case 3:
                promptMaxData();
                break;
            case 4:
                checkIfFailsOnTwoVertices();
                break;
            case 5:
                System.out.println("Thank you!");
                scanner.close();
                System.exit(0);
                break;
            default:
                System.out.println("Incorrect option.");
        }

        System.out.println("\n");

    }

    public static void checkIfFailsOnTwoVertices() {
        int V = g.V();
        if(V <= 3) {
            System.out.println("There are only " + V + " vertices. Removing two would cause the graph to be disconnected");
            return;
        }
        boolean doesNotFail = true;
        for(int v1=0; v1<V; v1++) {
            for(int v2=v1+1; v2<V; v2++) {
                if(!checkIfFailsOnTwoVertices(v1, v2)) {
                    doesNotFail = false;
                    System.out.println("It will not be connected if vertex " + v1 + " and " + v2 + " are removed.");
                    break;
                }
            }
        }
        if(doesNotFail) {
            System.out.println("It does not fail!");
        }

    }

    public static boolean checkIfFailsOnTwoVertices(int v1, int v2) {
        boolean[] marked = new boolean[g.V()]; // marked[v] = is there an s-v path
        Queue<Integer> q = new LinkedList<Integer>();
        marked[v1] = true;
        marked[v2] = true;
        int start = 0;
        if(start == v1) start = 1;
        if(start == v2) start = 2;
        marked[start] = true;
        q.add(start);

        int count = 1;
        while (!q.isEmpty()) {
            int v = q.remove();
            for (NetworkEdge e : g.adj(v)) {
                int w = e.to();
                if (!marked[w]) {
                    marked[w] = true;
                    count++;
                    q.add(w);
                }
            }
        }
        return count+2 == g.V();
    }



    public static void promptLowestLatency() {
        boolean validVertices = false;
        while(!validVertices) {
            System.out.print("\nEnter first vertex: ");
            int v1 = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter second vertex: ");
            int v2 = Integer.parseInt(scanner.nextLine());
            validVertices = v1 >= 0 && v2 >= 0;
            calculateLowestPath(v1, v2);
        }
    }

    public static void promptMaxData() {
        boolean validVertices = false;
        while(!validVertices) {
            System.out.print("\nEnter first vertex: ");
            int v1 = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter second vertex: ");
            int v2 = Integer.parseInt(scanner.nextLine());
            validVertices = v1 >= 0 && v2 >= 0;
            calculateMaxData(v1, v2);
        }
    }

    public static void calculateMaxData(int v1, int v2) {
        for(NetworkEdge e : g.edges()) {
            e.resetFlow();
        }
        FordFulkerson maxflow = new FordFulkerson(g, v1, v2);
        System.out.println("Maximum bandwidth is " + maxflow.value());
    }

    /*
        Does a BFS on the graph only with copper wires.
     */
    public static void displayIfCopperConnected() {
        int V = g.V();
        int count = bfs(0);
        boolean onlyCopperConnected = count == V;
        if(onlyCopperConnected) {
            System.out.println("This graph is copper-only connected.");
        } else {
            System.out.println("This graph is not copper-only connected.");
        }
    }

    private static int bfs(int s) {
        boolean[] marked = new boolean[g.V()]; // marked[v] = is there an s-v path
        Queue<Integer> q = new LinkedList<Integer>();
        marked[s] = true;
        q.add(s);
        int count = 1;
        while (!q.isEmpty()) {
            int v = q.remove();
            for (NetworkEdge e : g.adj(v)) {
                int w = e.to();
                if (!marked[w] && e.type() == NetworkEdge.Material.COPPER) {
                    marked[w] = true;
                    count++;
                    q.add(w);
                }
            }
        }
        return count;
    }

    /*
        Prints out edges from first to second
        Finds path with lowest amount of time
        Outputs lowest bandwidth among edge.
        Uses Dijikstra
     */
    public static void calculateLowestPath(int v1, int v2) {
        if(shortestPaths.hasPath(v1, v2)) {
            System.out.println(v1);
            int minBandwidth = Integer.MAX_VALUE;
            for(NetworkEdge e : shortestPaths.path(v1, v2)) {
                minBandwidth = Math.min(minBandwidth, e.bandwidth());
                System.out.println(e.toPrettyString());
            }
            System.out.println("Bandwidth available across this path: " + minBandwidth);
        } else {
            System.out.println("There is no path between those vertices!");
        }
    }

    public static void askOptionLoop() {
        int opt = getUserOption();
        while(opt > -1) {
            performAction(opt);
            showOptions();
            opt = getUserOption();
        }
        System.out.println("\nWould you like to exit the program? Type \"no\" for no, anything else to exit.");
        String exitStr = scanner.nextLine();
        if("no".equals(exitStr)) {
            showOptions();
            askOptionLoop();
        }
    }

    public static void startMenu() {
        System.out.println("============================================================");
        System.out.println("                Network Connectivity Program                ");
        System.out.println("============================================================");
        showOptions();
        askOptionLoop();
        System.out.println("Thank you!");
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Must specify filename as argument.");
            return;
        }
        scanner = new Scanner(System.in);
        String fileName = args[0];
        loadFromFile(fileName);
        startMenu();
        scanner.close();
    }
}
