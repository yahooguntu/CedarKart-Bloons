/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 */
public class SimpleController extends AbstractAppState implements ScreenController {

  private Nifty nifty;
  private Main app;
  private Screen screen;

  /** custom methods */
  public SimpleController() {
    /** You custom constructor, can accept arguments */
  }

  public void goToScreen(String nextScreen) {
      if(nextScreen.contains("loading")) {
          app.getRootNode().detachChild(app.getStartScreenBG());
          TextField uname = screen.findNiftyControl("username", TextField.class); 
          TextField sIP = screen.findNiftyControl("ip", TextField.class);  
          String username = uname.getText(); //username textfield
          System.out.println(username);
          String serverIP = sIP.getText(); //server IP address as string
		  app.setUsername(username);
		  app.setServerAddress(serverIP);
          //app.setTextToScreen("Username: "+username+" --- Server IP: "+serverIP);
          app.setScreenLoading(true);
      }
      nifty.gotoScreen(nextScreen);  // switch to another 
  }


  public void quitGame() {
    app.stop();
  }

  /** Nifty GUI ScreenControl methods */
  public void bind(Nifty nifty, Screen screen) {
    this.nifty = nifty;
    this.screen = screen;
  }

  public void onStartScreen() {
  }

  public void onEndScreen() {

  }
  
  
  

  /** jME3 AppState methods */
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    this.app = (Main)app;
  }


}