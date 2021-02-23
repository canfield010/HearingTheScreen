import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.time.ZonedDateTime;
import java.util.Random;
import java.lang.Thread;
import java.util.stream.IntStream;
import java.lang.Math;

public class HearingTheScreenImproved {
    public final static int resolution = 4;
    public static double[][] frequencies = new double[resolution][resolution];
    public static void main(String[] args) throws LineUnavailableException {
        final int duration = 1000000;
        int[][] brightness = new int[resolution][resolution];
        for (int x = 0; x<resolution; x++) {
            for (int y = 0; y<resolution; y++) {
                frequencies[x][y] = hilberts(resolution, x, y)/((double)(resolution*resolution)/1024D) + 150D;
                //frequencies[pos][0] = position/1 + 150;
                //frequencies[pos][0] = (double)position/((double)(resolution*resolution)/(double)1024) + 150;//adding these doubles fixed it completely
            }
        }

        //AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
        //SourceDataLine sdl = AudioSystem.getSourceDataLine( af );
        //sdl.open();
        //sdl.start();
        //Robot robot;
        final Rectangle rect = getMaximumScreenBounds();
        final int screenWidth = rect.width;
        final int screenHeight = rect.height;

        //playSound(sdl, frequencies, 2000);

        try {
            //robot = new Robot();
            int[] RGB = new int[3];
            java.awt.image.BufferedImage image;
            int count = 0;
            ScreenShot screenShot = new ScreenShot();
            screenShot.start();
            PlayingSound playingSound = new PlayingSound();
            playingSound.start();
            long t = ZonedDateTime.now().toInstant().toEpochMilli();
            while (ZonedDateTime.now().toInstant().toEpochMilli()<t + duration) {
                //System.out.println("sampled");
                //image = robot.createScreenCapture(rect);
                //System.out.println("sampled");
                while (!screenShot.has) {
                    Thread.sleep(1L);
                }
                //System.out.println("should have");
                //screenShot.wait();
                image = screenShot.image;
                //screenShot.notify();
                for (int x = 0; x<resolution; x++) {
                    //System.out.println("sampled");
                    for (int y = 0; y<resolution; y++) {
                        //screenShot.interrupt();
                        //screenShot.wait();

                        int rgb = image.getRGB((x*screenWidth)/resolution, (y*screenHeight)/resolution);
                        //screenShot.notify();
                        RGB[0] = (rgb>>16)&0xFF;
                        RGB[1] = (rgb>>8)&0xFF;
                        RGB[2] = rgb&0xFF;
                        int bright = (Math.max(RGB[0], Math.max(RGB[1], RGB[2])) + Math.min(RGB[0], Math.min(RGB[1], RGB[2])))/2;
                        //double position = hilberts(resolution, x, y);
                        //int pos = x + (y*resolution);
                        //frequencies[pos][0] = position;
                        brightness[x][y] = bright;
                    }
                }
                //System.out.println("sampled");

                //playSound(sdl, frequencies, 300);
                playingSound.setBrightnesses(brightness);

                //System.out.println("Iterated");
                count++;
            }
            //screenShot.interrupt();
            //screenShot.suspend();
            //screenShot.stop();
            screenShot.exit = true;
            playingSound.exit = true;
            //System.out.println("Color reading ready at " + Long.toString(ZonedDateTime.now().toInstant().toEpochMilli() - t));
        } catch (Exception e) {
            //System.out.println("here it is!");
            System.err.println(e);
            System.out.println(e);
        }
        //sdl.drain();
        //sdl.stop();
    }
    /*public static void playSound(SourceDataLine sdl, double[][] frequencies, int time) {
        byte[] buf = new byte[1];
        double angle;
        //double value = 0;
        float value = 0;
        for (int i = 0; i < time*(float)44100/1000; i++) {
            for (double[] frequency: frequencies) {
                angle = i/((float)44100 / frequency[0])*2.0*Math.PI;
                value += (float)Math.sin(angle)*(float)(frequency[1]/256.0);
            }
            buf[0] = (byte)(value/frequencies.length);
            sdl.write( buf, 0, 1 );
        }
    }*/
    public static Rectangle getMaximumScreenBounds() {
        int minx=0, miny=0, maxx=0, maxy=0;
        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for(final GraphicsDevice device : environment.getScreenDevices()){
            final Rectangle bounds = device.getDefaultConfiguration().getBounds();
            minx = Math.min(minx, bounds.x);
            miny = Math.min(miny, bounds.y);
            maxx = Math.max(maxx,  bounds.x+bounds.width);
            maxy = Math.max(maxy, bounds.y+bounds.height);
        }
        return new Rectangle(minx, miny, maxx-minx, maxy-miny);
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

class ScreenShot extends Thread {
    public java.awt.image.BufferedImage image;
    //private java.awt.image.BufferedImage imageStorage;
    public boolean has = false;
    public boolean exit = false;
    public void run() {
        //System.out.println("progress");
        try {
            Robot robot = new Robot();
            final Rectangle rect = HearingTheScreenImproved.getMaximumScreenBounds();
            //System.out.println("moreProgress");
            while (!exit) {
                //this.imageStorage = robot.createScreenCapture(rect);
                //System.out.println("trying");
                this.image = robot.createScreenCapture(rect);
                //System.out.println("suceeded");
                this.has = true;
                //this.image = imageStorage;
                //System.out.println("screenshotted");
            }
        } catch (Exception e) {
            System.out.println("oof");
        }
    }
}

class PlayingSound extends Thread {
    byte[] buf = new byte[1];
    double angle;
    double value = 0;
    double[][] frequencies = new double[HearingTheScreenImproved.resolution][HearingTheScreenImproved.resolution];
    int[][] brightness;
    double i = 0;
    Random r = new Random();
    public boolean exit = false;
    boolean sin = false;
    boolean paralell = true;
    double highestVal = 0;
    public void run() {
        for (int x = 0; x<HearingTheScreenImproved.resolution; x++) {
            for (int y = 0; y<HearingTheScreenImproved.resolution; y++) {
                frequencies[x][y] = HearingTheScreenImproved.hilberts(HearingTheScreenImproved.resolution, x, y)/((double)(HearingTheScreenImproved.resolution*HearingTheScreenImproved.resolution)/1024D) + 150D;
                //frequencies[pos][0] = position/1 + 150;
                //frequencies[pos][0] = (double)position/((double)(resolution*resolution)/(double)1024) + 150;//adding these doubles fixed it completely
            }
        }
        try {
            AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
            SourceDataLine sdl = AudioSystem.getSourceDataLine( af );
            sdl.open();
            //System.out.println("got here");
            sdl.start();
            //System.out.println("but Here?");
            boolean myBool = !exit;
            myBool = brightness == null;
            //System.out.println("how about here?");
            while (brightness==null && !exit) {
                //System.out.println("here");
                Thread.sleep(1L);
                //System.out.println("there");
            }
            //System.out.println("past the loop!");
            while (!exit) {
                //System.out.println("has frequencies");
                value = 0;
                r.setSeed(0);

                if (paralell) {
                    //System.out.println("in if statement");
                    double[] paralellValue = new double[1470];
                    IntStream.range(0, brightness.length*brightness.length).parallel().forEach(i -> {
                        //System.out.println("in multi");
                        for (int count = 0; count<1470; count++) {
                            //System.out.println("in for loop");
                            int rValue = r.nextInt(65536);
                            if (sin) {
                                angle = (i + rValue) / ((float) 44100 / (float) frequencies[i][0]) * 2.0 * Math.PI;
                                paralellValue[count] += Math.sin(angle) * (frequencies[i][1]);
                            } else {
                                //System.out.println("in else");
                                angle = (i + rValue) / ((float) 44100 / (float) frequencies[i % frequencies.length][(int) Math.floor((double) i / frequencies.length)]);

                                if (((int) angle) % 2 == 0) {
                                    paralellValue[count] += frequencies[i % frequencies.length][(int) Math.floor((double) i / frequencies.length)]/(double)brightness.length*brightness.length;
                                } else {
                                    paralellValue[count] -= frequencies[i % frequencies.length][(int) Math.floor((double) i / frequencies.length)]/(double)brightness.length*brightness.length;
                                }
                                //System.out.println("end of else");
                            }
                        }
                        //buf[0] = (byte)((value/(double)frequencies.length)*(highestVal/256));
                    });
                    for (int count = 0; count<1470; count++) {
                        buf[0] = (byte)((paralellValue[count]/(double)frequencies.length)*(highestVal/256));
                        sdl.write( buf, 0, 1 );
                    }
                    //sdl.write( buf, 0, 1 );
                } else {
                    for (double[] frequency: frequencies) {

                        int rValue = r.nextInt(65536);

                        if (sin) {
                            angle = (i + rValue)/((float)44100 / (float)frequency[0])*2.0*Math.PI;
                            value += Math.sin(angle)*(frequency[1]);
                        } else {
                            //angle = (i + rValue)/((float)44100 / (float)frequency[0])*2.0*Math.PI;
                            angle = ((double)(i + rValue))/((double)44100 / (double)frequency[0]);

                            //System.out.println((float)44100/(float)frequency[0]);
                            //System.out.println(i);
                            //System.out.println(i/(double)((float)44100/(float)frequency[0]));
                            //System.out.println(angle);

                            if (((int)angle)%2 == 0) {
                                value += frequency[1];///256.0;
                            } else {
                                value -= frequency[1];///256.0;
                            }
                        }
                        //value += Math.sin(angle)*(frequency[1]/256.0);

                        //value += Math.sqrt(Math.sqrt(Math.sin(angle)*(frequency[1]))*256);
                        //value += Math.pow(Math.pow(Math.sin(angle)*(frequency[1]/256.0), 2)/256, 2)/256;
                        if (highestVal<Math.abs(frequency[1])) {
                            highestVal = Math.abs(frequency[1]);
                        }
                    }
                    buf[0] = (byte)((value/(double)frequencies.length)*(highestVal/256));
                    sdl.write( buf, 0, 1 );
                } /////////////////////////////////////////////////////////////////////////////////////////////
                //value = Math.sqrt(value*256);
                //System.out.println(value);

                //System.out.println("making noise");
                //System.out.println(Double.toString((((double)value/(double)frequencies.length)*(highestVal))));
                //System.out.println(Double.toString(value));
                //System.out.println(Double.toString(highestVal));
                //System.out.println(Byte.toString(buf[0]));
                i+=1;
            }
            sdl.drain();
            sdl.stop();
        } catch (Exception e) {
            System.out.println("here it is!");
            System.out.println(e);
        }
    }
    public void setBrightnesses(int[][] brightness) {
        this.brightness = brightness;
        //System.out.println("obtained new frequencies");
    }
    /*public void setFrequencies(double[][] frequencies) {
        this.frequencies = frequencies;
    }*/
}