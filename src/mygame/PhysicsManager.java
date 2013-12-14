/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author Jengel
 */
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.renderer.Camera;
import com.jme3.input.FlyByCamera;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import java.util.Random;

public class PhysicsManager {

    // The game controllers
    private final Node rootNode;
    private final AssetManager assetManager;
    private final BulletAppState bulletAppState;
    protected Node vehicleNode;
    protected VehicleControl vehicle;
    private float maxSpeed = 160f;

    public PhysicsManager(Node rootNode, AssetManager assetManager,
            InputManager inputManager, Camera camera, FlyByCamera flyCam,
            AppStateManager stateManager, BulletAppState bulletAppState) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        stateManager.attach(bulletAppState);
    }

    public VehicleControl getVehiclePhysics() {
        return vehicle;
    }

    public Node getVehicleNode() {
        return vehicleNode;
    }
    
    public void setMaxSpeed(float s) {
        maxSpeed = s;
        
        if (vehicle.getLinearVelocity().length() > maxSpeed) {
            vehicle.setLinearVelocity(vehicle.getLinearVelocity().normalize().mult(maxSpeed));
        }
    }
    
    public float getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Builds the golf cart vehicle with physics
     *
     * @param player The designed object model
     */
    public void buildVehicle(Spatial player) {
        Material wheel_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wheel_mat.setColor("Color", ColorRGBA.Black);

        //create a car collision shape
        vehicleNode = new Node("vehicleNode");
        CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(player);
        vehicleNode.attachChild(player);
        vehicle = new VehicleControl(carHull, 400f);
        vehicle.setGravity(new Vector3f(1f, 1f, 1f).mult(10f));

        // adjust vehicle locations and rotations here
        vehicleNode.setLocalTranslation(-25f, 10f, 90);

        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 50.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMass(1200);


        // Add Physics Control on Vehicle object
        vehicleNode.addControl(vehicle);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radiusf = 0.2f;
        float radiusb = 0.2f;
        float restLength = 0.3f;
        float yOff = 0.42f;
        float xOff = 0.36f;
        float zOffFront = -0.61f;
        float zOffBack = 0.71f;

        Cylinder wheelMeshf = new Cylinder(16, 16, radiusf, radiusf * 0.6f, true);
        Cylinder wheelMeshb = new Cylinder(16, 16, radiusb, radiusb * 0.6f, true);

        // Front Right Wheel
        Node node2 = new Node("wheel Front Right node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMeshf);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(wheel_mat);
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOffFront),
                wheelDirection, wheelAxle, restLength, radiusf, false);

        // Back Right Wheel
        Node node1 = new Node("wheel Back Right node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMeshf);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(wheel_mat);
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOffFront),
                wheelDirection, wheelAxle, restLength, radiusf, false);

        // Front Left Wheel
        Node node3 = new Node("wheel Front Left node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMeshb);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(wheel_mat);
        vehicle.addWheel(node3, new Vector3f(xOff, yOff, zOffBack),
                wheelDirection, wheelAxle, restLength, radiusb, true);

        // Back Left Wheel
        Node node4 = new Node("wheel Back Left node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMeshb);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(wheel_mat);
        vehicle.addWheel(node4, new Vector3f(-xOff, yOff, zOffBack),
                wheelDirection, wheelAxle, restLength, radiusb, true);

        // add wheels to the object and then to the real world.
        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);
        
        rootNode.attachChild(vehicleNode);
        bulletAppState.getPhysicsSpace().add(vehicleNode);

        vehicleNode.setModelBound(new BoundingSphere());
        vehicleNode.updateModelBound();
        
        this.resetVehicle();
    }
    
    /**
     * Resets the Vehicle to starting position
     */
    public void resetVehicle() {
        Random r = new Random();
        vehicle.setPhysicsLocation(new Vector3f(76.37f + (r.nextInt(10) - 5), 11.46f, -103.0f));
        vehicle.setPhysicsRotation(new Quaternion(0.0f, 0.123f, 0, 0.992f));
        vehicle.setLinearVelocity(Vector3f.ZERO);
        vehicle.setAngularVelocity(Vector3f.ZERO);
        vehicle.resetSuspension();
    }

    public void updateVehicle() {
        vehicle.brake(vehicle.getLinearVelocity().length() * 4);
    }

    public void accelerateVehicle(float amount) {         
        if (vehicle.getCurrentVehicleSpeedKmHour() < maxSpeed) {
            vehicle.accelerate(amount);
        }
        
        else {
            vehicle.accelerate(-amount);
        }
    }
}
