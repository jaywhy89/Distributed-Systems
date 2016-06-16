import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;


public class ExecuteThread implements Runnable {
	private ConcurrentHashMap<Double, mQueue> ackmQueue;
	private ConcurrentSkipListMap<Double, MPacket>readyQueue;
	private ConcurrentSkipListMap<Double, MPacket> actionQueue= new ConcurrentSkipListMap<Double, MPacket>();
	public Hashtable<String, Client> playerTable=null;

	public ExecuteThread(ConcurrentSkipListMap<Double, MPacket> readyQueue, ConcurrentHashMap<Double, mQueue> ackmQueue, Hashtable<String, Client> playerTable){
		this.readyQueue= readyQueue;
		this.ackmQueue= ackmQueue;
		this.playerTable= playerTable;
	}


	public void run(){
		while(true){
			//while(!readyQueue.isEmpty() || !actionQueue.isEmpty()){
				/*double key=readyQueue.firstKey();
				//System.out.println("FIRSTKEY:" +key);

				if(ackmQueue.containsKey(key)&&ackmQueue.get(key).acked_id.size() >= NameServer.MAXCLIENT-1){
					ackmQueue.remove(key);
					MPacket packet=readyQueue.get(key);
					System.out.println("executing: "+ key);
					readyQueue.remove(key);
				}*/
				//System.out.println("READYQUEUE: "+ readyQueue );
				//System.out.println("ackmQueue: " + ackmQueue);
				//sort ACTION and ACKM
				try{
				if(!readyQueue.isEmpty()){

					Map.Entry<Double, MPacket> entry= readyQueue.pollFirstEntry();

					MPacket packet= (MPacket) entry.getValue();
					System.out.println("EXECUTE: " + packet +" targetseq "+ packet.targetSeq);
					if(packet.type== MPacket.ACTION){
						//System.out.println("ACTIONQUEUE: ACTION");
						actionQueue.put(entry.getKey(), entry.getValue());
					}else if(packet.type== MPacket.ACKM){
						//System.out.println("ACTIONQUEUE: ACKM");
						if(!ackmQueue.containsKey(packet.targetSeq)){
                		//	System.out.println("IM inside IF in PACKETORDERING THREAD");
                    		mQueue queue= new mQueue(packet.targetSeq, packet);
                    		queue.acked_id.add(packet.senderID);
                    		ackmQueue.put(packet.targetSeq, queue);
                    		System.out.println("acked_id for "+packet.targetSeq+ ": "+ ackmQueue.get(packet.targetSeq).acked_id);
	                    }else{
                		//	System.out.println("IM inside ELSE in PACKETORDERING THREAD");
                			mQueue queue = ackmQueue.get(packet.targetSeq);
                			queue.acked_id.add(packet.senderID);
                			System.out.println("acked_id for "+packet.targetSeq+ ": "+ ackmQueue.get(packet.targetSeq).acked_id);
                		}
					}
				}

				if(!actionQueue.isEmpty()){

					double key=actionQueue.firstKey();
					//System.out.println("FIRSTKEY:" +key);

					if(ackmQueue.containsKey(key)&&ackmQueue.get(key).acked_id.size() >= NameServer.MAXCLIENT-1){
						ackmQueue.remove(key);
						MPacket actionpacket=actionQueue.get(key);
						System.out.println("executing: "+ key);
						Client client = null;
						if(actionpacket.event == MPacket.UP){
			                client = playerTable.get(actionpacket.name);
			                client.forward();
			            }else if(actionpacket.event == MPacket.DOWN){
			                client = playerTable.get(actionpacket.name);
			                client.backup();

			            }else if(actionpacket.event == MPacket.LEFT){
			                client = playerTable.get(actionpacket.name);
			                client.turnLeft();
			            }else if(actionpacket.event == MPacket.RIGHT){
			                client = playerTable.get(actionpacket.name);
			                client.turnRight();

			            }else if(actionpacket.event == MPacket.FIRE){
			                client = playerTable.get(actionpacket.name);
			                client.fire();
						}
						actionQueue.remove(key);
					}
				}
			}catch(Exception e){
				System.out.println(e);
			}


				
			
		}
	}
}