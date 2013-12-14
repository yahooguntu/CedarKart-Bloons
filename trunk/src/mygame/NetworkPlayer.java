/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author yahooguntu
 */
public class NetworkPlayer extends Node implements BalloonHolder {

	private VehicleControl vehicleControl;
	private AssetManager assetMan;
	private BulletAppState bulletAppState;
	private boolean hasBalloon = false;
	private Spatial balloon;
        private int score;

	public NetworkPlayer(AssetManager assetMan, BulletAppState bulletAppState, String username) {
		this.assetMan = assetMan;
		this.bulletAppState = bulletAppState;

		balloon = assetMan.loadModel("Models/balloon.mesh.xml");
		balloon.setName("balloon");
		balloon.setLocalTranslation(0f, 2.8f, 0f);
		balloon.setLocalScale(1f);

		Material wheel_mat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
		wheel_mat.setColor("Color", ColorRGBA.Black);

		//create a car collision shape
		setName(username);

		Spatial cart = assetMan.loadModel("Models/vehicles/golfCart/golfCart.mesh.xml");
		cart.scale(0.1f);
		cart.setModelBound(new BoundingSphere());
		cart.updateModelBound();

		CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(cart);
		attachChild(cart);
		VehicleControl vehicle = new VehicleControl(carHull, 400f);


		// adjust vehicle locations and rotations here
		setLocalTranslation(-25f, 10f, 90);

		//setting suspension values for wheels, this can be a bit tricky
		//see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
		float stiffness = 50.0f;//200=f1 car
		float compValue = .3f; //(should be lower than damp)
		float dampValue = .4f;
		vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
		vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
		vehicle.setSuspensionStiffness(stiffness);
		vehicle.setMass(900);


		// Add Physics Control on Vehicle object
		addControl(vehicle);

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
		attachChild(node1);
		attachChild(node2);
		attachChild(node3);
		attachChild(node4);

		bulletAppState.getPhysicsSpace().add(this);

		setModelBound(new BoundingSphere());
		updateModelBound();

		Quaternion rotation = new Quaternion();
		rotation.fromAngleAxis(-FastMath.PI / 3.4f, new Vector3f(-696.03f, 0.0f, -455.76f));
		vehicle.setPhysicsRotation(rotation);
		vehicle.setPhysicsLocation(new Vector3f(-59.20f, 7.84f, 271.17f));
		vehicle.setLinearVelocity(Vector3f.ZERO);
		vehicle.setAngularVelocity(Vector3f.ZERO);
		vehicle.resetSuspension();

		// username billboard
		BitmapFont guiFont = assetMan.loadFont("Interface/Fonts/Default.fnt");
		BitmapText ch = new BitmapText(guiFont, false);
		ch.setSize(0.5f);
		ch.setText(username);
		ch.setColor(new ColorRGBA(1f, 0.8f, 0.3f, 0.8f));
		ch.setLocalTranslation(0f, 2.2f, 0f);
		ch.addControl(new BillboardControl());

		attachChild(ch);
	}

	public boolean hasBalloon() {
             return hasBalloon;
	}
    
	public void addBalloon() {
		if (!hasBalloon) {
			hasBalloon = true;
			attachChild(balloon);
		}
	}

	public void removeBalloon() {
		if (hasBalloon) {
                    System.out.println("Remove balloon from network player");
			hasBalloon = false;
			balloon.removeFromParent();
		}
	}
        
        public Vector3f getBalloonLocation() {            
            return getLocalTranslation();
        }
        
        public void setScore(int score)
        {
            this.score = score;
        }
        
        public int getScore()
        {
            return score;
        }
        
        public String getUsername()
        {
            return getName();
        }
}
