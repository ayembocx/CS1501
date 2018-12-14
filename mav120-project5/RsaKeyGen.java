import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

class RsaKey {
    public LargeInteger p;
    public LargeInteger q;
    public LargeInteger n;
    public LargeInteger phi_n;
    public LargeInteger e;
    public LargeInteger d;

    private LargeInteger ONE = new LargeInteger(new byte[] {(byte) 1});

    public RsaKey(Random rd) {
        p = new LargeInteger(255, rd);
        q = new LargeInteger(256, rd);
        n = p.multiply(q);
        phi_n = p.subtract(ONE).multiply(q.subtract(ONE));
        e = new LargeInteger(new byte[] {(byte) 1, (byte) 0, (byte) 1}); // 65537
        LargeInteger[] xgcd = phi_n.XGCD(e);
        while(!xgcd[0].equals(ONE)) {
            e = new LargeInteger(phi_n.length(), rd);
            xgcd = phi_n.XGCD(e);
        }
        d = xgcd[2].mod(phi_n);
        if(d.isNegative()) {
            d = d.add(phi_n);
        }
    }

    public PubKey getPubKey() {
        return new PubKey(n, e);
    }

    public PrivateKey getPrivateKey() {
        return new PrivateKey(n, d);
    }
}

public class RsaKeyGen {
    public static void main(String[] args) {
        Random rd = new Random();
        RsaKey k = new RsaKey(rd);

        System.out.println("Key info:");
        System.out.println("P:");
        System.out.println(k.p);
        System.out.println("Q:");
        System.out.println(k.q);
        System.out.println("N:");
        System.out.println(k.n);
        System.out.println("PHI_N:");
        System.out.println(k.phi_n);
        System.out.println("E:");
        System.out.println(k.e);
        System.out.println("D:");
        System.out.println(k.d);

        try {
            // create a new file with an ObjectOutputStream
            FileOutputStream out = new FileOutputStream("pubkey.rsa");
            ObjectOutputStream oout = new ObjectOutputStream(out);

            oout.writeObject(k.getPubKey());
            // close the stream
            oout.close();

            out = new FileOutputStream("privkey.rsa");
            oout = new ObjectOutputStream(out);

            oout.writeObject(k.getPrivateKey());

            oout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
