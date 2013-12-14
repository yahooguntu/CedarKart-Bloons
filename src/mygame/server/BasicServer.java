package mygame.server;

import java.io.*;
import java.net.*;

public class BasicServer
{
	private int listenPort;
	private int maxConnections;
	protected int numConnections;
	private int inc = 1;

	public BasicServer()
	{
		this (4220, 0);
	}
	
	public BasicServer(int port, int numConnects)
	{
		listenPort = port;
		maxConnections = numConnects;
		numConnections = 0;
	}

	public void start ()
	{

		try
		{
			ServerSocket listenSocket = new ServerSocket (listenPort);
			System.out.println("Server started.");
			
			while (true)
			{
				Socket newConnect;
				if ( (maxConnections == 0) || (numConnections <= maxConnections) )
				{
					newConnect = listenSocket.accept();
					System.out.println("Accepted " + newConnect.getInetAddress().toString().substring(1));
					System.out.flush();
					numConnections++;
					serviceConnection(newConnect);
				}
			}
			
			
		}
		catch (IOException ioe )
		{
			System.out.println("Unable to listen on port "+listenPort);
			if(inc < 100)
			{
				listenPort++;
				inc++;
			    System.out.println("Attemt[" + inc + "]: retrying conncection on new port...");
			    start();				
			}
			else
			{
				listenPort -= 100;
				System.out.println("Too many attempts. Try with a diferent port. ");
			}
			
		}
	}
	
	// overridden
	protected void serviceConnection (Socket connection) throws IOException
	{

		BufferedReader in = new BufferedReader
				(new InputStreamReader(connection.getInputStream()));

		PrintWriter out = new PrintWriter
				(new PrintWriter(connection.getOutputStream(), true));

		out.println("Hello from your server");
		out.flush();

		System.out.println("Closing connection");
		System.out.flush();

		connection.close();
		numConnections--;
	}
	
}