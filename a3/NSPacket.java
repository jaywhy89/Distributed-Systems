import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;


public class NSPacket implements Serializable{

	public static final int JOIN =100;
	public static final int JOIN_SUCCESS= 101;
	public static final int JOIN_FAILED_NAME= 102;
	public static final int JOIN_FAILED_PORT= 103;
	public static final int UPDATE=200;
	public static final int QUIT=300;


  //These are used to initialize the board
    public int mazeSeed;
    public int mazeHeight;
    public int mazeWidth; 



	
	public String ip="";
	public int port=0;
	public String name="";
	public int type=0;
	public ConcurrentHashMap<Integer, NSPacket> clientTable=null;
	public Player[] players= null;


	public NSPacket(String ip, int port, String name, int type){
		this.ip= ip;
		this.port= port;
		this.name= name;
		this.type= type;
	}
	public NSPacket(int type){
		this.type= type;
	}

	public NSPacket(int type, ConcurrentHashMap clientTable, Player[] players){
		this.type= type;
		this.clientTable= clientTable;
		this.players= players;

	}

	public String toString(){

	String retString = String.format("NSPacket(ip: %s, port: %d, name: %s, type: %d)", ip, port, name, type);
        return retString;
	}

}