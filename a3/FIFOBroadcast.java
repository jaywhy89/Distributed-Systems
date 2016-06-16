import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.*;
import java.util.HashSet;

public class FIFOBroadcast implements Runnable{

//gets MWPacket from the sender
//put it in the queue, and waits for ACK

    private BlockingQueue<MPacket> eventQueue = null;
    private ConcurrentHashMap<Double, mQueue> fifoQueue= null;
    private ConcurrentLinkedQueue<MSocket> mSocketList= null;
    private double sequenceNumber;
    private int senderID;
    
    private String name= null;
    private ConcurrentHashMap<MSocket, Integer> socketToid= null;
    private ConcurrentSkipListMap<Double, MPacket>readyQueue;
    private ConcurrentHashMap<Double, mQueue> ackmQueue;
    
    public FIFOBroadcast(int senderID, String name, BlockingQueue eventQueue, ConcurrentLinkedQueue<MSocket> mSocketList,
                    ConcurrentHashMap fifoQueue, ConcurrentHashMap<MSocket, Integer> socketToid, double sequenceNumber, 
                    ConcurrentSkipListMap<Double, MPacket>readyQueue, ConcurrentHashMap<Double, mQueue> ackmQueue){
        this.senderID= senderID;
        this.name=name;
        this.eventQueue = eventQueue;
        this.mSocketList= mSocketList;
        this.fifoQueue= fifoQueue;
        this.socketToid= socketToid;
        this.sequenceNumber= sequenceNumber+1;
        this.readyQueue= readyQueue;
        this.ackmQueue= ackmQueue;
    }

    public void run(){
        if(Debug.debug)System.out.println("broadcastingthread started");
    	MPacket toPeer= null;
       // new Thread(new Testing(name, senderID, eventQueue)).start();
    	try{
	    	while(true){
	    		//FIFO handling.. 
	    		//start timer here and queue until ack recieved..
		    	toPeer= (MPacket) eventQueue.take();
                //move the GUICLient
                if(!toPeer.retransmit && !(toPeer.type == MPacket.ACK)) {
                    //if it is not retransmit
                    
                    toPeer.sequenceNumber= sequenceNumber;
                    mQueue queue= new mQueue(sequenceNumber, toPeer);
    	    		fifoQueue.put(sequenceNumber, queue);
                    sequenceNumber++;
                }
                
                for(MSocket mSocket: mSocketList){
                    new Thread(new SenderThread(senderID, mSocket, toPeer, toPeer.sequenceNumber, fifoQueue, socketToid, eventQueue, readyQueue, ackmQueue)).start();
                }


	    	}
    	}catch(InterruptedException e){
    		 e.printStackTrace();
             System.out.println("ERROR IN FIFOBroadcast");
             Thread.currentThread().interrupt();   
    	}
    }
}
