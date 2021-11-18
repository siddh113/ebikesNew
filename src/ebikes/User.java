package ebikes;

/**
 *
 * @author Sid
 */
public class User 
{
    private final int accountNumber;
    private Bike currentBike;
    private int totalChargeUsed;
    
    private static int numberUsers = 0;
    private static int totalChargeUsedAllUsers;
    
    public User(){
        this.accountNumber = 1000 + (numberUsers++);
        this.totalChargeUsed = 0;
        this.currentBike = null;
    }
    
    public synchronized Bike takeBike(ChargingStation charger){
        currentBike  = charger.releaseBike();
        return currentBike;
    }
    
    public synchronized void returnBike(ChargingStation charger){
        currentBike = null;
    }
    
    public Bike getCurrentBike(){
        return this.currentBike;
    }
       
    public int getAccountNumber(){
        return this.accountNumber;
    }
    
    public int getTotalChargeUsed(){
        return this.totalChargeUsed;
    }
    
    public synchronized void addToChargeUsed(int used){
        this.totalChargeUsed += used;
        User.totalChargeUsedAllUsers += used;
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
    
    public static int getTotalChargeAllUsers(){
        return User.totalChargeUsedAllUsers;
    }
    
}
