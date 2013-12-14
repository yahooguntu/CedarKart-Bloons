/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 *
 * @author scottdykstra
 */
public class ScoreHUD extends Node {
   AppSettings settings;
    Main app;
    private Picture topBG;
    private Picture arrow;
    //private float arrowRot;
    //private Node arrowNode;
    private BitmapFont guiFont;
    BitmapText []topScores;
    BitmapText myScore;
    private Picture balloon;
    private boolean scoring;
    private Picture start;
    private int myScoreNum;
    BitmapText winner;
    
    ScoreHUD(Main appIn, AssetManager assetManager, AppSettings settingsIn, BitmapFont gFont) {
        app = appIn;
        settings = settingsIn;
        guiFont = gFont;
        
        float widthRatio = settings.getWidth()/1024f;
        float heightRatio = settings.getHeight()/768f;
        
        myScoreNum = 0;
        scoring =false;
        
        start = new Picture("start",false);
        start.setImage(assetManager, "Interface/start1.png", true);
        float startW = 378f*1.3f*(widthRatio);
        float startH = 92f*1.3f*(widthRatio);
        start.setWidth(startW);
        start.setHeight(startH);
        start.setPosition(settings.getWidth()/2-startW/2, settings.getHeight()/2-startH);
        
        
        float heightAdjust = 110f*widthRatio;
        topBG = new Picture("Top",false);
        topBG.setImage(assetManager, "Interface/topbar2.png", true);
        topBG.setWidth(settings.getWidth());
        topBG.setHeight(heightAdjust);
        topBG.setPosition(0, settings.getHeight()-heightAdjust);
        this.attachChild(topBG);
        
        balloon = new Picture("arrow",false);
        balloon.setImage(assetManager, "Interface/balloon2.png", true);
        balloon.setWidth(33f*(settings.getWidth()/1024f));
        balloon.setHeight(29f*(settings.getWidth()/1024f));
        balloon.setPosition(settings.getWidth()-33f*widthRatio-3f, settings.getHeight()-29f*heightRatio-3f);
        //this.attachChild(balloon);
        
        topScores = new BitmapText[3];
        float xLoc;
        for(int i=0; i<3; i++) {
            topScores[i] = new BitmapText(guiFont, false);       
            topScores[i].setSize((guiFont.getCharSet().getRenderedSize()-4f)*widthRatio);   
            topScores[i].setColor(ColorRGBA.Black);                           
            topScores[i].setText("No other players");  
            if(i == 0) xLoc = 36f;
            else xLoc = 36f + (205f*((float)i));
            topScores[i].setLocalTranslation(xLoc*widthRatio, settings.getHeight()-((topScores[i].getLineHeight()-2f)*heightRatio), 0);
            this.attachChild(topScores[i]);
        }
        myScore = new BitmapText(guiFont, false);       
        myScore.setSize(guiFont.getCharSet().getRenderedSize()*4f);   
        myScore.setColor(ColorRGBA.Cyan);                           
        myScore.setText(Integer.toString(myScoreNum));  
        myScore.setLocalTranslation(settings.getWidth()-(105f*widthRatio), settings.getHeight()-(1f*heightRatio), 0);
        this.attachChild(myScore);
        
        
        
        /*arrow = new Picture("arrow",false);
        arrow.setImage(assetManager, "Interface/arrow1.png", true);
        arrow.setWidth(96f*(settings.getWidth()/1024f)/1.5f);
        arrow.setHeight(22f*(settings.getWidth()/1024f)/1.5f);
        arrow.setPosition(settings.getWidth()/2f+5f, settings.getHeight()-(heightAdjust/2f)+16f);
        arrowNode = new Node();
        arrowNode.attachChild(arrow);
        this.attachChild(arrowNode);
        arrowRot = .5f;*/
        //arrow.rotate(0,0,.5f);
        //arrowRot = 1f;
    }  
    public void update() {
        for(int i=0; i<3; i++) {
            if(app.getNetworkManager().getTopScores()[i] != null) {
                String thisUser = app.getNetworkManager().getTopScores()[i].getUsername();
                int thisScore = app.getNetworkManager().getTopScores()[i].getScore();
                topScores[i].setText(thisUser+ " ("+thisScore+")");
                if(thisScore == 50) {
                    endGame(thisUser);
                }
            }
        }
    }
    
    public void endGame(String user) {
        myScoreNum = 0;
        app.getNetworkManager().updateScore(0);
        if(winner != null) winner.setText(user+ " has won! "+user+" can press 'b' to restart.");  
        else {
            myScore.setColor(ColorRGBA.Red);    
            winner = new BitmapText(guiFont, false);
            winner.setSize(guiFont.getCharSet().getRenderedSize()*3f);   
            winner.setColor(ColorRGBA.Red);                           
            winner.setText(user+ " has won! "+user+" can press 'b' to restart.");  
            System.out.println(user+ " has won! "+user+" can press 'b' to restart.");  
            winner.setLocalTranslation(settings.getWidth()/2-500f, settings.getHeight()/2, 0);
            this.attachChild(winner);
        }
    }
    
    public void addBalloon() {
        scoring = true;
        this.attachChild(balloon);
    }
    
    public void removeBalloon() {
        scoring = false;
        this.detachChild(balloon);
    }
    
    public boolean isScoring() {
        return scoring;
    }
    
    public void addPoint() {
        myScoreNum++;
        myScore.setText(Integer.toString(myScoreNum));
        app.getNetworkManager().updateScore(myScoreNum);
    }
    
    public void removeStart() {
        this.detachChild(start);
    }
    
    public void newGame() {
        myScore.setColor(ColorRGBA.Cyan); 
        if(winner != null) winner.setText("");    
        myScoreNum = 0;
        myScore.setText("0");  
        app.getNetworkManager().updateScore(0);
        this.attachChild(start);
        System.out.println("New game started!");
        for(int i=0; i<3; i++){
            if(app.getNetworkManager().getTopScores()[i] != null) {
                String thisUser = app.getNetworkManager().getTopScores()[i].getUsername();
                topScores[i].setText(thisUser+ " (0)");
            }
        }
    }
    
    /*(public void updateArrow() {
         if(app.getBalloon() == null) spinArrow();
         else {
            Vector3f bLoc = app.getBalloon().getLocalTranslation();
            Vector3f pLoc = app.getPlayer().getLocalTranslation();
            Quaternion rotation = app.getPlayer().vehicleNode().getLocalRotation();
            float r = rotation.getY()*rotation.getW();
            float cam = app.getCamera().getDirection().x;
            float w = rotation.getW();
            double angle = Math.atan2((double)pLoc.z-(double)bLoc.z,(double)pLoc.x-(double)bLoc.x);
            double dist = Math.sqrt(Math.pow(pLoc.x-bLoc.x,2)+Math.pow(pLoc.y-bLoc.y,2));
            if(dist < 70) spinArrow();
            else {
                //starts at 1
                float target = -r;
                if(target != arrowRot){
                    //arrow.rotate(0f,arrowRot-target,0f);
                    //arrow.rotate(0f,.25f,0f);
                    if(r == 0) r = -1f;
                    else if(r == 1) r = 0f;
                    arrow.setLocalRotation(new Quaternion(0f,0f,-r,1f));
                    arrowRot = target;
                }
            } 
            System.out.println(r + " - rw - "+ w + " - cam: "+ cam + " - angle--> " + angle + " - distance - " + dist);
         }
         
    }
    
    public void spinArrow() {
        arrow.rotate(0,0,.15f);
    }*/
    
}