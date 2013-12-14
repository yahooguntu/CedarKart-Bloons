/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yahooguntu
 */
public class WorldMaker {

    private static String HEIGHT_MAP = "Textures/terrain/cedarville.png";
    private static String ALPHA_MAP = "Textures/terrain/alphamap.png";
    private static String ALPHA_TEX_1 = "Textures/terrain/sidewalk.jpg";
    private static String ALPHA_TEX_2 = "Textures/terrain/grass.png";
    private static String ALPHA_TEX_3 = "Textures/terrain/asphalt.jpg";
    private static String PLACEMENT_FILE = "Models/placement.txt";
    private static BulletAppState bullet;
    private static float OBJECT_SCALE = 1.75f;
    private static float LOCATION_SCALE = 2.0f;
    private static float OBJECT_TRANSLATE_X = 0.0f;
    private static float OBJECT_TRANSLATE_Y = 0f;
    private static float OBJECT_TRANSLATE_Z = 0.0f;
    private TerrainQuad terrain;
    private Node placedObjects;
    private SimpleApplication app;

    public Node generateWorld(SimpleApplication app, BulletAppState blt) {
        this.app = app;
        bullet = blt;
        terrain = generateTerrain(app.getAssetManager(), app.getCamera());
        placedObjects = generatePlacedObjects(app, terrain);
        placedObjects.setName("placedObjects");
        app.getRootNode().attachChild(placedObjects);
        
        Thread t = new Thread(objUpdater);
		t.setDaemon(true);
        t.start();

        return terrain;
    }

    /**
     * Generates the terrain from HEIGHT_MAP, ALPHA_MAP, and ALPHA_TEX_[1-3].
     *
     * @param assetManager
     * @param camera
     * @return TerrainQuad The newly-made terrain object.
     */
    private TerrainQuad generateTerrain(AssetManager assetManager, Camera camera) {
        Material terrainMaterial;

        // init terrain
        terrainMaterial = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrainMaterial.setBoolean("useTriPlanarMapping", false);

        // ALPHA map (for splat textures)
        terrainMaterial.setTexture("AlphaMap", assetManager.loadTexture(ALPHA_MAP));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture(HEIGHT_MAP);

        // sidewalk texture
        Texture sidewalk = assetManager.loadTexture(ALPHA_TEX_1);
        sidewalk.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMap", sidewalk);
        terrainMaterial.setFloat("DiffuseMap_0_scale", 128f);

        // grass texture
        Texture grass = assetManager.loadTexture(ALPHA_TEX_2);
        grass.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMap_1", grass);
        terrainMaterial.setFloat("DiffuseMap_1_scale", 16f);

        // asphalt texture
        Texture asphalt = assetManager.loadTexture(ALPHA_TEX_3);
        asphalt.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMap_2", asphalt);
        terrainMaterial.setFloat("DiffuseMap_2_scale", 64f);

        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        /**
         * Optimal terrain patch size is 65 (64x64). The total size is up to
         * you. At 1025 it ran fine for me (200+FPS), however at size=2049, it
         * got really slow. But that is a jump from 2 million to 8 million
         * triangles...
         */
        TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, camera);
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f)); // patch size, and a multiplier
        terrain.addControl(control);
        terrain.setMaterial(terrainMaterial);
        terrain.setLocalTranslation(0f, -7.45f, 0f);
        terrain.setLocalScale(2f, 0.12f, 2f);

        terrain.setName("terrain");
        CollisionShape terrainShape = CollisionShapeFactory.createMeshShape(terrain);
        RigidBodyControl landscape = new RigidBodyControl(terrainShape, 0);
        terrain.addControl(landscape);
        bullet.getPhysicsSpace().add(terrain);

        return terrain;
    }
    private Runnable objUpdater = new Runnable() {
        public void run() {
            while (true) {
                try {
                    updatePlacedObjects();
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(WorldMaker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    private Node generatePlacedObjects(Application app, TerrainQuad terrain) {
        Node placedObjects = new Node();
        placedObjects.setName("placedObjects");

        try {
            Scanner s = new Scanner((String) app.getAssetManager().loadAsset(PLACEMENT_FILE));
            s.useDelimiter("\n");

            while (s.hasNext()) {
                try {
                    String objStr = s.next();
                    if (objStr.trim().length() == 0 || objStr.charAt(0) == '#') {
                        continue;
                    }

                    String[] objDef = objStr.split(", ?");

                    String name = objDef[0].trim().substring(0, objDef[0].length() - 1);
                    String filePath = objDef[1].trim().substring(1, objDef[1].length() - 1);
                    float locationX = Float.valueOf(objDef[2].trim()) * LOCATION_SCALE + OBJECT_TRANSLATE_X;
                    float locationY = Float.valueOf(objDef[3].trim()) + OBJECT_TRANSLATE_Y;
                    float locationZ = Float.valueOf(objDef[4].trim()) * LOCATION_SCALE + OBJECT_TRANSLATE_Z;
                    float scale = Float.valueOf(objDef[5].trim()) * OBJECT_SCALE;
                    float rotationX = Float.valueOf(objDef[6].trim());
                    float rotationY = Float.valueOf(objDef[7].trim());
                    float rotationZ = Float.valueOf(objDef[8].trim());
                    float rotationW = Float.valueOf(objDef[9].trim());

                    Spatial spatial = (Spatial) app.getAssetManager().loadModel(filePath);
                    spatial.setName(name);
                    spatial.setLocalTranslation(new Vector3f(locationX, locationY, locationZ));
                    spatial.setLocalScale(scale);
                    spatial.setLocalRotation(new Quaternion(rotationX, rotationY, rotationZ, rotationW));

                    CollisionShape buildingShape = CollisionShapeFactory.createMeshShape(spatial);
                    RigidBodyControl building = new RigidBodyControl(buildingShape, 0);
                    spatial.addControl(building);
                    bullet.getPhysicsSpace().add(spatial);

                    placedObjects.attachChild(spatial);
                } catch (AssetNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return placedObjects;
    }

    private void updatePlacedObjects() {
        try {
            Scanner s = new Scanner((String) app.getAssetManager().loadAsset(PLACEMENT_FILE));
            s.useDelimiter("\n");

            while (s.hasNext()) {
                try {
                    String objStr = s.next();
                    if (objStr.trim().length() == 0 || objStr.charAt(0) == '#') {
                        continue;
                    }

                    String[] objDef = objStr.split(", ?");

                    String name = objDef[0].trim().substring(0, objDef[0].length() - 1);
                    float locationX = Float.valueOf(objDef[2].trim()) * LOCATION_SCALE + OBJECT_TRANSLATE_X;
                    float locationY = Float.valueOf(objDef[3].trim()) + OBJECT_TRANSLATE_Y;
                    float locationZ = Float.valueOf(objDef[4].trim()) * LOCATION_SCALE + OBJECT_TRANSLATE_Z;
                    float scale = Float.valueOf(objDef[5].trim()) * OBJECT_SCALE;
                    float rotationX = Float.valueOf(objDef[6].trim());
                    float rotationY = Float.valueOf(objDef[7].trim());
                    float rotationZ = Float.valueOf(objDef[8].trim());
                    float rotationW = Float.valueOf(objDef[9].trim());

                    Spatial spatial = placedObjects.getChild(name);
                    Vector3f translationVector = new Vector3f(locationX, locationY, locationZ);
                    Quaternion rotationVector = new Quaternion(rotationX, rotationY, rotationZ, rotationW);

                    if (!spatial.getLocalTranslation().equals(translationVector)) {
                        enqueueTranslation(spatial, translationVector);
                    }
                    if (!spatial.getLocalRotation().equals(rotationVector)) {
                        enqueueRotation(spatial, rotationVector);
                    }
                } catch (AssetNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enqueueRotation(final Spatial spatial, final Quaternion rot) {
        app.enqueue(new Callable<Spatial>() {
            public Spatial call() throws Exception {
                spatial.setLocalRotation(rot);
                return spatial;
            }
        });
    }

    private void enqueueTranslation(final Spatial spatial, final Vector3f trans) {
        app.enqueue(new Callable<Spatial>() {
            public Spatial call() throws Exception {
                spatial.setLocalTranslation(trans);
                return spatial;
            }
        });
    }
}