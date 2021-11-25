package ebikes;


/**
 *
 * @author Sid
 */
public class Bike extends Thread {
    
    private boolean running;
    private int charge;
    private int totalCharge;
    private String id;
    public enum STATUS{AWAITING_CHARGE, CHARGING, READY, IN_USE}
    
    private volatile STATUS status;
    private User currentUser;
    private ChargingStation charger;
    
    private static int totalChargeAllBikes = 0;
    private static int timeForTravel = 20, timeForCharge = 20, intervalInRun = 2;
   
    
    public Bike(String id, ChargingStation s){
        this.id = id;
        this.charger = s;
        this.charge = 100;        
        this.status = STATUS.READY;
        this.setDaemon(true);
        this.currentUser = null;
    }
    
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
    
    public synchronized void startJourney(User user, ChargingStation destination){ /* moves bike from READY to IN_USE */
        currentUser = user;
        charger = destination; 
        this.status = STATUS.IN_USE;
    }
    
    public synchronized void travel(){ /* moves bike from IN_USE to RETURNED */
        int chargeUsed = (int)(Math.random()*charge);
        pause(timeForTravel);
        charge -= chargeUsed;
        currentUser.addToChargeUsed(chargeUsed);
        charger.getControl().addJourney(currentUser, chargeUsed, charger);
        charger.returnBike(this);
        currentUser.returnBike(charger);
        this.status = STATUS.AWAITING_CHARGE;
    }
    
    /* charger moves bike from RETURNED TO CHARGING */

    public synchronized void charging(){ /* moves bike from CHARGING to READY */
        int chargeUsed = 0;
        pause(timeForCharge);
        while(charge < 100){
            charge++;
            chargeUsed++;
            
        }
        totalCharge += chargeUsed;
        totalChargeAllBikes += chargeUsed;
        this.status = STATUS.READY;
        charger.addToReadyList(this, chargeUsed);
        
    }
    
    public void setStatus(STATUS s){ /* moves car from QUEUEING to PARKING */
        this.status = s;
    }

    public String getID(){
        return id;
    }
    
    public int getCharge(){
        return charge;
    }
    
    public String getStatus(){
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
    
    public int getTotalChargeThisBike(){
        return totalCharge;
    }
    
    public static int getTotalChargeAllBikes(){
        return Bike.totalChargeAllBikes;
    }
    
    public static void setIntervals(int msRun, int msTravel, int msCharge){
        intervalInRun = msRun;
        timeForTravel = msTravel;
        timeForCharge = msCharge;
    }
}
