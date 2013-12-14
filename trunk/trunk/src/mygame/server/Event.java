package mygame.server;

public class Event
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
	
	public int eventCode;
	public String msg1 = null;
	public String msg2 = null;
	public String msg3 = null;
	
	Event(int eventCode, String msg1, String msg2, String msg3)
	{
		this.eventCode = eventCode;
		this.msg1 = msg1;
		this.msg2 = msg2;
		this.msg3 = msg3;
	}
	
	Event(int eventCode, String msg1, String msg2)
	{
		this.eventCode = eventCode;
		this.msg1 = msg1;
		this.msg2 = msg2;
	}
	
	Event(int eventCode, String msg)
	{
		this.eventCode = eventCode;
		this.msg1 = msg;
	}
	
	public String toString()
	{
		return "[code=" + eventCode + ",msg1=" + msg1 + ",msg2=" + msg2 + ",msg3=" + msg3 + "]";
	}
}
