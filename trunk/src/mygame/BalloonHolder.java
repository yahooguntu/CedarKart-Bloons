/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;


/**
 *
 * @author tkirtland
 */
public interface BalloonHolder {
    public boolean hasBalloon();
    public void addBalloon();
    public void removeBalloon();
    public Vector3f getBalloonLocation();
    
    public String getUsername();
    public void setScore(int score);
    public int getScore();
}
