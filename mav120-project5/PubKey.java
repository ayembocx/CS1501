import java.io.Serializable;

public class PubKey implements Serializable {
    public LargeInteger e;
    public LargeInteger n;

    public PubKey(LargeInteger n, LargeInteger e) {
        this.e = e;
        this.n = n;
    }
}