import java.io.Serializable;

public class PrivateKey implements Serializable {
    public LargeInteger d;
    public LargeInteger n;

    public PrivateKey(LargeInteger n, LargeInteger d) {
        this.d = d;
        this.n = n;
    }
}