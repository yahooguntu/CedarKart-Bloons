/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.ui.Picture;

/**
 *
 * @author scottdykstra
 */
public class PlayerInfo {
    public String username;
    public int id;
    Picture marker;
    
    public PlayerInfo(String uname, int newid, Picture pic){
        username = uname;
        id = newid;
        marker = pic;
    }
}
