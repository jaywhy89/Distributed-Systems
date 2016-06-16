import java.util.concurrent.*;
import java.util.ArrayList;

public class PacketOrdering implements Runnable{
	private ArrayList<MPacket> executablefifo= new ArrayList<MPacket>();
	private ConcurrentHashMap<Double, MPacket> receivedfifo= null;
	double local_seq;
	public ConcurrentHashMap<MSocket, Integer> socketToid=null;
	MSocket mSocket=null;
	private ConcurrentSkipListMap<Double, MPacket>readyQueue;
	private ConcurrentHashMap<Double, mQueue> ackmQueue;

	public PacketOrdering(MSocket mSocket,ConcurrentHashMap<Double, MPacket> receivedfifo, ConcurrentHashMap<MSocket, Integer> socketToid, 
			double local_seq,ConcurrentSkipListMap<Double, MPacket>readyQueue, ConcurrentHashMap<Double, mQueue> ackmQueue){
		this.mSocket= mSocket;
		this.receivedfifo= receivedfifo;
		this.socketToid= socketToid;
		this.local_seq= local_seq +1;
		this.readyQueue= readyQueue;
		this.ackmQueue= ackmQueue;
	}

	public void run(){
		
			//System.out.println("PACKETORERING... local_seq: " + local_seq);
		while(true){

			while(!receivedfifo.isEmpty()){
                    if(receivedfifo.containsKey(local_seq)){
                    	//remove from receivedfio
                    	//add to executable

                    	MPacket packet= receivedfifo.get(local_seq);

                    		readyQueue.put(local_seq,packet);
                
                    	receivedfifo.remove(local_seq);

                    	
                    	local_seq++;
                    }
                
                }
		}
	}

}