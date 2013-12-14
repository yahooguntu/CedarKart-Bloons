package mygame;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Menu;
import de.lessvoid.nifty.controls.MenuItemActivatedEvent;
import de.lessvoid.nifty.controls.RadioButton;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.slider.builder.SliderBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import org.bushe.swing.event.EventTopicSubscriber;
 
public class GUIController implements ScreenController {
 
    private Element popup;
    private Nifty nifty;
    private Screen screen;
    private Main app;
    private StartGUI gui;

    public GUIController(Main app, StartGUI gui){
        this.app = app;
        this.gui = gui;
    }
    
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;             
    }
 
    public String getScreenId(){
        return screen.getScreenId();
    }
    
    public void onStartScreen(){}

    public void onEndScreen(){}
    
    public void gotoScreen(String nextScreen){
           
        if(nextScreen.contains("settings")){
            nifty.gotoScreen(nextScreen);
        }
        else{
            nifty.gotoScreen(nextScreen);
            //nifty.exit();
        }
    }
    
    public void quitGame() {
        System.exit(0);
    }
    
    
}
