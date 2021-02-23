import java.util.Random;

public class HilbertsCurve {
    public static void main(String[] args) {
        /*int n = 2;
        int x = 0;
        int y = 1;*/
        //int value = hilberts(n, x, y);

        Random r = new Random();
        r.setSeed(0);
        int value = r.nextInt(64);
        System.out.println(value);
    }

    public static int hilberts(int n, int x, int y) {
        boolean rx;
        boolean ry;
        int d = 0;
        for (int s = n/2; s>0; s/=2) {
            rx = (x&s) > 0;
            ry = (y&s) > 0;
            if (rx) {
                if (ry) {
                    d += s*s*(3 ^ 1);
                } else {
                    d += s*s*(3 ^ 0);
                }
            } else {
                if (ry) {
                    d += s*s*(0 ^ 1);
                } else {
                    d += s*s*(0 ^ 0);
                }
            }

            if (!ry) {
                if (rx) {
                    x = n-1 - x;
                    y = n-1 - y;
                }
                int t = x;
                x = y;
                y = t;
            }
        }
        return d;
    }
}
