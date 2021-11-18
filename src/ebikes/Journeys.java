/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ebikes;

/**
 *
 * @author Sid
 */
public class Journeys extends Thread {
    
    private Records control;
    private boolean running;
    
    private static int interval = 2;
    
    public Journeys(Records c){
        this.control = c;
        this.setDaemon(true);
    }
    
    @Override public void run(){
        running  = true;
        while(running){
            pause(interval);
            ChargingStation startingPoint = control.getRandomCharger();
            ChargingStation destination = control.getRandomCharger();
            User user = control.getRandomUser();        
            Bike bike = startingPoint.releaseBike();
            bike.startJourney(user, destination);
        }
    }
    
    public void shutdown(){
        running = false;
    }
    
    public static void setInterval(int ms){
        interval = ms;
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
}
