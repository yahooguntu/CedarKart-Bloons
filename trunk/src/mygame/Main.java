package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import de.lessvoid.nifty.Nifty;
import com.jme3.niftygui.NiftyJmeDisplay;
import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.audio.AudioNode;
import com.jme3.ui.Picture;
import com.jme3.math.Quaternion;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Main extends SimpleApplication implements ActionListener {

    private boolean gameLoaded = false;
    private boolean loadingScreenGone = false;
    private SimpleController startScreen;
    private Picture startScreenBG;
    private double loadingStart = 0;
    private boolean screenLoading = false;
    private static BulletAppState bulletAppState;
    private AnimChannel channel;
    private AnimControl control;
    private WaterFilter water;
    private Vector3f waterLight = new Vector3f(-4f, -1f, 5f);
    private boolean boostOn = false;
    private double boostStartTime = 0;
    private int boostDuration = 5; //# seconds for boost
    private Spatial balloon;
    private ArrayList<Node> cups;
    private ConcurrentLinkedQueue<Node> waitingCups;
    SpeedHUD speedometer;
    MinimapHUD minimap;
    ScoreHUD topBar;
    private PlayerManager player;
    private NetworkManager networkManager;
    private AudioNode soundtrack, engineSound, tireSql;
    private AudioNode boostSound, powerupSound, balloonPickup, startSound;
	private String username;
	private String serverAddr;
        private boolean canCollide;
    Timer timer;
    
    
    // Only used for testing
    private int addedCups = 0;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    public Vector3f getBalloonLocation() {
        if (balloon != null) {
            return balloon.getLocalTranslation();
        }
        
        else {
            return null;
        }
    }
    
    private void initPPcWater() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        water = new WaterFilter(rootNode, waterLight);

        water.setCenter(new Vector3f(12f, 9f, 180f));
        water.setRadius(121);
        water.setWaterHeight(5.5f);

        water.setWaveScale(0.008f);
        water.setMaxAmplitude(0.2f);
        water.setUseFoam(false);
        water.setUseRipples(true);

        water.setDeepWaterColor(ColorRGBA.Brown);
        water.setWaterColor(ColorRGBA.Brown.mult(2.0f));
        water.setWaterTransparency(0.5f);
        water.setRefractionStrength(0.2f);

        fpp.addFilter(water);
        viewPort.addProcessor(fpp);
    }
    
    private void initSound(){
        soundtrack = new AudioNode(assetManager, "Sounds/kartMusic.wav", false);
        soundtrack.setPositional(false);
        soundtrack.setLooping(true);
        soundtrack.setVolume(.1f);
        rootNode.attachChild(soundtrack);
        soundtrack.play();
        
        boostSound = new AudioNode(assetManager, "Sounds/boost.wav", false);
        boostSound.setPositional(true);
        rootNode.attachChild(boostSound);
        
        powerupSound = new AudioNode(assetManager, "Sounds/powerUp1.wav", false);
        boostSound.setPositional(true);
        rootNode.attachChild(powerupSound);
        
        engineSound = new AudioNode(assetManager, "Sounds/engine.wav", false);
        engineSound.setPositional(true);
        engineSound.setLooping(true);
        engineSound.setVolume(5);
        rootNode.attachChild(engineSound);
        
        tireSql = new AudioNode(assetManager, "Sounds/TireSql.wav", false);
        tireSql.setPositional(true);
        tireSql.setLooping(true);
        tireSql.setVolume(.3f);
        rootNode.attachChild(tireSql);
        
        balloonPickup = new AudioNode(assetManager, "Sounds/balloon.wav", false);
        balloonPickup.setPositional(true);
        rootNode.attachChild(balloonPickup);
        
        startSound = new AudioNode(assetManager, "Sounds/start.wav", false);
        startSound.setPositional(false);
        rootNode.attachChild(startSound);
        
    }

    private void initSkyBox() {
        Texture westTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0004-w.jpeg");
        Texture eastTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0001-e.jpeg");
        Texture northTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0005-n.jpeg");
        Texture southTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0002-s.jpeg");
        Texture upTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0003-u.jpeg");
        Texture downTex = assetManager.loadTexture("Textures/Skyboxes/skyboxsun25degtest/skyrender0006-d.jpeg");
        final Vector3f normalScale = new Vector3f(1, 1, 1);
        rootNode.attachChild(SkyFactory.createSky(assetManager, westTex, eastTex,
                northTex, southTex, upTex, downTex, normalScale));
    }

    private void initKeys() {
        inputManager.addMapping("Location", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("Powerup", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Start", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addListener(this, "Location");
        inputManager.addListener(this, "Powerup");
        inputManager.addListener(this, "Start");
    }
    
    @Override
    public void simpleInitApp() {
        setDisplayFps(false); // to hide the FPS
        setDisplayStatView(false); // to hide the statistics

        startScreen = new SimpleController();
        stateManager.attach(startScreen);
        
        assetManager.registerLoader(TextLoader.class, "txt");

        cups = new ArrayList<Node>();

        /**
         * Activate the Nifty-JME integration:
         */
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/niftyGUIInit.xml", "start", startScreen);
        
        flyCam.setDragToRotate(true);

        waitingCups = new ConcurrentLinkedQueue<Node>();
        
        initSound();
        
        startScreenBG = new Picture("Start BG",false);
        startScreenBG.setImage(assetManager, "Interface/splash-1040.png", true);
        startScreenBG.setWidth(settings.getWidth());
        startScreenBG.setHeight(settings.getHeight());
        startScreenBG.setPosition(0, 0);
        rootNode.attachChild(startScreenBG);
        
        canCollide = true;
    }

    public void initGameplay() {
		System.out.println(username);
		System.out.println(serverAddr);
		
        gameLoaded = true;
        loadingScreenGone = true;        

        initHUD();
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        PhysicsManager physics = new PhysicsManager(rootNode, assetManager, inputManager,
                cam, flyCam, stateManager, bulletAppState);
        player = new PlayerManager(this, assetManager, inputManager,
                cam, flyCam, physics, username);
        player.initCharacter();
        player.setupChaseCamera();
        player.setupKeys();
                
        WorldMaker worldMaker = new WorldMaker();
        Spatial world = worldMaker.generateWorld(this, bulletAppState);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
        rootNode.addLight(dl);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White);
        rootNode.addLight(al);

        rootNode.attachChild(world);

        addCups();

        initKeys();
        initPPcWater();
        initSkyBox();
        
        networkManager = new NetworkManager(this, serverAddr, assetManager, bulletAppState, player, false);

        // addBalloon();
        
        Random r = new Random();
        player.physicsManager().getVehiclePhysics().setPhysicsLocation(new Vector3f(76.37f + (r.nextInt(10) - 5), 11.46f, -103.0f));
        player.physicsManager().getVehiclePhysics().setPhysicsRotation(new Quaternion(0.0f, 0.123f, 0, 0.992f));
        
        System.out.println("Gameplay initialized");
    }
    
    public void initHUD() {
        topBar = new ScoreHUD(this, assetManager, settings, guiFont);
        guiNode.attachChild(topBar);
        speedometer = new SpeedHUD(boostDuration, assetManager, settings);
        guiNode.attachChild(speedometer);
        minimap = new MinimapHUD(this, assetManager, settings);
        guiNode.attachChild(minimap);
        engineSound.play();
    }

    public void addBalloon(Vector3f location) {
        balloon = assetManager.loadModel("Models/balloon.mesh.xml");
        balloon.setLocalTranslation(location);
        balloon.setLocalScale(1f);

        balloon.setModelBound(new BoundingSphere());
        balloon.updateModelBound();

        rootNode.attachChild(balloon);
    }
	
    public void removeBalloonFromMap()
    {
        if (balloon != null)
        {
                balloon.removeFromParent();
                balloon = null;
        }
    }

    public void addCups() {
        try {
            Scanner scanner = new Scanner((String) assetManager.loadAsset("Models/cup_placement.txt"));
            scanner.useDelimiter("\n");

            while (scanner.hasNext()) {
                try {
                    String[] line = scanner.nextLine().split(",");
                    String name = line[0];
                    float x = Float.parseFloat(line[1]);
                    float y = Float.parseFloat(line[2]);
                    float z = Float.parseFloat(line[3]);

                    Node cup = (Node) getAssetManager().loadModel("Models/cup.mesh.xml");
                    cup.setName(name);
                    cup.setLocalScale(0.25f);
                    cup.setLocalTranslation(x, y, z);

                    cup.setModelBound(new BoundingSphere());
                    cup.updateModelBound();

                    control = cup.getControl(AnimControl.class);
                    channel = control.createChannel();
                    channel.setLoopMode(LoopMode.Loop);

                    channel.setAnim("spin");

                    rootNode.attachChild(cup);

                    cups.add(cup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Runnable cupUpdater = new Runnable() {
        public void run() {
            try {
                Thread.sleep(20000);

                Node cup = waitingCups.poll();

                cups.add(cup);
                addCup(cup);

            } catch (InterruptedException ex) {
                Logger.getLogger(WorldMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    private void addCup(final Node cup) {
        this.enqueue(new Callable<Node>() {
            public Node call() throws Exception {
                rootNode.attachChild(cup);
                return cup;
            }
        });
    }
    
    public void remoteCupTaken(int cupID) {
        Node cup = cups.get(cupID);
        waitingCups.add(cup);
        
        Thread t = new Thread(cupUpdater);
        t.start();

        cup.removeFromParent();
    }
    
    public Runnable collideUpdater = new Runnable() {
        public void run() {
            try {
                canCollide = false;
                Thread.sleep(2000);
                canCollide = true;
                
            } catch (InterruptedException ex) {
                Logger.getLogger(WorldMaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    
    @Override
    public void simpleUpdate(float tpf) {
        if (gameLoaded) {
            if (loadingScreenGone) {
                startScreen.goToScreen("playGame");
                loadingScreenGone = false;
            }
            
            //get rid of start logo
            if(timer != null) 
                if(timer.seconds == 2) topBar.removeStart();
            
            player.update();
            
            //update HUDS
            topBar.update();
            speedometer.setSpeedMarker(player.getSpeed() / player.getMaxSpeed());
            minimap.update();
            
            //update engine speed sound
            updateEngineSound();
            updateTireSqueal();
            
            if (player.boostOn()) {
                speedometer.updateBoost();
                double currTime = System.nanoTime() / Math.pow(10, 9);
                if (currTime - player.boostStartTime() >= boostDuration - 1 && currTime - player.boostStartTime() < boostDuration) {
                    player.toggleBoost(-1, speedometer);
                }
                if (currTime - player.boostStartTime() >= boostDuration) {
                    player.toggleBoost(0, speedometer);
                }
            }
            
            for (int i = 0; i < cups.size(); i++) {
                Node cup = cups.get(i);
                if (cup != null && !waitingCups.contains(cup)) {
                    CollisionResults results = new CollisionResults();
                    player.vehicleNode().collideWith(cup.getWorldBound(), results);
                    if (results.size() > 0) {    
                        powerupSound.playInstance();
                        waitingCups.add(cup);
                        networkManager.cupTaken(i);

                        Thread t = new Thread(cupUpdater);
						t.setDaemon(true);
                        t.start();

                        cup.removeFromParent();
                        player.addBoost(speedometer);
                        break;
                    }
                }
            }

            if (player != null) {
                if (!player.hasBalloon() && canCollide) {
                    Node bp = networkManager.getPlayerWithBalloon();
                    if (bp != null) {
                        CollisionResults r = new CollisionResults();
                        player.vehicleNode().collideWith(bp.getWorldBound(), r);
                        if (r.size() > 15) {
                            System.out.println("Vehicle collision");
                            player.addBalloon();

                            networkManager.claimBalloon();
                            Thread t = new Thread(collideUpdater);
							t.setDaemon(true);
                            t.start();
                        }
                    }

                    else if (balloon != null) {            
                        CollisionResults r = new CollisionResults();
                        player.vehicleNode().collideWith(balloon.getWorldBound(), r);
                        if (r.size() > 10) {
                            System.out.println("Hit balloon");
                            player.addBalloon();
							
                            removeBalloonFromMap();

                            networkManager.claimBalloon();
                        }
                    }
                    playBalloonSound();
                }
            }
        }
        if (screenLoading) {
            if ((System.nanoTime() - loadingStart) / Math.pow(10, 7) > 50) {
                screenLoading = false;
                initGameplay();
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Location")) {
            if (!isPressed) {
                String loc = player.physicsManager().getVehicleNode().getLocalTranslation().toString();
                loc = loc.substring(1, loc.length() - 1);
                
                System.out.println(loc);
                System.out.println(player.physicsManager().getVehicleNode().getLocalRotation());
            }
        } else if (binding.equals("Powerup")) {
            if (!isPressed && player.numBoosts() > 0) {
                player.toggleBoost(1, speedometer);
                player.removeBoost();
                boostSound.play();

            }
        } else if (binding.equals("Start")) {
            if (!isPressed) {
                networkManager.startGame();
                playStartSound();
            }
        }
    }
    
    public void updateEngineSound() {
        float speedSound = player.getSpeed()/10;
        if(speedSound < .5){
            engineSound.setPitch(.5f);
        }
        else if(speedSound > 2.0){
            engineSound.setPitch(2f);
        }
        else{
            engineSound.setPitch(speedSound);
        }
    }
    
    public void updateTireSqueal(){
        if(player.getTurn() == true && player.getSpeed()/10 > 2f){
            tireSql.play();
        }
        else{
            tireSql.pause();
        }

    }
    
    public void playBalloonSound(){
        if(player.hasBalloon()){
            balloonPickup.play();
        }
    }
    
    public void playStartSound(){
        startSound.play();
    }

    public void setScreenLoading(boolean option) {
        screenLoading = option;
        if (screenLoading) {
            loadingStart = System.nanoTime();
        }
    }
    
    public void setTextToScreen(String s) {
        BitmapText hudText = new BitmapText(guiFont, false);          
        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudText.setColor(ColorRGBA.Blue);                             // font color
        hudText.setText(s);             // the text
        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0); // position
        guiNode.attachChild(hudText);
    }
	
    public void setUsername(String username)
    {
            this.username = username;
    }

    public void setServerAddress(String serverAddress)
    {
            serverAddr = serverAddress;
    }
    
    public PlayerManager getPlayer() {
        return player;
    }
    
    public Picture getStartScreenBG() {
        return startScreenBG;
    }
    
    public Spatial getBalloon() {
        return balloon;
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
	
	// called on close
	@Override
	public void destroy()
	{
		networkManager.shutdown();
		System.out.println("We're exiting!");
		super.destroy();
	}
	
	public void dieToStartScreen(String msg)
	{
		//TODO
		// this is so the network thread can kill the game and kick the
		// player back to the start screen when the connection is lost,
		// the connection fails, or the server admin kicks the player.
		// it'd be nice if the message (msg) could be displayed in a dialog too.
	}
}
