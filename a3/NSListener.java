import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;


public class NSListener extends Thread{
	public Socket socket= null;
	boolean flag_init=true;
	private static Integer clientID=1;
	//private static Socket[] SocketList=null;
	private static ObjectOutputStream[] bos= null;
	private int clientcount;
	private static ConcurrentHashMap<Integer, NSPacket> clientTable= new ConcurrentHashMap();
	public static Player[] players= new Player[NameServer.MAXCLIENT];
	public static Random randomGen = null;

	public NSListener(Socket socket, ObjectOutputStream[] bos, int clientcount){
		this.socket=socket;
		this.bos= bos;
		this.clientcount=clientcount;
	}

	public void run(){

		try{
			ObjectInputStream is= new ObjectInputStream(socket.getInputStream());
			//ObjectOutputStream os= new ObjectOutputStream(socket.getOutputStream());
			ObjectOutputStream os= bos[clientcount];
			//when getting a packet from the client, extract ip, port
			while(true){

				NSPacket received= (NSPacket) is.readObject();
				System.out.println("Received " + received);

				if(received.type== NSPacket.JOIN){
					/*client wants to join
					check if someone is using the same PORT and NAME
					*/
					//System.out.println("inside if");
					Set<Integer> set= clientTable.keySet();
					Iterator<Integer> it= set.iterator();
					while( it.hasNext()){
						//Map.Entry map= it.next();
						Integer map= it.next();
						NSPacket fromTable= (NSPacket) (clientTable.get(map));
						if(fromTable.name ==received.name){
							System.out.println("from NSListner: name already in use");
							os.writeObject(new NSPacket(NSPacket.JOIN_FAILED_NAME));
							break;
						}
						if(fromTable.port == received.port){
							System.out.println("from NSListner: port already in use");
							os.writeObject(new NSPacket(NSPacket.JOIN_FAILED_PORT));
							break;
						}
					}
					

					clientTable.put(clientID, received);
					if(randomGen == null){
                 	  randomGen = new Random(received.mazeSeed); 
                	}
					Point point = new Point(randomGen.nextInt(received.mazeWidth),randomGen.nextInt(received.mazeHeight));
					Player player= new Player(clientID, received.name, point, Player.North);
					players[clientID-1]= player;
					clientID++;

				}
				System.out.println("clientTable: " + clientTable +" MAXCLIENT: "+ NameServer.MAXCLIENT);
				if((clientTable.size() == NameServer.MAXCLIENT)){
					//send client list 
					System.out.println("when all player is connected...");
					System.out.println(players);
					//os.writeObject(new NSPacket(NSPacket.UPDATE));

					//broadcast
					for(int i=0; i< NameServer.MAXCLIENT; i++){
						
						System.out.println("broadcasting");
						bos[i].writeObject(new NSPacket(NSPacket.UPDATE, clientTable, players));
					}

				}

			}
		}
		catch(Exception e){
			System.out.println(e);
		}


	}


}