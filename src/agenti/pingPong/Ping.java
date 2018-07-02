package agenti.pingPong;

import java.util.ArrayList;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.swing.plaf.synth.SynthSpinnerUI;

import jms.JMSQueue;
import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Baza;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Ping extends Agent{

	@EJB
	Baza db;
	
	@Override
	public void stop() {
		System.out.println("Ping agent finished its job.");
	}

	@Override
	public void handleMessage(ACLPoruka poruka) {
		System.out.println("Ping has rcived message, well see what it does. "+poruka);
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			ACLPoruka aclPoruka = new ACLPoruka();
			aclPoruka.setSender(this.getAid());
			aclPoruka.setReceivers(new AID[]{poruka.getSender()});
			aclPoruka.setConversationID(poruka.getConversationID());
			aclPoruka.setPerformative(Performative.REQUEST);
			new JMSQueue(aclPoruka);
		}else if(poruka.getPerformative().equals(Performative.INFORM)) {
			System.out.println("Pong has responded, awesome.");
		}
	}
	
	/*private AID[] findPongAgents() {
		ArrayList<AID> ai = db.getAgentByType("Pong");
		if (ai.isEmpty())
			return new AID[]{};
		else
			return new AID[]{ai.get(0)};
	}*/
}
