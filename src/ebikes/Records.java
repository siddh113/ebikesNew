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
public class Records extends Thread{
    
    /* Collections Stored within the Records object */
    CopyOnWriteArrayList<Bike> bikes;
    CopyOnWriteArrayList<User> users;
    CopyOnWriteArrayList<ChargingStation> chargers;
    HashMap<User, CopyOnWriteArrayList<Integer>> journeys;
    
    /* Properties of the Records class */
    Journeys journeyPlanner;
    
    GUI gui;
    /* Using locks to make the methods thread safe */
    private static ReentrantLock lock;
    
    /* Initializing executor object */
    Executor executor = Executors.newCachedThreadPool();
    
    boolean running;
    
    /* Records constructor */
    public Records(GUI gui){
        this.gui = gui;
        journeys = new HashMap();
        chargers = new CopyOnWriteArrayList();
        bikes = new CopyOnWriteArrayList();
        users = new CopyOnWriteArrayList();
        journeyPlanner = new Journeys(this);
        lock = new ReentrantLock();
        
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
            journeys.put(users.get(i), new CopyOnWriteArrayList());
        }
        System.out.println("There are " + users.size() + " users");
        
    }
    
    /* Runnable for Records class */ 
    @Override public void run(){
        
        for(ChargingStation c: chargers) {
            executor.execute(c);
        }
        executor.execute(journeyPlanner);
        
        //------------ NOW GUI IS RESPONSIBLE FOR ITS OWN UPDATES ------------------------
        //journeyPlanner.start();
        //running = true;
//        while(running){
//            pause(500);
//            //gui.updateData(); 
//        }
    }
    
    /* Method to shutdown every thread */
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
    
    /* Method to add journeys made by the user */
    public void addJourney(User user, int chargeUsed, ChargingStation charger){
        try{
            lock.lock();
            journeys.get(user).add(chargeUsed);
            gui.addToLog("User " + user.getAccountNumber() + " travelled to " + charger.getStationName() + " using " + chargeUsed + " of charge" );
        } finally{
            lock.unlock();
        }

    }
    
    /* Method to get full report of the user and the bikes */
    public void getFullReport(){
        try{
          for(ChargingStation c: chargers){
            System.out.println(c);
            for(Bike b: c.getListOfReturnedBikesAtStation()) System.out.println(b);
            for(Bike b: c.getListOfChargingBikesAtStation()) System.out.println(b);
            for(Bike b: c.getListOfFullyChargedBikesAtStation()) System.out.println(b);
        }
        }finally{
            lock.unlock();
        }
    }
            
    public synchronized ChargingStation getRandomCharger(){
        int number = (int)(Math.random()*chargers.size());
        return chargers.get(number);
    }
    
    public synchronized User getRandomUser(){
        while(true){
            //pause(1);
            int number = (int)(Math.random()*users.size());           
            User user = users.get(number);           
            if(user.getCurrentBike() == null) return user;
        }        
    }
    
    /* Getters and Setters */
    
    public CopyOnWriteArrayList<Bike> getBikes(){
        return bikes;
    }
    
    public CopyOnWriteArrayList<User> getUsers(){
        return users;
    }
    
    public HashMap<User, CopyOnWriteArrayList<Integer>> getJourneys(){
        return journeys;
    }
    
    public int getNumberJourneys(){
        try{
            lock.lock();
            int number = 0;
            for(User u: journeys.keySet()){
                number += journeys.get(u).size();            
        }
        return number;
        }finally{
            lock.unlock();
        }
    }
    
    public CopyOnWriteArrayList<ChargingStation> getListOfChargingStations(){
        return chargers;
    }
    
    public ChargingStation getCharger(int spot){
        return chargers.get(spot);
    }
    
    /* Method to get total number of charged bikes */
    public int getTotalChargeBikes(){
        try{
            lock.lock();
            int sum = 0;
            for(Bike b: bikes){
                sum += b.getTotalChargeThisBike();
        }
        return sum;
        }finally{
            lock.unlock();
        }
    }
    
    /* Method to get total chargers required to charge the bikes */
    public int getTotalChargeChargers(){
        try{
            lock.lock();
            int total = 0;
            for(ChargingStation c: chargers){
                total += c.getTotalChargeIssued();
        }
        return total;
        }finally{
            lock.unlock();
        }
    }
    
    /* Method to get total number of users who used the chargers to charge thier bikes */
    public int getTotalChargeUsers(){
        try{
            lock.lock();
            int total = 0;
            for(User u: users){
                total += u.getTotalChargeUsed();
        }
        return total;
        }finally{
            lock.unlock();
        }
    }
    
    /* Method to get total charged used by the journeys that the user made */
    public int getTotalChargeJourneys(){
        try{
            lock.lock();
            int total = 0;
            for(User u: journeys.keySet()){
                for(Integer charge: journeys.get(u)){
                    total += charge;
            }       
        }
        return total;
        }finally{
            lock.unlock();
        }
    }
    
    public void pause(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {/* ignore */}
    }
    
}
