import java.io.Serializable;

import java.io.Serializable;

public class MPacket implements Serializable {

    /*The following are the type of events*/
    public static final int HELLO = 100;
    public static final int ACTION = 200;
    public static final int ACK= 300;
    public static final int ACKM= 400;
    /*The following are the specific action 
    for each type*/
    /*Initial Hello*/
    public static final int HELLO_INIT = 101;
    /*Response to Hello*/
    public static final int HELLO_RESP = 102;


    /*Action*/
    public static final int UP = 201;
    public static final int DOWN = 202;
    public static final int LEFT = 203;
    public static final int RIGHT = 204;
    public static final int FIRE = 205;
    public static final int MOVEPROJECTILE= 206;
    
    //These fields characterize the event  
    public int type;
    public int event; 
    public boolean retransmit=false;

    //The name determines the client that initiated the event
    public String name;
    public int senderID;
    public int targetID;
    
    //The sequence number of the event
    public double sequenceNumber;
    public double targetSeq;


    //These are used to initialize the board
    public int mazeSeed;
    public int mazeHeight;
    public int mazeWidth; 
    //public Player[] players;

    public MPacket(int type, int event){
        this.type = type;
        this.event = event;
    }


    //ack packet
    public MPacket(int type, int senderID, double sequenceNumber, int targetID, double targetSeq){
        this.type= type;
        this.senderID= senderID;
        this.sequenceNumber= sequenceNumber;
        this.targetID= targetID;
        this.targetSeq= targetSeq;
    }

    public MPacket(String name, int type, int event){
        this.name= name;
        this.type= type;
        this.event= event;
    }


    
    public MPacket(String name, int senderID, int type, int event){
        this.name = name;
        this.senderID = senderID;
        this.type = type;
        this.event = event;
    }


    
    public String toString(){
        String typeStr;
        String eventStr;
        
        switch(type){
            case 100:
                typeStr = "HELLO";
                break;
            case 200:
                typeStr = "ACTION";
                break;
            case 300:
                typeStr = "ACK";
                break;
            case 400:
                typeStr = "ACKM";
                break;
            default:
                typeStr = "ERROR";
                break;        
        }
        switch(event){
            case 101:
                eventStr = "HELLO_INIT";
                break;
            case 102:
                eventStr = "HELLO_RESP";
                break;
            case 201:
                eventStr = "UP";
                break;
            case 202:
                eventStr = "DOWN";
                break;
            case 203:
                eventStr = "LEFT";
                break;
            case 204:
                eventStr = "RIGHT";
                break;
            case 205:
                eventStr = "FIRE";
                break;
            case 206:
                eventStr = "MOVEPROJECTILE";
                break;
            default:
                eventStr = "ERROR";
                break;        
        }
        //MPACKET(NAME: name, <typestr: eventStr>, SEQNUM: sequenceNumber)
        String retString = String.format("MPACKET(NAME: %s, senderID: %s, <%s: %s>, SEQNUM: %s)", name, senderID,
            typeStr, eventStr, sequenceNumber);
        return retString;
    }

}
