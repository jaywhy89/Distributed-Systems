import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.*;
import java.util.ArrayList;

public class ReceiverThread implements Runnable{

	private MSocket mSocket=null;
	private ConcurrentHashMap<Double, mQueue> fifoQueue= null;
	private int myID;
	public ConcurrentHashMap<MSocket, Integer> socketToid=null;
	private ConcurrentHashMap<Double, MPacket> receivedfifo= new ConcurrentHashMap();
	public BlockingQueue<MPacket> eventQueue;
	public boolean init= true;

	private ConcurrentSkipListMap<Double, MPacket>readyQueue;
	private ConcurrentHashMap<Double, mQueue> ackmQueue;
	public ReceiverThread(int myID, MSocket mSocket, ConcurrentHashMap<Double, mQueue> fifoQueue, ConcurrentHashMap<MSocket, Integer> socketToid,BlockingQueue<MPacket> eventQueue,ConcurrentSkipListMap<Double, MPacket>readyQueue,ConcurrentHashMap<Double, mQueue> ackmQueue){
		this.myID= myID;
		this.mSocket= mSocket;
		this.fifoQueue= fifoQueue;
		this.socketToid=socketToid;
		this.eventQueue= eventQueue;
		this.readyQueue= readyQueue;
		this.ackmQueue= ackmQueue;
	}

	public void run(){
		//receiverfifo to executable packet THREAD

		while(true){
			try{
				//System.out.println("IM LISTENING");
				MPacket packet= (MPacket) mSocket.readObject();
				//System.out.println("sucessfuly read packet");
				if(init){
					
					if(Debug.debug)System.out.println("Starting a packetOrdering");
					new Thread(new PacketOrdering(mSocket,receivedfifo, socketToid, (double) packet.senderID/10, readyQueue, ackmQueue)).start();
					init= false;
				}
				
				if(!(socketToid.containsValue(packet.senderID))){
					//if doesn't have the mapping, insert
					socketToid.put(mSocket, packet.senderID);
				}
				
				if(packet.type == MPacket.ACK && packet.targetID== myID){
					//System.out.println("Received ACK for" + packet.sequenceNumber+ " from " + packet.senderID);
					mQueue queue=fifoQueue.get(packet.sequenceNumber);
					//int senderID = queue.packet.senderID;
					int senderID= packet.senderID;
					if(!(queue.acked_id.contains(senderID))){
						//then add myID to the set
						queue.acked_id.add(senderID);
						if(Debug.debug)System.out.println("ACK "+packet.sequenceNumber+  ": "+ queue.acked_id);
					}
					if(queue.acked_id.size()== NameServer.MAXCLIENT -1){
						//all ack was received.. safely remove seq# from fifoQueue
						readyQueue.put(packet.sequenceNumber, fifoQueue.get(packet.sequenceNumber).packet);
						fifoQueue.remove(packet.sequenceNumber);
						if(Debug.debug)System.out.println("removed "+ packet.sequenceNumber+" from fifoQueue");
					}
				}
				else if(packet.type == MPacket.ACTION){
					if(Debug.debug)System.out.println("Received ACTION" + packet + " from" + packet.name);
					if(!receivedfifo.containsKey(packet.sequenceNumber)){	
						MPacket ackm= new MPacket(MPacket.ACKM, myID, packet.sequenceNumber, packet.senderID, packet.sequenceNumber);
						eventQueue.put(ackm);
					}

					receivedfifo.put(packet.sequenceNumber, packet);
					//System.out.println("RECIEVEDFIFO: " + receivedfifo);

					MPacket ack_packet= new MPacket(MPacket.ACK, myID, packet.sequenceNumber, packet.senderID,packet.sequenceNumber);
					ack_packet.retransmit= true;
					eventQueue.put(ack_packet);
				}
				else if(packet.type ==MPacket.ACKM){
					//if ACKM recieved

					if(Debug.debug)System.out.println("Received ACKM for " + packet.targetSeq + " from " + packet.senderID);
					MPacket ack_packet= new MPacket(MPacket.ACK, myID, packet.sequenceNumber, packet.senderID, packet.sequenceNumber);
					ack_packet.retransmit= true;
					eventQueue.put((MPacket) ack_packet);
					receivedfifo.put(packet.sequenceNumber, packet);


				}
			}catch(Exception e){
				System.out.println("ERROR IN RECEVIER");
				System.out.println(e);
			}
		}
	}
}