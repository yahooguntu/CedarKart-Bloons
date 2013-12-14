package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.input.InputManager;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.renderer.Camera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.concurrent.Callable;

public class PlayerManager extends Node implements ActionListener, BalloonHolder {

    // The game controllers
    private AssetManager assetManager;
    private InputManager inputManager;
    private Camera camera;
    private FlyByCamera flyCam;
    private PhysicsManager physicsMan;
    private Spatial player;
    public ChaseCamera chaseCam;
    private float steeringisPressed = 0;
    private float accelerationisPressed = 0;
    private float accelerationForce = 1000.0f;
    private boolean controllingCar = true;
    private int boosts = 0;
    private boolean hasBalloon = false;
    private Spatial balloon;
    private float maxSpeed = 30;
    private float startingAcceleration = 0.005f;
    private float startingMaxSpeed = 0.2f;
    private float acceleration;
    private float boostRatio = 1f;
    private boolean boostOn = false;
    private double boostStartTime = 0;
    private String username;
    private boolean Turn = false;
    private int score;
    Main app;
    
    private boolean forward, backward;
    

    public PlayerManager(Main mainIN, AssetManager assetManager, InputManager inputManager,
            Camera camera, FlyByCamera flyCam, PhysicsManager physicsMan, String username) { 
        
        app = mainIN;
        
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.camera = camera;
        this.flyCam = flyCam;
        this.physicsMan = physicsMan;
        acceleration = startingAcceleration;
        maxSpeed = startingMaxSpeed;
        this.username = username;
        
        setName(username);
		
		initBalloon();
    }
    
    public PlayerManager(AssetManager assetManager, PhysicsManager physicsMan){
		this.assetManager = assetManager;
        this.physicsMan = physicsMan;
		
		player = assetManager.loadModel("Models/vehicles/golfCart/golfCart.mesh.xml");

        //player.setLocalRotation(rotation);
        player.scale(0.1f);

        player.setModelBound(new BoundingSphere());
        player.updateModelBound();
        
        //player.move(new Vector3f(-59.20f, 7.84f, 271.17f));
        //player.rotate(-696.03f, 0.0f, -455.76f);
		
		initBalloon();
    }
	
	private void initBalloon()
	{
		balloon = assetManager.loadModel("Models/balloon.mesh.xml");
		balloon.setName("balloon");
		balloon.setLocalTranslation(0f, 2.8f, 0f);
		balloon.setLocalScale(1f);
	}
    
    public PhysicsManager physicsManager() {
        return physicsMan;
    }

    /**
     * Simple create Character
     */
    public void initCharacter() {
        player = assetManager.loadModel("Models/vehicles/golfCart/golfCart.mesh.xml");

        //player.setLocalRotation(rotation);
        player.scale(0.1f);

        player.setModelBound(new BoundingSphere());
        player.updateModelBound();
        
        physicsMan.buildVehicle(player);
    }
    
    public Node vehicleNode() {
        return physicsMan.getVehicleNode();
    }

    public void setupChaseCamera() {
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(30f);
        flyCam.setDragToRotate(true);

        chaseCam = new ChaseCamera(camera, player, inputManager);

        chaseCam.setDefaultDistance(3f);
        chaseCam.setMaxDistance(3.2f);
        chaseCam.setDefaultVerticalRotation(0.2f);
        chaseCam.setTrailingSensitivity(1f);
        chaseCam.setLookAtOffset(new Vector3f(0, 1, 0));
        chaseCam.setSmoothMotion(true);
        chaseCam.setTrailingEnabled(true);
    }

    public void setupKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("Detach", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Reset");
        inputManager.addListener(this, "Detach");
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Left") && controllingCar) {
            if (isPressed) {
                steeringisPressed += .2f;
                Turn = true;
            } else {
                steeringisPressed += -.2f;
                Turn = false;
            }

            physicsMan.vehicle.steer(steeringisPressed);
        } else if (name.equals("Right") && controllingCar) {
            if (isPressed) {
                steeringisPressed += -.2f;
                Turn = true;
            } else {
                steeringisPressed += .2f;
                Turn = false;
            }
            physicsMan.vehicle.steer(steeringisPressed);

        } else if (name.equals("Up") && controllingCar) {
            if (isPressed) {
                accelerationisPressed += accelerationForce;
                forward = true;
            } 
            
            else {
                accelerationisPressed -= accelerationForce;
                forward = false;
            }
            
            physicsMan.accelerateVehicle(accelerationisPressed);

        } else if (name.equals("Down") && controllingCar) {
            if (isPressed) {
                accelerationisPressed -= accelerationForce;
                backward = true;
            } 
            
            else {
                accelerationisPressed += accelerationForce;
                backward = false;
            }
            
            // physicsMan.accelerateVehicle(accelerationisPressed);

        } else if (name.equals("Reset") && controllingCar) {
            if (isPressed) {
                physicsMan.resetVehicle();
            } else {
            }
        } else if (name.equals("Detach")) {
            if (isPressed) {
                controllingCar = !controllingCar;
                flyCam.setEnabled(!flyCam.isEnabled());
                chaseCam.setEnabled(!chaseCam.isEnabled());
            } else {
            }
        }
    }

    public float getMaxSpeed() {
        return 28f;
    }

    public float getSpeed() {
        return physicsMan.getVehiclePhysics().getLinearVelocity().length();
    }
    
    public boolean boostOn() {
        return boostOn;
    }
    
    public void setBoost(boolean b) {
        boostOn = b;
    }
    
    public double boostStartTime() {
        return boostStartTime;
    }
    
    public boolean getTurn(){
        return Turn; 
            }
    
    public void toggleBoost(int option, SpeedHUD speedometer) {
        if (option == 0) {
            //end speedometer boost
            boostOn = false;
            physicsMan.setMaxSpeed(physicsMan.getMaxSpeed() - 15f);
            speedometer.endBoost();
        } else if (option == -1) {
            //end vehicle boost
            endBoost();
        } else {
            //start boost
            startBoost();
            boostStartTime = System.nanoTime() / Math.pow(10, 9);
            boostOn = true;
            physicsMan.setMaxSpeed(physicsMan.getMaxSpeed() + 15f);
            speedometer.startBoost();
        }

        // System.out.println("Boost: " + boostOn);
        // System.out.println("Vehicle speed: " + vehicle.getSpeed());
    }

    public void addBoost(SpeedHUD speedometer) {
        boosts++;
        speedometer.addCup();
    }
    
    public void removeBoost() {
        if (boosts > 0) {
            boosts--;
        }
    }

    public int numBoosts() {
        return boosts;
    }
    
    public void startBoost() {
	boostRatio = 2f;
    }

    public void endBoost() {
	boostRatio = 1f;
    }
    
    public String getUsername() {
        return username;
    }

    public float getVelocity() {
        return physicsMan.vehicle.getLinearVelocity().length();
    }
    
    public void setVelocity(float v)
    {
        physicsMan.vehicle.setLinearVelocity(physicsMan.vehicle.getLinearVelocity().normalize().mult(v));
    }
	
    public Vector3f getTranslation()
    {
            return player.getWorldTranslation();
    }

    public Quaternion getRotation()
    {
            return player.getWorldRotation();
    }

    public void resetVehicle() {
        physicsMan.resetVehicle();
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float a) {
        acceleration = a;
    }

    public void update() {
        if (forward) {    
	    physicsMan.accelerateVehicle(boostRatio * accelerationForce);
        }
        
        else if (backward) {
            physicsMan.accelerateVehicle(-accelerationForce);
        }
        
        else {
            physicsMan.updateVehicle();
        }
    }

    public boolean hasBalloon() {
        return hasBalloon;
    }
    
    public void addBalloon() {
        if (!hasBalloon) {
            System.out.println("Added balloon.");
            hasBalloon = true;
            physicsMan.getVehicleNode().attachChild(balloon);
            app.topBar.addBalloon();
            
            physicsMan.setMaxSpeed(physicsMan.getMaxSpeed() / 3);
        }
    }
	
    public void removeBalloon() {
        if (hasBalloon) {
            System.out.println("Removed balloon.");
            hasBalloon = false;
            balloon.removeFromParent();
            app.topBar.removeBalloon();
            
            physicsMan.setMaxSpeed(physicsMan.getMaxSpeed() * 3);
        }
    }
    
    public Vector3f getBalloonLocation() {
        return vehicleNode().getLocalTranslation();
    }
    
    public void setScore(int score)
    {
        this.score = score;
    }
    
    public int getScore()
    {
        return score;
    }
}
