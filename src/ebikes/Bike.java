package ebikes;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author Sid
 */
public class Bike extends Thread {
    
    /* Properties of the bike class */
    private boolean running;
    private int charge;
    private int totalCharge;
    private String id;
    public enum STATUS{AWAITING_CHARGE, CHARGING, READY, IN_USE}
    
    private volatile STATUS status;
    private User currentUser;
    private ChargingStation charger;
    
    private static AtomicInteger totalChargeAllBikes = new AtomicInteger(0);
    private static int timeForTravel = 20, timeForCharge = 20, intervalInRun = 2;
    
    private static ReentrantLock lock;
   
    /* Bike constructor */
    public Bike(String id, ChargingStation s){
        this.id = id;
        this.charger = s;
        this.charge = 100;        
        this.status = STATUS.READY;
        this.setDaemon(true);
        this.currentUser = null;
        lock = new ReentrantLock();
    }
    
    /* Runnable for the class */
    @Override public void run(){
        running = true;
        while(running){
            if(status == STATUS.CHARGING) charging();
            else if(status == STATUS.IN_USE) travel();
            
            pause(intervalInRun);
        }       
    }
    
    public void shutdown(){
        running = false;
    }
    
    /* moves bike from READY to IN_USE */
    public void startJourney(User user, ChargingStation destination){
        try{
            lock.lock();
            currentUser = user;
            charger = destination; 
            this.status = STATUS.IN_USE;
        }finally{
            lock.unlock();
        }       
    }
    
    /* moves bike from IN_USE to RETURNED */
    public void travel(){ 
        try{
            lock.lock();
            int chargeUsed = (int)(Math.random()*charge);
            pause(timeForTravel);
            charge -= chargeUsed;
            currentUser.addToChargeUsed(chargeUsed);
            charger.getControl().addJourney(currentUser, chargeUsed, charger);
            charger.returnBike(this);
            currentUser.returnBike(charger);
            this.status = STATUS.AWAITING_CHARGE;
        }finally{
            lock.unlock();
        }
    }
    
    /* charger moves bike from RETURNED TO CHARGING
     * moves bike from CHARGING to READY */
    public void charging(){
        try{
            lock.lock();
            int chargeUsed = 0;
            pause(timeForCharge);
            while(charge < 100){
                charge++;
                chargeUsed++;         
            }
            totalCharge += chargeUsed;
            totalChargeAllBikes.addAndGet(chargeUsed);
            this.status = STATUS.READY;
            charger.addToReadyList(this, chargeUsed);
        }finally{
            lock.unlock();
        }       
    }
    
    /* moves car from QUEUEING to PARKING */
    public synchronized void setStatus(STATUS s){ 
        this.status = s;
    }

    /* Getters and setters */
    
    public String getID(){
        return id;
    }
    
    public synchronized int getCharge(){
        return charge;
    }
    
    public synchronized String getStatus(){
        return status.toString();
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
    
    public String toString(){
        return "Bike " + id + ": " + status + " charger="+ charger.getStationName() + " charge=" + charge;
    }
    
    public synchronized int getTotalChargeThisBike(){
        return totalCharge;
    }
    
    public static int getTotalChargeAllBikes(){
        return Bike.totalChargeAllBikes.get();
    }
    
    public static void setIntervals(int msRun, int msTravel, int msCharge){
        intervalInRun = msRun;
        timeForTravel = msTravel;
        timeForCharge = msCharge;
    }
}
