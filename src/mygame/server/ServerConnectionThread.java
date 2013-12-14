package mygame.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ServerConnectionThread extends Thread
{
	private ThreadedIMServer server;
	private Socket connection;

	public ServerConnectionThread(ThreadedIMServer server, Socket connection)
	{
		super();

		this.server = server;
		this.connection = connection;
		String name = connection.getInetAddress().toString();
		name = name.substring(name.indexOf("/")+1);
		this.setName(name);
	}

	public void run()
	{
		String user = null;
		BufferedReader in = null;
		PrintWriter out = null;
		String input = null;
		System.out.println("Thread spun off for " + getName());
		try
		{
			ServerConnectionThread currThread = (ServerConnectionThread) Thread.currentThread();
			Socket socket = currThread.getSocket();
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());

			input = in.readLine();
			//TODO this needs a timeout of some sort
			while (true)
			{
				try
				{
					//die if the connection is closed
					if (input == null)
					{
						break;
					}

					int msgCode = Integer.parseInt(input.substring(0, input.indexOf(" ")));
					String msgBody = input.substring(input.indexOf(" ") + 1).trim();
					
					if (msgCode != 0)
						System.out.println(input);
					
					//Position+Speed
					if(msgCode == 0 && user != null)
					{
						server.queueEventDispatch(new Event(0, user, msgBody));
					}
					// Log on
					else if(msgCode == 1 && user == null)
					{
						String msgUsername = msgBody.toLowerCase();
						if (!server.isLoggedOn(msgUsername) && server.userSignOn(msgUsername, out))
						{
							user = msgUsername;
							out.write("6 " + msgUsername + "\n");
							out.flush();
							server.queueEventDispatch(new Event(1, user));
						}
						else
						{
							out.write("7 " + msgUsername + "\n");
							out.flush();
							System.out.println("Incorrect password or already signed in: " + msgUsername);
						}
					}
					// Log off
					else if(msgCode == 2 && user != null)
					{
						server.userSignOff(user);
						server.queueEventDispatch(new Event(2, user));
						user = null;
						connection.close();
						return;
					}
					// Outgoing/incoming message
					else if(msgCode == 3 && user != null)
					{
						int splitLoc = msgBody.indexOf(" ");
						
						if (splitLoc == -1)
						{
							System.out.println("Incorrect message format from " + getName() + ": " + input);
						}
						else
						{
							String recipient = msgBody.substring(0, splitLoc).toLowerCase();
							String message = msgBody.substring(splitLoc + 1);
							
							server.queueEventDispatch(new Event(3, user, recipient, message));
						}
					}
					// cup notification
					else if(msgCode == 8 && user != null)
					{
						server.queueEventDispatch(new Event(8, msgBody, user));
					}
					// balloon claim notification
					else if(msgCode == 9 && user != null)
					{
						server.queueEventDispatch(new Event(9, msgBody));
					}
					
                                        // Reset all players to starting position
                                        else if (msgCode == 10 && user != null) {
                                            server.queueEventDispatch(new Event(10, msgBody));
                                        }
                                        else if (msgCode == 11 && user != null) {
                                            server.queueEventDispatch(new Event(11, user, msgBody));
                                        }
                                                                               
					input = in.readLine();
				}
				catch (StringIndexOutOfBoundsException e)
				{
					System.out.println("Invalid/illegal message from " + getName() + ": " + input);
				}
			}
		}

		catch (SocketException e)
		{
			System.err.println("Connection reset!");
			if (user != null)
			{
				server.userSignOff(user);
				server.queueEventDispatch(new Event(2, user));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			// kill off this thread and close its resources
			System.out.println("Thread suicide: " + getName());
			if (user != null)
			{
				server.userSignOff(user);
				server.queueEventDispatch(new Event(2, user));
			}

			server.onCloseConnection();

			try
			{
				connection.close();
			}
			catch (Exception e){}
		}
	}

	public ThreadedIMServer getServer()
	{
		return server;
	}

	public Socket getSocket()
	{
		return connection;
	}
}
