/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author scottdykstra
 */
public class Timer implements Runnable {
    private long startTime;
    public int seconds;
    public int millis;
    Main app;
    boolean reset;
    
    public Timer(Main appIn) {
        reset = false;
        app = appIn;
        startTime = System.nanoTime();
        Thread timer = new Thread(this);
        timer.start();
    }
    
    @Override
    public void run() {
        double previousSecond = 0;
        while(true) {
            if(reset) {
                reset = false;
                startTime = System.nanoTime();
                System.out.println("Game timer has reset!");
            }
            double elapsedTime = (double)(System.nanoTime()-startTime);
            seconds = (int)((double)elapsedTime / 1000000000.0);   
            millis = (int)((double)elapsedTime / 10000000.0);  
            if(app.topBar.isScoring()) {
                if(seconds != previousSecond) {
                    previousSecond = seconds;
                    app.topBar.addPoint();
                }
            }
        }
    }
    
}
    
