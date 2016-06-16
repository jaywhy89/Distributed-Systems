import java.util.*;




public class mQueue{
	public MPacket packet;
	public double sequenceNumber;

	public HashSet<Integer> acked_id= new HashSet();

	public mQueue(double sequenceNumber, MPacket packet){
		this.sequenceNumber=sequenceNumber;
		this.packet=packet;

	}
}