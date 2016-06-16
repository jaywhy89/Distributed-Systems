import java.io.Serializable;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

/*
For connecting to NameServer
Getting client info ,ready for initiating p2p connections
*/

public class ClientNSThread extends Thread{

	private int port=0;
	private String ip="";
	private String name= "";
	private boolean init_flag= true;
	private	BlockingQueue<MPacket> eventQueue;
	public ConcurrentLinkedQueue<MSocket> mSocketList=new ConcurrentLinkedQueue(); 
	private static ConcurrentHashMap<Integer, mQueue> fifoQueue= null;
	private String serverIP=null;
	private int serverPort=0;
	private boolean loop_flag= true;
	
	private int clientCount= 0;
	private int id=0;
	//public HashSet<Integer> id_set= new HashSet();
	private ConcurrentHashMap<MSocket, Integer> socketToid= new ConcurrentHashMap();

	public ClientNSThread(int port, String name, boolean init_flag,BlockingQueue<MPacket> eventQueue, ConcurrentHashMap fifoQueue){
		this.port= port;
		this.name= name;
		this.init_flag= init_flag;
		this.eventQueue= eventQueue;
		this.fifoQueue=fifoQueue;

	}

	public void run(){
			try{
				ip= InetAddress.getLocalHost().getHostAddress();
				//connect to NS
				Socket socket =new Socket("localhost", 8000);
				ObjectOutputStream os= new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is= new ObjectInputStream(socket.getInputStream());
				while(true){
					if(init_flag){
						System.out.println("ipaddress from ClientNSThread: "+ip);
						NSPacket hello= new NSPacket(ip, port, name, NSPacket.JOIN);
						
						os.writeObject(new NSPacket());
					}
					NSPacket fromNS= (NSPacket) is.readObject();
					System.out.println("Received " + fromNS);
					if(fromNS.type ==NSPacket.JOIN_FAILED_NAME || fromNS.type== NSPacket.JOIN_FAILED_PORT){
						System.out.println("JOINING FAIL..PLZ try again");
						System.exit(-1);
					}
					if(fromNS.type == NSPacket.UPDATE){
						Set<Integer> set= fromNS.clientTable.keySet();
						Iterator<Integer> it= set.iterator();
						while(it.hasNext()){
							Integer map= it.next();
							NSPacket fromTable= (NSPacket)(fromNS.clientTable.get(map));

							if(fromTable.port==this.port){
								id= map;
								if(map != NameServer.MAXCLIENT){
									//System.out.println("BEFOREim gonna start serversocket");
									new Thread(new ConnectionThread(id ,port,eventQueue,mSocketList, fifoQueue, socketToid)).start();
									loop_flag = false;
								}
							}
							
						}

						Iterator<Integer> itclient= set.iterator();
						while(itclient.hasNext()){
								Integer map= itclient.next();
								NSPacket fromTable= (NSPacket)(fromNS.clientTable.get(map));

								if(map < id){
									//client
									serverIP= fromTable.ip;
									serverPort=fromTable.port;
									System.out.println("gonna clientsocket to "+ serverIP+ ": " + serverPort + "currently map is: " +map);
									MSocket mSocket= new MSocket(serverIP, serverPort);

									socketToid.put(mSocket, map);
									new Thread(new ReceiverThread(id, mSocket, fifoQueue,socketToid)).start();
									mSocketList.add(mSocket);
								}
						}
						new Thread(new FIFOBroadcast(id, name, eventQueue, mSocketList, fifoQueue, socketToid)).start();

					}
					init_flag= false;
				}



			}
			catch(Exception e){
				Thread thread= Thread.currentThread();
				System.out.println("thread: "+ thread);
				System.out.println(e);
			}
		
	}

}