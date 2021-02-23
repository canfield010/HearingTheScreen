//package Javas.playingTheAudio;
/*
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

public class HearingTheScreen {
    public static void main(String[] args) throws LineUnavailableException {
        final int duration = 1000000;
        final int resolution = 32;
        double[][] frequencies = new double[resolution*resolution][2];
        for (int x = 0; x<resolution; x++) {
            for (int y = 0; y<resolution; y++) {
                double position = hilberts(resolution, x, y);
                int pos = x + (y*resolution);
                //frequencies[pos][0] = position/1 + 150;
                frequencies[pos][0] = (double)position/((double)(resolution*resolution)/(double)1024) + 150;//adding these doubles fixed it completely
            }
        }
        
        
        //byte[] buf = new byte[ 1 ];;
        AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
        SourceDataLine sdl = AudioSystem.getSourceDataLine( af );
        sdl.open();
        sdl.start();
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
                        RGB[2] = (rgb>>0)&0xFF;
                        int brightness = (Math.max(RGB[0], Math.max(RGB[1], RGB[2])) + Math.min(RGB[0], Math.min(RGB[1], RGB[2])))/2;
                        //double position = hilberts(resolution, x, y);
                        //int pos = x + (y*resolution);
                        //frequencies[pos][0] = position;
                        frequencies[x + y*resolution][1] = brightness;
                    }
                }
                //System.out.println("sampled");

                //playSound(sdl, frequencies, 300);
                playingSound.setBrightnesses(frequencies);

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
            System.err.println(e);
            System.out.println(e);
        }
        sdl.drain();
        sdl.stop();
    }
    public static void playSound(SourceDataLine sdl, double frequencies[][], int time) {
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
    }
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
        final Rectangle rect = HearingTheScreen.getMaximumScreenBounds();
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
    double frequencies[][];
    double i = 0;
    Random r = new Random();
    public boolean exit = false;
    boolean sin = false;
    boolean paralell = false;
    double highestVal = 0;
    public void run() {
        try {
        AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
        SourceDataLine sdl = AudioSystem.getSourceDataLine( af );
        sdl.open();
        sdl.start();
        while (frequencies==null && !exit) {
            Thread.sleep(1L);
        }
        while (!exit) {
            //System.out.println("has frequencies");
            value = 0;
            r.setSeed(0);

            if (paralell) {
                IntStream.range(0, frequencies.length).parallel().forEach(i -> {
                    int rValue = r.nextInt(65536);
                    if (sin) {
                        angle = (i + rValue)/((float)44100 / (float)frequencies[i][0])*2.0*Math.PI;
                        value += Math.sin(angle)*(frequencies[i][1]);
                    } else {
                        angle = (i + rValue)/((float)44100 / (float)frequencies[i][0]);

                        if (((int)angle)%2 == 0) {
                            value += frequencies[i][1];
                        } else {
                            value -= frequencies[i][1];
                        }
                    }
                });
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
            }
            //value = Math.sqrt(value*256);
            //System.out.println(value);
            buf[0] = (byte)((value/(double)frequencies.length)*(highestVal/256));
            sdl.write( buf, 0, 1 );
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
            System.err.println(e);
        }
    }
    public void setFrequencies(double frequencies[][]) {
        this.frequencies = frequencies;
        //System.out.println("obtained new frequencies");
    }
}*/