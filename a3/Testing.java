import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;



public class Testing implements Runnable{

	private String name;
	private BlockingQueue<MPacket> eventQueue;
	private int myID;

	public Testing(String name, int myID,BlockingQueue<MPacket> eventQueue){
		this.name= name;
		this.myID= myID;
		this.eventQueue= eventQueue;
	}

	public void run(){
		Timer timer=new Timer();
		timer.schedule(new Task(), 15000, 5000);
	}

	class Task extends TimerTask{
		public void run(){
			System.out.println("putting to eventQueue");
			try{
				eventQueue.put(new MPacket(name, myID, MPacket.ACTION, MPacket.UP));
			}catch(Exception e){
				System.out.println(e);
			}

		}
	}
}