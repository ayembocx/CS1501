/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width

    private static final int MAX_W = 16;
    private static boolean maxCodeBook = false;

    private static void writeMode(char mode) {
        switch (mode) {
            case 'r':
                BinaryStdOut.write(false);
                BinaryStdOut.write(true);
                break;

            case 'm':
                BinaryStdOut.write(true);
                BinaryStdOut.write(true);
                break;

            case 'n':
                BinaryStdOut.write(false);
                BinaryStdOut.write(false);
                break;
        }
    }

    private static char readMode() {
        int code = BinaryStdIn.readInt(2);
        char[] codemap = {'n', 'r', '0','m'};
        if(code == 2) throw new UnsupportedOperationException("This file was not compressed with a mode");
        return codemap[code];
    }

    private static void updateWidth(int codeval, char mode) {
        W++;
        if(W > MAX_W) {
            W = MAX_W;
            maxCodeBook = true;
        } else {
            L *= 2;
        }
    }

    public static TST<Integer> initCodebook() {
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        return st;
    }

    public static String[] initArrayCodebook() {
        // initialize symbol table with all 1-character strings
        int i;
        String[] st = new String[(int)Math.pow(2, MAX_W)];
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF
        return st;
    }

    private static class CompressionRatioCalculator {
        public final double THRESHOLD = 1.1;

        public int uncompressed = 0;
        public int compressed = 0;


        public double oldRatio = -1;
        public double newRatio = -1;

        public void add(int length) {
            uncompressed += 8 * length;
            compressed += W;
        }

        public void resetRatio() {
            oldRatio = -1;
        }

        public void calculateRatios() {
            if(oldRatio == -1) {
                oldRatio = ((double)(uncompressed))/(compressed);
            }
            newRatio = ((double)uncompressed)/compressed;
        }

        public boolean isExceedingThreshold() {
            return (oldRatio/newRatio) > THRESHOLD;
        }
    }

    public static void compress(char mode) {
        writeMode(mode);
        String input = BinaryStdIn.readString();
        int offset = 0;
        TST<Integer> st = initCodebook();
        int code = R+1;  // R is codeword for EOF
        CompressionRatioCalculator crc = new CompressionRatioCalculator();
        while (input.length() > offset) {
            String s = st.longestPrefixOf(input, offset);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            crc.add(t);
            if(code == L && !maxCodeBook) updateWidth(code, mode);

            if(maxCodeBook) {
                boolean resetCodebook = false;
                if(mode == 'r') {
                    resetCodebook = true;
                } else if(mode == 'm') {
                    crc.calculateRatios();
                    resetCodebook = crc.isExceedingThreshold();
                }
                if(resetCodebook) {
                    crc.resetRatio();
                    maxCodeBook = false;
                    W = 9;
                    L = 512;
                    code = R+1;
                    st = initCodebook();
                }
            }
            if (offset + t < input.length() && code < L) {
                // Add s to symbol table.
                String codeword = input.substring(offset, offset + t + 1);
                st.put(codeword, code++);

            }
            offset += t;
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        String[] st = initArrayCodebook();
        char mode = readMode();
        int i = R+1; // next available codeword value

        CompressionRatioCalculator crc = new CompressionRatioCalculator();
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        while (true) {
            crc.add(val.length());
            BinaryStdOut.write(val);

            if (i == L && !maxCodeBook) {
                updateWidth(i, mode);
            }
            if(maxCodeBook) {
                boolean resetCodebook = false;
                if (mode == 'r') {
                    resetCodebook = true;
                } else if(mode == 'm') {
                    crc.calculateRatios();
                    resetCodebook = crc.isExceedingThreshold();
                }

                if(resetCodebook) {
                    st = initArrayCodebook();
                    crc.resetRatio();
                    maxCodeBook = false;
                    W = 9;
                    L = 512;
                    i = R+1;
                }
            }

            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;

            String s = st[codeword];

            if (i == codeword) s = val + val.charAt(0);   // special case hack

            if (i < L) {
                st[i++] = val + s.charAt(0);
            }

            val = s;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress(args[1].charAt(0));
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
