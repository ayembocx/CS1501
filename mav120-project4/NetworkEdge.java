public class NetworkEdge {
    private static final double FLOATING_POINT_EPSILON = 1E-10;
    private final int COPPER_SPEED = 230000000;
    private final int OPTIC_SPEED = 200000000;

    private final int v;             // from
    private final int w;             // to
    private int bandwidth;   // bandwidth
    private int flow;             // flow
    private int length;
    private double time;

    enum Material {
        COPPER, OPTICAL
    }

    private Material type;

    public NetworkEdge(int v, int w, int bandwidth, int length, String type) {
        if (v < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (w < 0) throw new IllegalArgumentException("vertex index must be a non-negative integer");
        if (!(bandwidth >= 0.0))  throw new IllegalArgumentException("edge bandwidth must be non-negative");
        this.flow = 0;
        this.v = v;
        this.w = w;
        this.bandwidth = bandwidth;
        this.length = length;
        this.type = type.equals("copper") ? Material.COPPER : Material.OPTICAL;
        this.time = calculateTime();
    }

    public NetworkEdge(NetworkEdge e) {
        this.v = e.v;
        this.w = e.w;
        this.bandwidth = e.bandwidth;
        this.flow = e.flow;
        this.type = e.type;
        this.length = e.length;
    }

    public int from() {
        return v;
    }

    public int to() {
        return w;
    }

    public int bandwidth() {
        return bandwidth;
    }

    public int flow() {
        return flow;
    }

    public double time() {
        return time;
    }

    public Material type() { return type; }

    private double calculateTime() {
        double secondsPerMeter;
        if(this.type == Material.COPPER) {
            secondsPerMeter = ((double) 1)/COPPER_SPEED;
        } else {
            secondsPerMeter = ((double) 1)/OPTIC_SPEED;
        }
        // d = rt, t = d/r
        return length * secondsPerMeter * Math.pow(10, 9); // convert to nanoseconds
    }

    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new IllegalArgumentException("invalid endpoint");
    }

    public double residualCapacityTo(int vertex) {
        if      (vertex == v) return flow;              // backward edge
        else if (vertex == w) return bandwidth - flow;   // forward edge
        else throw new IllegalArgumentException("invalid endpoint");
    }

    public void addResidualFlowTo(int vertex, double delta) {
        if (!(delta >= 0.0)) throw new IllegalArgumentException("Delta must be nonnegative");

        if      (vertex == v) flow -= delta;           // backward edge
        else if (vertex == w) flow += delta;           // forward edge
        else throw new IllegalArgumentException("invalid endpoint");

        // round flow to 0 or bandwidth if within floating-point precision
        if (Math.abs(flow) <= FLOATING_POINT_EPSILON)
            flow = 0;
        if (Math.abs(flow - bandwidth) <= FLOATING_POINT_EPSILON)
            flow = bandwidth;

        if (!(flow >= 0.0))      throw new IllegalArgumentException("Flow is negative");
        if (!(flow <= bandwidth)) throw new IllegalArgumentException("Flow exceeds bandwidth");
    }


    public void resetFlow() {
        flow = 0;
    }

    public String toString() {
        return v + "->" + w + " " + flow + "/" + bandwidth;
    }

    public String toPrettyString() {
        return "From [" + v + "] to [" + w + "] via " + type + " wire with bandwidth of " + bandwidth + " megabits per second.";
    }
}