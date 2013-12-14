package mygame.server;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;



public class DispatcherThread extends Thread
{
	/*
	 * Codes are:
	 * 0  - POSITION UPDATE		C->S
	 * 1  - LOGON				C->S
	 * 2  - LOGOFF				C->S
	 * 3  - MESSAGE				C<->S
	 * 4  - USER ON				S->C
	 * 5  - USER OFF			S->C
	 * 6  - LOGON SUCCESSFUL	S->C
	 * 7  - LOGON FAILED		S->C
	 */
	
	private BlockingQueue<Event> queue;
	private ConcurrentHashMap<String, PrintWriter> printWriters;
	private ArrayList<String> usersOnline;
	
	private String lastBalloonMsg = null;
	private String[] topScorerNames = new String[3];
	private int[] topScorerInts = new int[3];

	public DispatcherThread(BlockingQueue<Event> queue, ConcurrentHashMap<String, PrintWriter> printWriters)
	{
		super();
		this.queue = queue;
		this.printWriters = printWriters;
		usersOnline = new ArrayList<String>();
	}

	public void run()
	{
		System.out.println("Dispatcher thread is up!");

		while (true)
		{
			try
			{
				Event e = queue.take();
				PrintWriter destination = null;
				
				switch (e.eventCode)
				{
					//position update
				case 0:
					sendToAllBut(e.msg1, "0 " + e.msg1 + " " + e.msg2 + "\n");
					break;
					
					//log on
				case 1:
					System.out.println("Dispatcher thread: Logon by " + e.msg1);
					usersOnline.add(e.msg1);
					sendOnlineList(e.msg1);
					sendToAllBut(e.msg1, "4 " + e.msg1 + "\n");
					break;

					//log off
				case 2:
					System.out.println("Dispatcher thread: Logoff by " + e.msg1);
					usersOnline.remove(e.msg1);
					sendToAll("5 " + e.msg1 + "\n");
					break;

					//message
				case 3:
					destination = printWriters.get(e.msg1);
					if (destination != null)
					{
						System.out.println("Dispatcher thread: Message from " + e.msg1 + " to " + e.msg2 + ": " + e.msg3.substring(0, e.msg3.length()));
						destination.write("3 " + e.msg1 + " " + e.msg2 + " " + e.msg3 + "\n");
						destination.flush();
					}
					else
					{
						System.out.println("Dispatcher thread: Message could not be sent from " + e.msg1 + " to " + e.msg2 + "!");
						destination = printWriters.get(e.msg1);
						if (destination != null)
						{
							destination.write("12 " + e.msg1 + " " + e.msg2 + " " + e.msg3 + "\n");
							destination.flush();
						}
					}
					break;
					
				case 8:
					sendToAllBut(e.msg2, "8 " + e.msg1 + "\n");
					break;
					
				case 9:
					lastBalloonMsg = "9 " + e.msg1 + "\n";
					sendToAllBut(e.msg1, lastBalloonMsg);
					break;
                
                case 10:
					lastBalloonMsg = "10 " + e.msg1 + "\n";
                    sendToAll(lastBalloonMsg);
                    break;
                
                case 11:
                    sendToAllBut(e.msg1, "11 " + e.msg1 + " " + e.msg2 + "\n");
					updatePlayerTopScores(e.msg1, Integer.parseInt(e.msg2));
					break;

				default:
					System.err.println("Dispatcher thread: Unknown eventCode: " + e.toString());
					break;
				}
			}
			catch (InterruptedException e)
			{
				System.out.println("Dispatcher thread: InterruptedException!");
				e.printStackTrace();
			}
			catch (NullPointerException e)
			{
				System.out.println("Dispatcher thread: incorrect number of arguments!");
			}
		}

	}
	
	private void sendOnlineList(String user)
	{
		PrintWriter userStream = printWriters.get(user);
		
		if (userStream == null)
		{
			System.err.println("Dispatcher thread (sendInitialBuddies): No Printwriter found for " + user + "!");
			return;
		}
		
		for (String u : usersOnline)
		{
			if (!u.equals(user))
				userStream.write("4 " + u + "\n");
		}
		
		userStream.write(lastBalloonMsg);
		
		if (topScorerNames[0] != null)
			userStream.write("11 " + topScorerNames[0] + " " + topScorerInts[0] + "\n");
		if (topScorerNames[1] != null)
			userStream.write("11 " + topScorerNames[1] + " " + topScorerInts[1] + "\n");
		if (topScorerNames[2] != null)
			userStream.write("11 " + topScorerNames[2] + " " + topScorerInts[2] + "\n");
		
		userStream.flush();
	}
	
	private void sendToAll(String msg)
	{
		for (String u : usersOnline)
		{
			PrintWriter pw = printWriters.get(u);
			if (pw != null)
			{
				pw.write(msg);
				pw.flush();
			}
		}
	}
	
	private void sendToAllBut(String user, String msg)
	{
		for (String u : usersOnline)
		{
			PrintWriter pw = printWriters.get(u);
			if (pw != null && u != user)
			{
				pw.write(msg);
				pw.flush();
			}
		}
	}
	
	private void updatePlayerTopScores(String user, int score)
	{
		if (topScorerNames[0] == null) {
			topScorerNames[0] = user;
			topScorerInts[0] = score;
		} else if (topScorerNames[1] == null && !topScorerNames[0].equals(user)) {
			topScorerNames[1] = user;
			topScorerInts[1] = score;
		} else if (topScorerNames[2] == null && !topScorerNames[0].equals(user) && !topScorerNames[1].equals(user)) {
			topScorerNames[2] = user;
			topScorerInts[2] = score;
		} else if (topScorerInts[0] < score && !topScorerNames[0].equals(user)) {
			
			String tmpName = topScorerNames[0];
			int tmpScore = topScorerInts[0];
			
			topScorerNames[0] = topScorerNames[1];
			topScorerInts[0] = topScorerInts[1];
			
			topScorerNames[1] = tmpName;
			topScorerInts[1] = tmpScore;
			
		} else if (topScorerNames[1] != null && topScorerInts[1] < score && !topScorerNames[1].equals(user)) {
			
			String tmpName = topScorerNames[2];
			int tmpScore = topScorerInts[2];
			
			topScorerNames[2] = topScorerNames[1];
			topScorerInts[2] = topScorerInts[1];
			
			topScorerNames[2] = tmpName;
			topScorerInts[2] = tmpScore;
			
		} else if (topScorerNames[2] != null && topScorerInts[2] < score && !topScorerNames[2].equals(user)) {
			topScorerNames[2] = user;
			topScorerInts[2] = score;
		}
	}
}
