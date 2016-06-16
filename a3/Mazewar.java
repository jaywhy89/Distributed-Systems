import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Mazewar extends JFrame{

    private  ConcurrentHashMap<MSocket, Integer> socketToid;
    public  ConcurrentLinkedQueue<MSocket> mSocketList;
	private  BlockingQueue<MPacket> eventQueue;
	private  ConcurrentHashMap<Double, mQueue> fifoQueue;
    private ConcurrentHashMap<Double, mQueue> ackmQueue= new ConcurrentHashMap<Double, mQueue>();

    private ConcurrentSkipListMap<Double, MPacket>readyQueue= new ConcurrentSkipListMap<Double, MPacket>();
	private   String name;
    private String ip;
    private int port;
    private int id;

    private  ScoreTableModel scoreModel= null;
	//private ArrayList<MPacket> receivedfifo= new ArrayList<MPacket>();

	/**
         * The default width of the {@link Maze}.
         */
        private static final int mazeWidth = 20;

        /**
         * The default height of the {@link Maze}.
         */
        private static final int mazeHeight = 10;

        /**
         * The default random seed for the {@link Maze}.
         * All implementations of the same protocol must use 
         * the same seed value, or your mazes will be different.
         */
        private static final int mazeSeed = 42;

        /**
         * The {@link Maze} that the game uses.
         */
        private static Maze maze = null;

        /**
         * The Mazewar instance itself. 
         */
        private Mazewar mazewar = null;
        private MSocket mSocket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;

        /**
         * The {@link GUIClient} for the game.
         */
        private GUIClient guiClient = null;
        
        
        //ConcurrentHashMap<Integer, NSPacket> clientTable=null;

        private Hashtable<String, Client> playerTable=null; 
        
        /**
         * The panel that displays the {@link Maze}.
         */
        private OverheadMazePanel overheadPanel = null;

        /**
         * The table the displays the scores.
         */
        private JTable scoreTable = null;
        
        /** 
         * Create the textpane statically so that we can 
         * write to it globally using
         * the static consolePrint methods  
         */
        private static final JTextPane console = new JTextPane();
      
        /** 
         * Write a message to the console followed by a newline.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrintLn(String msg) {
                console.setText(console.getText()+msg+"\n");
        }
        
        /** 
         * Write a message to the console.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrint(String msg) {
                console.setText(console.getText()+msg);
        }
        
        /** 
         * Clear the console. 
         */
        public static synchronized void clearConsole() {
           console.setText("");
        }
        
        /**
         * Static method for performing cleanup before exiting the game.
         */
        public static void quit() {
                // Put any network clean-up code you might have here.
                // (inform other implementations on the network that you have 
                //  left, etc.)
                

                System.exit(0);
        }


        public Mazewar(String name, String ip, int port) throws IOException, ClassNotFoundException {

            super("ECE419 Mazewar");
            consolePrintLn("ECE419 Mazewar started!");

            this.name= name;
            this.ip= ip;
            this.port= port;
                
            socketToid= new ConcurrentHashMap();
            mSocketList=new ConcurrentLinkedQueue(); 
            eventQueue = new LinkedBlockingQueue<MPacket>();
            fifoQueue= new ConcurrentHashMap();
                // Create the maze
                maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
                assert(maze != null);
                
                // Have the ScoreTableModel listen to the maze to find
                // out how to adjust scores.
                scoreModel = new ScoreTableModel();
                assert(scoreModel != null);
                maze.addMazeListener(scoreModel);
                
                // // Throw up a dialog to get the GUIClient name.
                // String name = JOptionPane.showInputDialog("Enter your name");
                // if((name == null) || (name.length() == 0)) {
                //   Mazewar.quit();
                // }

        }


    public void initialize(){

        try{
            
            //connect to NS
            Socket socket =new Socket("localhost", 8000);
            ObjectOutputStream os= new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream is= new ObjectInputStream(socket.getInputStream());
            System.out.println("ipaddress from ClientNSThread: "+ip);
            NSPacket hello= new NSPacket(ip, port, name, NSPacket.JOIN);
            hello.mazeWidth = mazeWidth;
            hello.mazeHeight = mazeHeight;
                    
                os.writeObject(hello);
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

                        if(fromTable.port==port){
                            id= map;
                            if(map != NameServer.MAXCLIENT){
                                System.out.println("BEFOREim gonna start serversocket");
                                new Thread(new ConnectionThread(id ,port,eventQueue,mSocketList, fifoQueue, socketToid,readyQueue, ackmQueue)).start();
                            }
                        }
                        
                    }

                    Iterator<Integer> itclient= set.iterator();
                    while(itclient.hasNext()){
                            Integer map= itclient.next();
                            NSPacket fromTable= (NSPacket)(fromNS.clientTable.get(map));

                            if(map < id){
                                //client
                                String serverIP= fromTable.ip;
                                int serverPort=fromTable.port;
                                System.out.println("gonna clientsocket to "+ serverIP+ ": " + serverPort + "currently map is: " +map);
                                MSocket mSocket= new MSocket(serverIP, serverPort);

                                socketToid.put(mSocket, map);
                                new Thread(new ReceiverThread(id, mSocket, fifoQueue,socketToid,eventQueue,readyQueue, ackmQueue)).start();
                                mSocketList.add(mSocket);
                            }
                    }

                    //Initialize hash table of clients to client name 
                    System.out.println("IM gonna update playerTable " + fromNS.players[id-1]);
                    playerTable = new Hashtable<String, Client>(); 
                    
                    // Create the GUIClient and connect it to the KeyListener queue
                    //RemoteClient remoteClient = null;
                    for(Player player: fromNS.players){  
                            System.out.println("inside forloop");
                            if(player.name.equals(name)){
                                if(Debug.debug)System.out.println("Adding guiClient: " + player);
                                    guiClient = new GUIClient(name, eventQueue);
                                    maze.addClientAt(guiClient, player.point, player.direction);
                                    this.addKeyListener(guiClient);
                                    playerTable.put(player.name, guiClient);
                            }else{
                                if(Debug.debug)System.out.println("Adding remoteClient: " + player);
                                    RemoteClient remoteClient = new RemoteClient(player.name);
                                    maze.addClientAt(remoteClient, player.point, player.direction);
                                    playerTable.put(player.name, remoteClient);
                            }
                    }

                    new Thread(new FIFOBroadcast(id, name, eventQueue, mSocketList, fifoQueue, socketToid, (double) id/10, readyQueue, ackmQueue)).start();
                    new Thread(new ExecuteThread(readyQueue, ackmQueue, playerTable)).start();

                }
            // Create the panel that will display the maze.
                overheadPanel = new OverheadMazePanel(maze, guiClient);
                assert(overheadPanel != null);
                maze.addMazeListener(overheadPanel);
                
                // Don't allow editing the console from the GUI
                console.setEditable(false);
                console.setFocusable(false);
                console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
               
                // Allow the console to scroll by putting it in a scrollpane
                JScrollPane consoleScrollPane = new JScrollPane(console);
                assert(consoleScrollPane != null);
                consoleScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Console"));
                
                // Create the score table
                scoreTable = new JTable(scoreModel);
                assert(scoreTable != null);
                scoreTable.setFocusable(false);
                scoreTable.setRowSelectionAllowed(false);

                // Allow the score table to scroll too.
                JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
                assert(scoreScrollPane != null);
                scoreScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scores"));
                
                // Create the layout manager
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                getContentPane().setLayout(layout);
                
                // Define the constraints on the components.
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 1.0;
                c.weighty = 3.0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                layout.setConstraints(overheadPanel, c);
                c.gridwidth = GridBagConstraints.RELATIVE;
                c.weightx = 2.0;
                c.weighty = 1.0;
                layout.setConstraints(consoleScrollPane, c);
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1.0;
                layout.setConstraints(scoreScrollPane, c);
                                
                // Add the components
                getContentPane().add(overheadPanel);
                getContentPane().add(consoleScrollPane);
                getContentPane().add(scoreScrollPane);
                
                // Pack everything neatly.
                pack();

                // Let the magic begin.
                setVisible(true);
                overheadPanel.repaint();
                this.requestFocusInWindow();


            }catch(Exception e){

                System.out.println(e);
            }

    }

	public static void main(String[] args) throws IOException,
                                        ClassNotFoundException{
		try{

            String ip= InetAddress.getLocalHost().getHostAddress();
			int port= Integer.parseInt(args[0]);
			String name= (args[1]);
            Mazewar mazewar= new Mazewar(name, ip ,port);
            mazewar.initialize(); 
			//new ClientNSThread(port, name, init_flag, eventQueue, fifoQueue).start();
			
			

		}catch(Exception e){
			System.out.println(e);
		}
	} 
	
	
}