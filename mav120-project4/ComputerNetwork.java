import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ComputerNetwork {
    private int V;
    private int E;
    /*
     The book implementation uses a Bag. I changed this to use a Linked List.

    */
    private LinkedList<NetworkEdge>[] adj;

    @SuppressWarnings("unchecked")
    public ComputerNetwork(int V) {
        this.V = V;
        this.E = 0;
        adj = (LinkedList<NetworkEdge>[])(new LinkedList[V]);
        for(int v=0; v < V; v++)
            adj[v] = new LinkedList<NetworkEdge>();
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public void addEdge(NetworkEdge e) {
        int v = e.from();
        int w = e.to();
        validateVertex(v);
        validateVertex(w);
        adj[v].add(e);
        adj[w].add(e);
        E++;
    }

    public Iterable<NetworkEdge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    public Iterable<NetworkEdge> edges() {
        Set<NetworkEdge> list = new HashSet<>();
        for(int v=0; v < V; v++) {
            for(NetworkEdge e : adj(v)) {
                if(e.to() != v)
                    list.add(e);
            }
        }
        return list;
    }

    public int V() { return V; }
    public int E() { return E; }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("V: " + V + " E: " + E + '\n');
        for (int v = 0; v < V; v++) {
            s.append(v + ":  ");
            for (NetworkEdge e : adj[v]) {
                if (e.to() != v) s.append(e + "  ");
            }
            s.append('\n');
        }
        return s.toString();
    }
}
