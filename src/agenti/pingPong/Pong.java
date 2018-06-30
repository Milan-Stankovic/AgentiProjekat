package agenti.pingPong;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import jms.JMSQueue;
import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Baza;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Pong extends Agent{
	
	@Override
	public void stop() {
		System.out.println("Pong agent finished its job.");
	}

	@Override
	public void handleMessage(ACLPoruka poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			ACLPoruka aclPoruka = new ACLPoruka();
			aclPoruka.setSender(this.getAid());
			aclPoruka.setReceivers(new AID[]{poruka.getSender()});
			aclPoruka.setConversationID(poruka.getConversationID());
			aclPoruka.setPerformative(Performative.INFORM);
			new JMSQueue(aclPoruka);
		}
	}
}
