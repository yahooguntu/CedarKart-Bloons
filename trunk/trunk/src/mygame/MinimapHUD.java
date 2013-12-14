/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 *
 * @author scottdykstra
 */
public class MinimapHUD extends Node {
   AppSettings settings;
    Main app;
    private Picture minimapBG;
    private Picture marker;
    private Node markerNode;
    float width;
    float height;
    float iconSize;
    
    private Picture balloon;
    private Node balloonNode;
    
    private Node opponentsNode;
    private PlayerInfo [] opponents;
    
    public MinimapHUD(Main appIn, AssetManager assetManager, AppSettings settingsIn) {
        app = appIn;
        settings = settingsIn;
        
        width = 200f;
        float ratio = 596f/777f;
        height = width/ratio;
        iconSize = 25;
        
        minimapBG = new Picture("Speed Marker",false);
        minimapBG.setImage(assetManager, "Interface/world3.png", true);
        minimapBG.setWidth(width);
        minimapBG.setHeight(height);
        minimapBG.setPosition(settings.getWidth()-width, 0);
        this.attachChild(minimapBG);
        
        opponentsNode = new Node();
        this.attachChild(opponentsNode);
        
        marker = new Picture("cart",false);
        marker.setImage(assetManager, "Interface/carticon1.png", true);
        marker.setWidth(iconSize);
        marker.setHeight(iconSize);
        marker.setPosition(settings.getWidth(),0); 
        markerNode = new Node();
        markerNode.attachChild(marker);
        this.attachChild(markerNode);
        
        balloon = new Picture("Balloon",false);
        balloon.setImage(assetManager, "Interface/balloon1.png", true);
        balloon.setWidth(.1f*113f);
        balloon.setHeight(.1f*150f);
        balloon.setPosition(settings.getWidth(),0); 
        balloonNode = new Node();
        balloonNode.attachChild(balloon);
        this.attachChild(balloonNode);
    }  
    
    public void update() {
        updatePlayerLocation();
        updateBalloonLocation();
    }
    
    public void updatePlayerLocation() {
        Vector3f loc = app.getPlayer().vehicleNode().getLocalTranslation();
        //1.06 is the ratio +-.02 starting at 0,70
        float xRatio = width/611;
        //top is -167z, bottom is 376z
        //left is -346x, right side is 265x, 
        float xZeroed = (loc.x+347f)*xRatio; //left is 0x, right is 462x after this line
        float screenX = settings.getWidth()-width+xZeroed-20f;
        float zZeroed = (loc.z*-1f)+ 410f+(78f); //bottom is 0x, top is 690z after this line
        float screenZ = zZeroed * xRatio;
        //System.out.println("x:"+screenX+" -- z: "+screenZ);
        // System.out.println("x:"+screenX+" -- z: "+screenZ);
        marker.setPosition(screenX,screenZ); 
    }
    
    public void updateBalloonLocation() {
        if(app.getBalloon() != null) {
            Vector3f loc = app.getBalloon().getLocalTranslation();
            float xRatio = width/611;
            float xZeroed = (loc.x+347f)*xRatio; 
            float screenX = settings.getWidth()-width+xZeroed-20f;
            float zZeroed = (loc.z*-1f)+ 410f+(78f);
            float screenZ = zZeroed * xRatio;
            //System.out.println("x:"+screenX+" -- z: "+screenZ);
            balloon.setPosition(screenX+5f,screenZ+5f); 
        } else {
            if(app.getNetworkManager().getPlayerWithBalloon() != null) {
                Vector3f loc = ((BalloonHolder) app.getNetworkManager().getPlayerWithBalloon()).getBalloonLocation();
                float xRatio = width/611;
                float xZeroed = (loc.x+347f)*xRatio; 
                float screenX = settings.getWidth()-width+xZeroed-20f;
                float zZeroed = (loc.z*-1f)+ 410f+(78f);
                float screenZ = zZeroed * xRatio;
                balloon.setPosition(screenX+5f,screenZ+5f); 
            }   
        }
    }
}
