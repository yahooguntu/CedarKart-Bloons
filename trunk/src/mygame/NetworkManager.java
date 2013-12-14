package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.server.ThreadedIMServer;

public class NetworkManager implements Runnable {

	private final String SERVER;
	private final int PORT = 4225;
	private Socket connection;
	private PrintWriter writer;
	private BufferedReader reader;
	private ThreadedIMServer server;
	private Thread incomingMessageListener;
	private SimpleApplication app;
	private Node players;
	private PlayerManager myself;
	private BulletAppState bulletAppState;
	private AssetManager assetMan;
	private Node balloonPlayer = null;
	private BalloonHolder[] topScorers = new BalloonHolder[3];
	private boolean stopping = false;

	public NetworkManager(SimpleApplication simpleApp, String serverAddr, AssetManager assets, BulletAppState physics, PlayerManager currPlayer, boolean startServer) {
		SERVER = serverAddr;
		app = simpleApp;
		assetMan = assets;
		bulletAppState = physics;
		players = new Node("players");
		app.getRootNode().attachChild(players);
		myself = currPlayer;

		try {
			if (startServer) {
				server = new ThreadedIMServer();
				server.start();
				connection = new Socket("localhost", PORT);

			} else {
				connection = new Socket(SERVER, PORT);
			}

			writer = new PrintWriter(connection.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			incomingMessageListener = new Thread(this);
			incomingMessageListener.setDaemon(true);
			incomingMessageListener.start();

			writer.write("1 " + myself.getUsername() + "\n");
			writer.flush();

		} catch (Exception e) {
			System.err.println("Failed to open socket!");
			((Main) app).dieToStartScreen("Failed to open socket!");
			e.printStackTrace();
			connection = null;
		}
	}

	private void sendMessage(String msg) {
		if (connection != null) {
			writer.write(msg);
			writer.flush();
		}
	}

	public Node getPlayers() {
		return players;
	}

	public void cupTaken(int cupId) {
		sendMessage("8 " + cupId + "\n");
	}

	public void startGame() {
		try {
			Scanner s = new Scanner((String) assetMan.loadAsset("Models/balloon_placement.txt"));
			ArrayList<String> balloon_pos = new ArrayList<String>();

			while (s.hasNextLine()) {
				balloon_pos.add(s.nextLine());
			}

			Random r = new Random();
			String pos = balloon_pos.get(r.nextInt(balloon_pos.size()));

			sendMessage("10 " + pos + "\n");
		} catch (Exception ex) {
			Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void resetPlayer() {
		try {
			Scanner s = new Scanner((String) assetMan.loadAsset("Models/balloon_placement.txt"));
			ArrayList<String> balloon_pos = new ArrayList<String>();

			while (s.hasNextLine()) {
				balloon_pos.add(s.nextLine());
			}

			Random r = new Random();
			String pos = balloon_pos.get(r.nextInt(balloon_pos.size()));

			sendMessage("11 " + pos + "\n");
		} catch (Exception ex) {
			Logger.getLogger(NetworkManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void run() {
		String input = "";
		try {
			while (connection != null) {

				input = reader.readLine();

				int code = Integer.parseInt(input.substring(0, input.indexOf(" ")));
				String msgBody = input.substring(input.indexOf(" ") + 1).trim();
				switch (code) {
					case 0:
						// Format:  0 user locX,locY,locZ,rotX,rotY,rotZ,rotW,spdX,spdY,spdZ,rSpdX,rSpdY,rSpdZ
						String[] msgParts = msgBody.split(" ");
						String[] numbers = msgParts[1].split(",");

						updatePlayerLocation(msgParts[0],
								Float.parseFloat(numbers[0]),
								Float.parseFloat(numbers[1]),
								Float.parseFloat(numbers[2]),
								Float.parseFloat(numbers[3]),
								Float.parseFloat(numbers[4]),
								Float.parseFloat(numbers[5]),
								Float.parseFloat(numbers[6]),
								Float.parseFloat(numbers[7]),
								Float.parseFloat(numbers[8]),
								Float.parseFloat(numbers[9]),
								Float.parseFloat(numbers[10]),
								Float.parseFloat(numbers[11]),
								Float.parseFloat(numbers[12]));
						break;

					case 3:
						// incoming message
						System.out.println("Stuff");
						break;

					case 4:
						// user on
						System.out.println("User signed on: " + msgBody);

						//Spatial vehicle = generatePhysicsVehicle();
						NetworkPlayer vehicle = new NetworkPlayer(assetMan, bulletAppState, msgBody);
						vehicle.setModelBound(new BoundingSphere());
						vehicle.updateModelBound();
						//vehicle.setName(msgBody);
						addToPlayersNode(vehicle);
						break;

					case 5:
						// user off
						System.out.println("User signed off: " + msgBody);
						removeFromPlayersNode(msgBody);
						break;

					case 6:
						// successful login
						System.out.println("Successfully logged in.");
						startPositionUpdaterThread();
						break;

					case 7:
						// failed login
						System.out.println("Failed to log in.");
						System.exit(2);
						break;

					case 8:
						// cup taken remotely
						System.out.println("Cup taken remotely: " + msgBody);
						removeCup(Integer.parseInt(msgBody));
						break;

					case 9:
						// user has claimed the balloon
						System.out.println("User claimed balloon: " + msgBody);

						giveBalloonToPlayer(msgBody);

						break;

					case 10:
						// start game, with balloon position
						String[] split = msgBody.split(", ");
						resetPlayerPositions(new Vector3f(Float.parseFloat(split[0]),
								Float.parseFloat(split[1]),
								Float.parseFloat(split[2])));
						app.enqueue(new Callable<Integer>() {
							public Integer call() throws Exception {
								((Main) app).topBar.newGame();
								return null;
							}
						});
						if (((Main) app).timer == null) {
							((Main) app).timer = new Timer(((Main) app));
						}
						((Main) app).timer.reset = true;

						break;

					case 11:
						// score update
						String[] scoreMsg = msgBody.split(" ");
						BalloonHolder msgUser = (NetworkPlayer) players.getChild(scoreMsg[0]);
						int msgScore = Integer.parseInt(scoreMsg[1]);

						updatePlayerTopScores(msgUser, msgScore);
						break;
				}
			}
		} catch (NullPointerException e) {
			System.err.println("You were kicked by the server admin!");
			((Main) app).dieToStartScreen("You were kicked by the server admin!");
		} catch (Exception e) {
			System.out.println("Exception thrown!");
			System.out.println(input);
			e.printStackTrace();
			System.exit(1);
			connection = null;
		}
	}

	private void updatePlayerTopScores(BalloonHolder user, int score) {
		user.setScore(score);

		if (topScorers[0] == null) {
			topScorers[0] = user;
		} else if (topScorers[1] == null && !topScorers[0].getUsername().equals(user.getUsername())) {
			topScorers[1] = user;
		} else if (topScorers[2] == null && !topScorers[0].getUsername().equals(user.getUsername()) && !topScorers[1].getUsername().equals(user.getUsername())) {
			topScorers[2] = user;
		} else if (topScorers[0].getScore() < score && !topScorers[0].getUsername().equals(user.getUsername())) {
			BalloonHolder tmp = topScorers[0];
			topScorers[0] = topScorers[1];
			topScorers[1] = tmp;
		} else if (topScorers[1] != null && topScorers[1].getScore() < score && !topScorers[1].getUsername().equals(user.getUsername())) {
			BalloonHolder tmp = topScorers[2];
			topScorers[2] = topScorers[1];
			topScorers[2] = tmp;
		} else if (topScorers[2] != null && topScorers[2].getScore() < score && !topScorers[2].getUsername().equals(user.getUsername())) {
			topScorers[2] = user;
		}
	}

	private void resetPlayerPositions(final Vector3f pos) {
		myself.resetVehicle();

		app.enqueue(new Callable<Vector3f>() {
			public Vector3f call() throws Exception {
				((Main) app).removeBalloonFromMap();

				if (balloonPlayer != null) {
					((BalloonHolder) balloonPlayer).removeBalloon();
					balloonPlayer = null;
				}

				((Main) app).addBalloon(pos);
				return pos;
			}
		});
	}

	private void removeCup(final int i) {
		app.enqueue(new Callable<Integer>() {
			public Integer call() throws Exception {
				((Main) app).remoteCupTaken(i);
				return i;
			}
		});
	}

	private void addToPlayersNode(final NetworkPlayer player) {
		app.enqueue(new Callable<NetworkPlayer>() {
			public NetworkPlayer call() throws Exception {
				players.attachChild(player);
				return player;
			}
		});
	}

	private void removeFromPlayersNode(final String user) {
		app.enqueue(new Callable<Node>() {
			public Node call() throws Exception {
				players.detachChildNamed(user);
				return null;
			}
		});
	}

	private void updatePlayerLocation(final String user, final float locX, final float locY, final float locZ, final float rotX, final float rotY, final float rotZ, final float rotW, final float spdX, final float spdY, final float spdZ, final float rAccX, final float rAccY, final float rAccZ) {
		app.enqueue(new Callable<Node>() {
			public Node call() throws Exception {
				Spatial userNode = players.getChild(user);

				//userNode.setLocalTranslation(locX, locY, locZ);
				//userNode.setLocalRotation(new Quaternion(rotX, rotY, rotZ, rotW));

				VehicleControl vC = userNode.getControl(VehicleControl.class);

				vC.setPhysicsLocation(new Vector3f(locX, locY, locZ));
				vC.setPhysicsRotation(new Quaternion(rotX, rotY, rotZ, rotW));

				vC.setLinearVelocity(new Vector3f(spdX, spdY, spdZ));
				vC.setAngularVelocity(new Vector3f(rAccX, rAccY, rAccZ));

				return null;
			}
		});
	}

	private void startPositionUpdaterThread() {
		System.out.println("Updater Started.");

		Runnable senderThread = new Runnable() {
			public void run() {
				try {
					while (!stopping) {
						Vector3f loc = myself.getTranslation();
						Quaternion rot = myself.getRotation();
						VehicleControl vC = myself.physicsManager().getVehiclePhysics();
						Vector3f linAcc = vC.getLinearVelocity();
						Vector3f rotAcc = vC.getAngularVelocity();

						writer.write("0 "
								+ loc.getX() + ","
								+ loc.getY() + ","
								+ loc.getZ() + ","
								+ rot.getX() + ","
								+ rot.getY() + ","
								+ rot.getZ() + ","
								+ rot.getW() + ","
								+ linAcc.getX() + ","
								+ linAcc.getY() + ","
								+ linAcc.getZ() + ","
								+ rotAcc.getX() + ","
								+ rotAcc.getY() + ","
								+ rotAcc.getZ() + ","
								+ "\n");
						writer.flush();

						Thread.sleep(100L);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(senderThread);
		t.setDaemon(true);
		t.start();
	}

	private void giveBalloonToPlayer(String username) {
		if (username == null) {
			return;
		}

		Thread t = new Thread(((Main) app).collideUpdater);
		t.setDaemon(true);
		t.start();

		final Node player = (Node) players.getChild(username);
		final Node oldBalloonPlayer = balloonPlayer;
		balloonPlayer = player;

		if (username.equals(myself.getName())) {
			balloonPlayer = myself;
		}

		app.enqueue(new Callable<Node>() {
			public Node call() throws Exception {
				if (balloonPlayer != null) {
					((BalloonHolder) balloonPlayer).addBalloon();
					if (balloonPlayer != oldBalloonPlayer) {
						((Main) app).removeBalloonFromMap();

						if (oldBalloonPlayer != null) {
							((BalloonHolder) oldBalloonPlayer).removeBalloon();
						}
					}
				}

				return null;
			}
		});
	}

	//get player with balloon get balloon
	public Node getPlayerWithBalloon() {
		return balloonPlayer;
	}

	public void claimBalloon() {
		final BalloonHolder oldBallooner = (BalloonHolder) balloonPlayer;
		balloonPlayer = myself;

		app.enqueue(new Callable<NetworkPlayer>() {
			public NetworkPlayer call() throws Exception {
				if (oldBallooner != null) {
					oldBallooner.removeBalloon();
				}
				return null;
			}
		});

		writer.write("9 " + myself.getUsername() + "\n");
	}

	public void updateScore(int points) {
		writer.write("11 " + points + "\n");
		writer.flush();

		updatePlayerTopScores(myself, points);
	}

	public BalloonHolder[] getTopScores() {
		return topScorers;
	}

	public void shutdown() {
		writer.write("2 " + myself.getUsername() + "\n");
		writer.flush();
		stopping = true;

		connection = null;
	}
}
