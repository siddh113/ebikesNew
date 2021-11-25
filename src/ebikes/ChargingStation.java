package ebikes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

/**
 *
 * @author Sid
 */
public class ChargingStation extends Thread {
    String stationName;
    Records control;
    
    int capacity;
    int numberChargersFree;
    boolean running;
    
    private Vector<Bike> returnedBikes;
    private Vector<Bike> chargingBikes;
    Vector<Bike> fullyChargedBikes;
    
    int totalChargeIssued;
    private static int interval = 10;    
    
    public ChargingStation(String n, int c, int initial, Records control){
        totalChargeIssued = 0;
        returnedBikes = new Vector();
        chargingBikes = new Vector();
        fullyChargedBikes = new Vector();
        this.stationName = n;
        this.capacity = c;
        this.numberChargersFree = capacity;
        this.control = control;
        this.setDaemon(true);
        for(int i = 0; i < initial; i++) addNewBike(new Bike(stationName.charAt(0)+""+i, this));
    }
    
    @Override public void run(){
        System.out.println(this);
        running = true;
        
        while(running){
          pause(interval);
          checkForReturnedBikes();            
        }
    }
    
    public synchronized void checkForReturnedBikes(){
        if(returnedBikes.size() > 0 && numberChargersFree > 0) {
                Bike bike = returnedBikes.remove(0);
                numberChargersFree--;
                chargingBikes.add(bike);
                bike.setStatus(Bike.STATUS.CHARGING);
        }
    }
    
    
    public void shutdown(){
        running = false;
    }
    
    public synchronized void addNewBike(Bike bike){
        this.fullyChargedBikes.add(bike);
        control.getBikes().add(bike);
        bike.setStatus(Bike.STATUS.READY);
        bike.start();        
    }
    
    public synchronized void returnBike(Bike bike){
        this.returnedBikes.add(bike);
    }
    
    public synchronized void addToReadyList(Bike bike, int chargeUsed){
        this.chargingBikes.remove(bike);
        this.numberChargersFree++;
        this.totalChargeIssued += chargeUsed;
        
        this.fullyChargedBikes.add(bike);
    }
    
    public synchronized Bike releaseBike(){
        while(fullyChargedBikes.size() < 1){pause(1);}
        return fullyChargedBikes.remove(0);      
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
    
    @Override public String toString(){
        return "{" + stationName + " " + numberChargersFree + "|" + capacity 
                + "," + returnedBikes.size() 
                + "," + chargingBikes.size()
                + "," + fullyChargedBikes.size() + "}";
    }
    
    public Vector<Bike> getListOfReturnedBikesAtStation(){
        return this.returnedBikes;
    }
    
    public Vector<Bike> getListOfChargingBikesAtStation(){
        return this.chargingBikes;
    }
    
    public Vector<Bike> getListOfFullyChargedBikesAtStation(){
        return this.fullyChargedBikes;
    }
    
    public int getNumberChargersFree(){
        return this.numberChargersFree;
    }
    
    public String getStationName(){
        return stationName;
    }
    
    public Records getControl(){
        return control;
    }
    
    public int getTotalChargeIssued(){
        return this.totalChargeIssued;
    }
    
    public static void setInterval(int ms){
        interval = ms;
    }
}
