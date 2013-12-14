
package mygame;


import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import de.lessvoid.nifty.Nifty;

public class StartGUI {

    private Nifty nifty;
    private GUIController controller;   
    
    private AssetManager assetManager;
    private InputManager inputManager;
    private AudioRenderer audioRenderer;
    private ViewPort guiViewPort;
    private Node guiNode;
    private AppSettings settings;
    private Main app;
    private BitmapText gameTimes;
    private BitmapFont font;
    private Node endNode;
    private NiftyJmeDisplay niftyDisplay;
    
    public StartGUI(AssetManager assetManager, InputManager inputManager, 
            AudioRenderer audioRenderer, 
            ViewPort guiViewPort, Node guiNode, AppSettings settings,
            Main app){
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.audioRenderer = audioRenderer;
        this.guiViewPort = guiViewPort;
        this.guiNode = guiNode;
        this.settings = settings;
        this.app = app;
        
    }
    
    public void createGUI() {
        controller = new GUIController(app, this);
        
        niftyDisplay = new NiftyJmeDisplay(assetManager,
                                                          inputManager,
                                                          audioRenderer,
                                                          guiViewPort);

        // Endgame display
        endNode = new Node();
        int width = 389;
        int height = 132;
        Picture background = new Picture("Overlay");
        background.setWidth(width);
        background.setHeight(height);
        background.setPosition((settings.getWidth() - width)/2, (settings.getHeight() - height)/2);
        background.setImage(assetManager, "Interface/endgame.png", true);
        endNode.attachChild(background);

        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/niftyGUIInit.xml", "start", controller);
        //nifty.setDebugOptionPanelColors(true);
                
        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        
        //make cursor visible (necessary to use gui controls)
        inputManager.setCursorVisible(true);

    }

    public void removeGUI() {
        guiViewPort.removeProcessor(niftyDisplay);
    }
    
}
