import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.HashSet;

public class SenderThread implements Runnable{

	public MSocket mSocket=null;
	public MPacket toPeer= null;
	public double sequenceNumber;
    private ConcurrentHashMap<Double, mQueue> fifoQueue= null;
    private int senderID;
   // public HashSet<Integer> id_set= null;
    private ConcurrentHashMap<MSocket, Integer> socketToid= null;
    private BlockingQueue<MPacket> eventQueue = null;
    private ConcurrentSkipListMap<Double, MPacket>readyQueue;
    private ConcurrentHashMap<Double, mQueue> ackmQueue;

	public SenderThread(int senderID, MSocket mSocket, MPacket toPeer, double sequenceNumber, 
                                    ConcurrentHashMap<Double, mQueue> fifoQueue,
                                    ConcurrentHashMap<MSocket, Integer> socketToid, BlockingQueue<MPacket> eventQueue, ConcurrentSkipListMap<Double, MPacket>readyQueue, ConcurrentHashMap<Double, mQueue> ackmQueue){
        this.senderID= senderID;
		this.mSocket= mSocket;
		this.toPeer= toPeer;
		this.sequenceNumber= sequenceNumber;
        this.fifoQueue=fifoQueue;
        this.socketToid= socketToid;
        this.eventQueue= eventQueue;
        this.readyQueue=readyQueue;
        this.ackmQueue= ackmQueue;

	}

	public void run(){
        if(Debug.debug)System.out.println("IM SENDING " +toPeer.sequenceNumber + " event: "+ toPeer.type);
        toPeer.senderID= senderID;

		mSocket.writeObject((MPacket)toPeer);
        if(!(toPeer.type==MPacket.ACK)){
            Timer timeout= new Timer();
            timeout.schedule(new TimeOut(senderID,mSocket,toPeer, socketToid, fifoQueue), 10000);
        }
	}

    class TimeOut extends TimerTask{
        private int senderID;
    	private MPacket packet;
        private MSocket mSocket;
        private ConcurrentHashMap<MSocket, Integer> socketToid;
        private ConcurrentHashMap<Double,mQueue> fifoQueue;

    	TimeOut(int senderID ,MSocket mSocket,MPacket packet, ConcurrentHashMap<MSocket, Integer> socketToid, ConcurrentHashMap<Double,mQueue> fifoQueue){
            this.senderID= senderID;
            this.mSocket= mSocket;
    		this.packet= packet;
            this.socketToid= socketToid;
            this.fifoQueue= fifoQueue;
    	}

    	public void run(){
            //int seq= packet.sequenceNumber;
            if(fifoQueue.containsKey(packet.sequenceNumber)){

                mQueue queue= fifoQueue.get(packet.sequenceNumber);
                int from= socketToid.get(mSocket);
                //System.out.println(queue.acked_id);
                if(!(queue.acked_id.contains(from))){
        		  //System.out.println("timer triggered. ACK for "+ packet.sequenceNumber + " wasn't recevied from" + from);
                    if(Debug.debug)System.out.println("retransmission to "+from);
                    toPeer.retransmit= true;
                    try{
                        eventQueue.put((MPacket)toPeer);
                    }catch(Exception e){
                        System.out.println("ERROR IN SENDERTHREAD");
                        System.out.println(e);
                    }
                    //new Thread(new SenderThread(senderID, mSocket, toPeer, sequenceNumber, fifoQueue, socketToid)).start();
                }
            }

    	}
    }
}
