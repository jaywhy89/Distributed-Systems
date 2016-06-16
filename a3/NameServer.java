import java.io.*;
import java.net.*;
import java.util.concurrent.*;


public class NameServer {

	private static ObjectOutputStream[] bos= null;
	//private static Socket[] SocketList = null;
	public static final int MAXCLIENT =3;
	public static int clientcount=0;
	private static boolean flag_init=true;

	public static void main(String[] args) throws IOException{
		ServerSocket nameServerSocket=null;
		

		try{
			//create serversocket at port defined at args[0]
			//SocketList = new Socket[MAXCLIENT];
			bos = new ObjectOutputStream[MAXCLIENT];
			nameServerSocket= new ServerSocket(Integer.parseInt(args[0]));
		}
		catch(IOException e){
			System.err.println("ERROR in creating socket");
			System.exit(-1);
		}

		while(true){
			
				System.out.println("Starting a new thread...");
				Socket socket= nameServerSocket.accept();
				//SocketList[clientcount]=socket;
				bos[clientcount]= new ObjectOutputStream(socket.getOutputStream());
				System.out.println(clientcount);
				new NSListener(socket, bos, clientcount).start();
				clientcount++;
			
		}
	}
}

