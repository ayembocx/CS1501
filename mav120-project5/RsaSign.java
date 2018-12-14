import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RsaSign {
    static String filename;
    static LargeInteger hash;

    public static void saveToSigFile(String filename, LargeInteger sig) {
        byte data[] = sig.getVal();
        try {
            FileOutputStream out = new FileOutputStream(filename+".sig");
            out.write(data);
            out.close();
        } catch(Exception e) {
            System.out.println("Could not save signature file. ");
            System.exit(1);
        }
    }

    public static LargeInteger readFromSigFile(String _filename) {
        try {
            Path fileLocation = Paths.get(filename+".sig");
            byte[] data = Files.readAllBytes(fileLocation);
            return new LargeInteger(data);

        } catch (Exception e) {
            System.out.println("Could not find signature file " + filename + ".sig");
            System.exit(1);
            return null;
        }


    }

    public static Object readKeyFile(String _filename) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(_filename));
            return (ois.readObject());
        } catch(Exception e) {
            System.out.println("Make sure " + _filename + " is in this directory.");
            System.exit(1);
            return null;
        }
    }

    public static void sign() {
        PrivateKey pk = (PrivateKey)readKeyFile("privkey.rsa");

        LargeInteger sig = hash.modularExp(pk.d, pk.n);
        saveToSigFile(filename, sig);
    }

    public static void verify() {
        PubKey pk = (PubKey)readKeyFile("pubkey.rsa");
        LargeInteger sig = readFromSigFile(filename);
        System.out.println();
        LargeInteger m = sig.modularExp(pk.e, pk.n);

        boolean isSame = m.subtract(hash).isZero();
        if(isSame) {
            System.out.println("The signature is valid.");
        } else {
            System.out.println("The signature is not valid.");
        }
    }
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Must have two arguments. s or v and then the filename");
            return;
        }
        char type = args[0].charAt(0);
        filename = args[1];
        hash = HashEx.generateHash(filename);

        if(type == 's') sign();
        else verify();


    }
}
