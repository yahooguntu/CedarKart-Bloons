package mygame.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadedIMServer extends BasicServer implements Runnable
{
	private BlockingQueue<Event> dispatchQueue;
	public ConcurrentHashMap<String, PrintWriter> printWriters;
	
	public ThreadedIMServer()
	{
		super(4225, 0);
		dispatchQueue = new ArrayBlockingQueue<Event>(20);
		printWriters = new ConcurrentHashMap<String, PrintWriter>();
		
		// fire up the dispatcher
		DispatcherThread dispatcher = new DispatcherThread(dispatchQueue, printWriters);
		dispatcher.start();
	}

	public static void main(String[] args)
	{
		ThreadedIMServer myServer = new ThreadedIMServer();

		// monitor heartbeat, etc.
		ServerMonitorThread monitor = new ServerMonitorThread(myServer);
		monitor.start();

		// spins on ServerSocket
		myServer.start();
	}

	protected void serviceConnection(Socket connection) throws IOException
	{
		ServerConnectionThread connectThread = new ServerConnectionThread(this, connection);
		connectThread.start();
	}
	
	// queues up an event to be dispatched
	public void queueEventDispatch(Event e)
	{
		try
		{
			dispatchQueue.put(e);
		} catch (InterruptedException ex) {
			System.out.println("Failed to enter event into dispatch queue: " + e.toString());
			ex.printStackTrace();
		}
	}
	
	public boolean userSignOn(String user, PrintWriter output)
	{
		if (printWriters.get(user) != null)
		{
			return false;
		}
		
		printWriters.put(user, output);
		
		return true;
	}
	
	public void userSignOff(String user)
	{
		printWriters.remove(user);
	}
	
	public boolean isLoggedOn(String user)
	{
		return printWriters.containsKey(user);
	}

	/*
	 * The realm of the monitor thread!
	 */
	public void run()
	{
		System.out.println("Console started!");
		
		Scanner s = new Scanner(System.in);
		s.useDelimiter("\n");
		
		while (true)
		{
			String input = s.next();
			String[] cmd = input.split("[ \n]");
			if (cmd[0].equals("say"))
			{
				String msg = "";
				for (int i = 2; i < cmd.length; i++)
					msg += cmd[i];

				queueEventDispatch(new Event(3, "Server", cmd[1], msg));
			}
			else if (cmd[0].equals("broadcast"))
			{
				String msg = "";
				for (int i = 1; i < cmd.length; i++)
					msg += cmd[i] + " ";

				for (String user : printWriters.keySet())
				{
					queueEventDispatch(new Event(3, "Server", user, msg));
				}
			}
			else if (cmd[0].equals("kick"))
			{
				if (cmd[1].equals("all"))
				{
					Set<String> users = printWriters.keySet();
					for (String u : users)
					{
						PrintWriter pw = printWriters.remove(u);
						pw.write("7 " + u + "\n");
						pw.flush();
						pw.close();
					}
				}
				else
				{
					PrintWriter pw = printWriters.remove(cmd[1]);
					if (pw != null)
					{
						pw.write("7 " + cmd[1] + "\n");
						pw.flush();
						pw.close();
					}
				}
			}
			else if (cmd[0].equals("list"))
			{
				Set<String> users = printWriters.keySet();
				System.out.println("Online users:");
				for (String u : users)
				{
					System.out.println("\t" + u);
				}
			}
			else if (cmd[0].equals("exit"))
			{
				System.exit(0);
			}
			else
				System.out.println("Malformed Command! Use 'say [user] [message]' or 'broadcast [message]'.");
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onCloseConnection()
	{
		synchronized(this)
		{
			numConnections--;
		}
	}
}