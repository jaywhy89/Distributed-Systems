import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

public class ConnectionThread implements Runnable {

	private int port=0;
	private int id=0;
	private	BlockingQueue<MPacket> eventQueue;
	public ConcurrentLinkedQueue<MSocket> mSocketList=null;
	private ConcurrentHashMap<Double, mQueue> fifoQueue= null;
	private ConcurrentHashMap<MSocket, Integer> socketToid= null;
	private ConcurrentSkipListMap<Double, MPacket>readyQueue;
	private ConcurrentHashMap<Double, mQueue> ackmQueue;

	public ConnectionThread(int id,int port, BlockingQueue<MPacket> eventQueue, 
				ConcurrentLinkedQueue mSocketList, ConcurrentHashMap<Double, mQueue> fifoQueue, ConcurrentHashMap<MSocket, Integer> socketToid,ConcurrentSkipListMap<Double, MPacket>readyQueue,
				ConcurrentHashMap<Double, mQueue> ackmQueue){
		this.port= port;
		this.id= id;
		this.eventQueue= eventQueue;
		this.mSocketList= mSocketList;
		this.fifoQueue= fifoQueue;
		this.socketToid= socketToid;
		this.readyQueue= readyQueue;
		this.ackmQueue= ackmQueue;
	}
	public void run(){

		try{

			System.out.println("My ID is " +id + " and port is" + port);
			MServerSocket mServerSocket= new MServerSocket(port);

			while(true){
				System.out.println("in ConnectionThread..accepting connection");
				MSocket mSocket = mServerSocket.accept();
				new Thread(new ReceiverThread(id, mSocket, fifoQueue, socketToid, eventQueue, readyQueue, ackmQueue)).start();
				System.out.println("before adding to mSocketList");
				mSocketList.add(mSocket);
				System.out.println("mSocketList: " + mSocketList);
			}
		
		}catch(Exception e){
			            Thread thread= Thread.currentThread();
            System.out.println("thread: "+ thread);
			System.out.println(e);
		}
	}
}

