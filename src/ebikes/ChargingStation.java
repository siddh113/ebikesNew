package ebikes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sid
 */
public class ChargingStation extends Thread {
    
    /* Properties of chatgingStationClass */
    String stationName;
    Records control;
    
    int capacity;
    int numberChargersFree;
    boolean running;
    
    /* Initializing a executor object */
    Executor executor = Executors.newCachedThreadPool();
    
    /* Collections stored within the chargingStation Object */
    private ArrayBlockingQueue<Bike> returnedBikes;
    private ArrayBlockingQueue<Bike> chargingBikes;
    CopyOnWriteArrayList<Bike> fullyChargedBikes;
    
    int totalChargeIssued;
    private static int interval = 10; 
    
    /* Initializing lock variable */
    private static ReentrantLock lock;
    
    /* ChargingStation Constructor */
    public ChargingStation(String n, int c, int initial, Records control){
        totalChargeIssued = 0;
        returnedBikes = new ArrayBlockingQueue(10000);
        chargingBikes = new ArrayBlockingQueue(10000);
        fullyChargedBikes = new CopyOnWriteArrayList();
        this.stationName = n;
        this.capacity = c;
        this.numberChargersFree = capacity;
        this.control = control;
        lock = new ReentrantLock();
        this.setDaemon(true);
        for(int i = 0; i < initial; i++) addNewBike(new Bike(stationName.charAt(0)+""+i, this));
    }
    
    /* Runnable for the class */
    @Override public void run(){
        System.out.println(this);
        running = true;
        
        while(running){
          pause(interval);
          checkForReturnedBikes();            
        }
    }
    
    /* Method that will check if the bike was returned after checking or not */
    public void checkForReturnedBikes(){
        try{
            lock.lock();
            if(returnedBikes.size() > 0 && numberChargersFree > 0) {
              Bike bike = null;
              try {
                bike = returnedBikes.take();
            } catch (InterruptedException ex) {}
                numberChargersFree--;
                chargingBikes.add(bike);
                bike.setStatus(Bike.STATUS.CHARGING);
        }
        }finally{
            lock.unlock();
        }
    }
    
    
    public void shutdown(){
        running = false;
    }
    
    /* Method to add new bikes to charge */
    public void addNewBike(Bike bike){
        try{
            lock.lock();
            this.fullyChargedBikes.add(bike);
            control.getBikes().add(bike);
            bike.setStatus(Bike.STATUS.READY);
            executor.execute(bike);                 // Using executor to start a thread in a thread safe manner
        }finally{
            lock.unlock();
        }
    }
    
    /* This method returns all the charged bikes in a list */
    public void returnBike(Bike bike){
        try{
            lock.lock();
             this.returnedBikes.add(bike);
        }finally{
            lock.unlock();
        }
    }
    
    /* Method to add all the fully charged bikes to a list */
    public void addToReadyList(Bike bike, int chargeUsed){
        try{
            lock.lock();
            this.chargingBikes.remove(bike);
            this.numberChargersFree++;
            this.totalChargeIssued += chargeUsed;
        
            this.fullyChargedBikes.add(bike);
        }finally{
            lock.unlock();
        }
    }
    
    /* Method to remove a fully charged bike from the list */
    public Bike releaseBike(){
        try{
            lock.lock();
            while(fullyChargedBikes.size() < 1){pause(1);}
            return fullyChargedBikes.remove(0);
        }finally{
            lock.unlock();
        }
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
    
    /* Getters and setters */
    
    public synchronized ArrayBlockingQueue<Bike> getListOfReturnedBikesAtStation(){
        return this.returnedBikes;
    }
    
    public synchronized ArrayBlockingQueue<Bike> getListOfChargingBikesAtStation(){
        return this.chargingBikes;
    }
    
    public synchronized CopyOnWriteArrayList<Bike> getListOfFullyChargedBikesAtStation(){
        return this.fullyChargedBikes;
    }
    
    public synchronized int getNumberChargersFree(){
        return this.numberChargersFree;
    }
    
    public String getStationName(){
        return stationName;
    }
    
    public Records getControl(){
        return control;
    }
    
    public synchronized int getTotalChargeIssued(){
        return this.totalChargeIssued;
    }
    
    public static void setInterval(int ms){
        interval = ms;
    }
}
