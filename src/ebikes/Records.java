/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ebikes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sid
 */
public class Records extends Thread{
    Vector<Bike> bikes;
    Vector<User> users;
    Vector<ChargingStation> chargers;
    
    HashMap<User, Vector<Integer>> journeys;
    
    Journeys journeyPlanner;
    
    GUI gui;
    
    boolean running;
    
    public Records(GUI gui){
        this.gui = gui;
        journeys = new HashMap();
        chargers = new Vector();
        bikes = new Vector();
        users = new Vector();
        journeyPlanner = new Journeys(this);
        
        testSetUp();     
    }
    
    public void testSetUp(){
        String[] sites = new String[]{"RGUCampus","Union St","King St","Woolmanhill","BusStation"};
        int[] capacities = new int[]{100,100,100,100,100};
        int[] initialBikes = new int[]{50,50,50,50,50};
        
        for(int i = 0; i<capacities.length; i++){
            chargers.add(new ChargingStation(sites[i], capacities[i], initialBikes[i], this));
        }
        
        for(int i = 0; i < 1000; i++){
            users.add(new User());
            journeys.put(users.get(i), new Vector());
        }
        System.out.println("There are " + users.size() + " users");
        
    }
    
    @Override public void run(){
        
        for(ChargingStation c: chargers) {
            c.start();
        }
            
        journeyPlanner.start();
        
        running = true;
        
//        while(running){
//            pause(500);
//            //gui.updateData(); // Now GUI is responsible for its own updates.
//        }
    }
    
    public void shutdown(){
        journeyPlanner.shutdown();
        try {
                journeyPlanner.join();
                System.out.println("Journey Planner is terminated");
            } catch (InterruptedException ex) {
                System.out.println("Error shutting journeyPlanner");
            }
        
        for(ChargingStation c: chargers) {
            c.shutdown();
        }
        for(ChargingStation c: chargers) {
            try {
                c.join();
                System.out.println(c + "thread is terminated");
            } catch (InterruptedException ex) {
                System.out.println("Error shutting " + c);
            }
        }
        
        running = false;       
    }
    
    public void addJourney(User user, int chargeUsed, ChargingStation charger){
        journeys.get(user).add(chargeUsed);
        gui.addToLog("User " + user.getAccountNumber() + " travelled to " + charger.getStationName() + " using " + chargeUsed + " of charge" );
    }
    
    public void getFullReport(){
        for(ChargingStation c: chargers){
            System.out.println(c);
            for(Bike b: c.getListOfReturnedBikesAtStation()) System.out.println(b);
            for(Bike b: c.getListOfChargingBikesAtStation()) System.out.println(b);
            for(Bike b: c.getListOfFullyChargedBikesAtStation()) System.out.println(b);
        }
//        for(User u: journeys.keySet()){
//            System.out.println(journeys.get(u));            
//        }
    }
            
    public ChargingStation getRandomCharger(){
        int number = (int)(Math.random()*chargers.size());
        return chargers.get(number);
    }
    
    public User getRandomUser(){
        while(true){
            //pause(1);
            int number = (int)(Math.random()*users.size());           
            User user = users.get(number);           
            if(user.getCurrentBike() == null) return user;
        }        
    }
    
    public Vector<Bike> getBikes(){
        return bikes;
    }
    
    public Vector<User> getUsers(){
        return users;
    }
    
    public HashMap<User, Vector<Integer>> getJourneys(){
        return journeys;
    }
    
    public int getNumberJourneys(){
        int number = 0;
        for(User u: journeys.keySet()){
            number += journeys.get(u).size();            
        }
        return number;
    }
    
    public Vector<ChargingStation> getListOfChargingStations(){
        return chargers;
    }
    
    public ChargingStation getCharger(int spot){
        return chargers.get(spot);
    }
    
    public synchronized int getTotalChargeBikes(){
        int sum = 0;
        for(Bike b: bikes){
            sum += b.getTotalChargeThisBike();
        }
        return sum;
    }
    
    public synchronized int getTotalChargeChargers(){
        int total = 0;
        for(ChargingStation c: chargers){
            total += c.getTotalChargeIssued();
        }
        return total;
    }
    
    public synchronized int getTotalChargeUsers(){
        int total = 0;
        for(User u: users){
            total += u.getTotalChargeUsed();
        }
        return total;
    }
    
    public synchronized int getTotalChargeJourneys(){
        int total = 0;
        for(User u: journeys.keySet()){
            for(Integer charge: journeys.get(u)){
                total += charge;
            }            
        }
        return total;
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
    
}
