import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.ArrayList;

public class ClientListenerThread implements Runnable {

    private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    private int local_seq= 0;
    private ArrayList<MPacket> msg_queue= new ArrayList<MPacket>();

    public ClientListenerThread( MSocket mSocket,
                                Hashtable<String, Client> clientTable){
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        if(Debug.debug) System.out.println("Instatiating ClientListenerThread");
    }
    
    public void dequeue_seq(boolean flag, int i){
        if(flag){
            Client client = null;
            System.out.println("index from queue that contains "+ local_seq + "is "+ i+ "and the content is "+ msg_queue.get(i));
            MPacket dequeued = msg_queue.get(i);
            
            if(dequeued.event == MPacket.UP){
                client = clientTable.get(dequeued.name);
                client.forward();
            }else if(dequeued.event == MPacket.DOWN){
                client = clientTable.get(dequeued.name);
                client.backup();

            }else if(dequeued.event == MPacket.LEFT){
                client = clientTable.get(dequeued.name);
                client.turnLeft();
            }else if(dequeued.event == MPacket.RIGHT){
                client = clientTable.get(dequeued.name);
                client.turnRight();

            }else if(dequeued.event == MPacket.FIRE){
                client = clientTable.get(dequeued.name);
                client.fire();
            }else if(dequeued.event == MPacket.MOVEPROJECTILE){
               // Maze maze= this.maze;
                //maze.moveProjectile();

                client = clientTable.get(dequeued.name);
                client.moveprojectiles();
                //System.out.println(maze);
            }else{
                throw new UnsupportedOperationException();
            } 

            msg_queue.remove(i); 
            local_seq++;   

        }
    }

    public void run() {
        MPacket received = null;

        if(Debug.debug) System.out.println("Starting ClientListenerThread");
        while(true){
            try{
                received = (MPacket) mSocket.readObject();
                System.out.println("Received " + received);

                msg_queue.add(received);
                int i=0;
                boolean contains_flag= true;

                while(!msg_queue.isEmpty() && contains_flag){
                    contains_flag= false;
                    for(i=0; i<msg_queue.size(); i++){
                        if(msg_queue.get(i).sequenceNumber == local_seq){
                            contains_flag= true;
                            break;
                        }
                    }
                    dequeue_seq(contains_flag, i);
                }
                
             

            }catch(IOException e){
                Thread.currentThread().interrupt();    
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }            
        }
    }
}
