import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Messages {
	public static ACLMessage Subscribe(int type,String name,String content,boolean islocal) {
		ACLMessage message = new ACLMessage(type);
		message.addReceiver(new AID(name,islocal));
		message.setContent(content);
		return message;
	}
}
