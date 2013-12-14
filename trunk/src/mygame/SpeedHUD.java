/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 *
 * @author scottdykstra
 */
public class SpeedHUD extends Node {
    private AssetManager assetManager;
    private AppSettings settings;
            
    private Picture speedMarker;
    private Picture speedBG;
    private Picture boostHUD;
    private int boostDuration;
    private boolean boostOn = false;
    private double boostStart;
    private float boostRotation;
    Node boostNode;
    Node speedMarkerNode;
    float speedMarkerRotation;
    boolean lastIdleDirection;
    float lastSpeedPercent;
    double lastBoostTime;
        //private Picture map;
    //private Picture balloonMarker;
    //private Picture playerMarker;
            
    private Node cupNode;
    private Picture[] cups;
    private int cupNum = 0;
    
    public SpeedHUD(int newBoostDuration, AssetManager assetM, AppSettings settingsIn) {
        assetManager = assetM;
        settings = settingsIn;
        
        boostDuration = newBoostDuration;
        lastIdleDirection = false;
        lastSpeedPercent = 0;
        lastBoostTime = 0;
        
        speedBG = new Picture("Speedometer", false);
        speedBG.setImage(assetManager, "Interface/speed2.png", true);
        speedBG.setWidth(173);
        speedBG.setHeight(173);
        speedBG.setPosition(0, 0);
        this.attachChild(speedBG);
        
        speedMarker = new Picture("Speed Marker",false);
        speedMarker.setImage(assetManager, "Interface/speedneedle1.png", true);
        speedMarker.setWidth(345);
        speedMarker.setHeight(32);
        speedMarker.setPosition(-167, -11);
        speedMarkerNode = new Node();
        speedMarkerNode.attachChild(speedMarker);
        this.attachChild(speedMarkerNode);
        
        boostHUD = new Picture("Boost", false);
        boostHUD.setImage(assetManager, "Interface/powerup-nocrop.png", true);
        boostHUD.setWidth(432);
        boostHUD.setHeight(432);
        boostHUD.setPosition(-216, -216); 
        boostNode = new Node();
        boostNode.attachChild(boostHUD);
        this.attachChild(boostNode);
        boostNode.rotate(0, 0, -1.7f);
        boostRotation = -1.7f;
          
        cupNode = new Node();
        this.attachChild(cupNode);
        cups = new Picture[3];
        
        for(int i=0; i<3; i++) {
            float widthPosition = (settings.getWidth()/2) + (i *60) - 85;
            cups[i] = new Picture("Rinnova",false);
            cups[i].setImage(assetManager, "Interface/rinnova2.png", true);
            cups[i].setWidth(60);
            cups[i].setHeight(86);
            cups[i].setPosition(widthPosition, 10f);   
        }
        
        //speedMarker.setLocalTranslation(-4.5f, -4.5f, 1);
        //posistions are with the lower left hand corner being (0,0)
        //this.setLocalTranslation(0, 0, 0);
        System.out.println("Initialized Speed HUD");
    }
    
    public void addCup() {
        cupNum++;
        if(cupNum <= 3) {
            System.out.println("Attaching cup #"+cupNum+" to HUD");
            cupNode.attachChild(cups[cupNum-1]);
        }
        
    }
    public void removeCup() {
        if(cupNum <= 3) {
            System.out.println("Removing cup #"+cupNum+" from HUD");
            cupNode.detachChild(cups[cupNum-1]);
        }
        cupNum--;
    }
    
    public void clearCups() {
        while (cupNum > 0) {
            removeCup();
        }
    }
    
    public void setSpeedMarker(double speedPercent){
        float speed = (float) speedPercent;
        setSpeedMarker(speed);
    }
    
    public void setSpeedMarker(float speedPercent) {
        speedPercent = Math.abs(speedPercent);
        if(boostOn) speedPercent *= 1.45f;
        if(speedPercent > 1.45f) speedPercent = 1.45f;
        if(speedPercent != lastSpeedPercent) {
            if(speedPercent > speedMarkerRotation) {
            //accelerating
            speedMarkerNode.rotate(0, 0, speedPercent - speedMarkerRotation);
            speedMarkerRotation = speedPercent;
            } else if(speedPercent < speedMarkerRotation) {
                //decelerating
                speedMarkerNode.rotate(0, 0, speedPercent - speedMarkerRotation);
                speedMarkerRotation = speedPercent;
            } 
            lastSpeedPercent = speedPercent;
        } else {
            if(lastIdleDirection) {
                speedMarkerNode.rotate(0, 0, -.02f);
                speedMarkerRotation -= .02f;
                lastIdleDirection = false;
            } else {
                speedMarkerNode.rotate(0, 0, .02f);
                speedMarkerRotation += .02f;
                lastIdleDirection = true;
            }
        }
    }
    
    public void startBoost() {
        boostOn = true;
        boostStart = System.nanoTime();
        if(boostRotation != -1.7f) boostNode.rotate(0, 0, -boostRotation - 1.7f);
        boostNode.rotate(0, 0, 1.7f);
        boostRotation = 0f;
        removeCup();
    }
    
    public void updateBoost() {
        float rotationsAllowed = boostDuration * 10f;
        int boostTime = (int)Math.floor((System.nanoTime()-boostStart)/Math.pow(10,8));
        if(boostTime != lastBoostTime) {
            boostRotation -= 1.7f / rotationsAllowed;
            boostNode.rotate(0, 0, -1.7f / rotationsAllowed);
            lastBoostTime = boostTime;
        }  
    }
    
    public void endBoost() {
        boostOn = false;
        if(boostRotation != -1.7f) boostNode.rotate(0, 0, -boostRotation - 1.7f);
        boostRotation += -boostRotation - 1.7f;
    }
    
    private static Picture[] push(Picture[] array, Picture push) {
        Picture[] longer = new Picture[array.length + 1];
        for (int i = 0; i < array.length; i++)
            longer[i] = array[i];
        longer[array.length] = push;
        return longer;
    }
}

