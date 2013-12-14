
package mygame.server;

public class ServerMonitorThread extends Thread
{
	private ThreadedIMServer server;
	
	public ServerMonitorThread(BasicServer server)
	{
		super((Runnable) server);
	}
}
