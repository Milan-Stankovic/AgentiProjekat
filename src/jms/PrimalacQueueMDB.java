package jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.ACLPoruka;
import model.AgentInterface;
import model.Baza;



@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/jms/queue/mojQueue") })

public class PrimalacQueueMDB implements MessageListener {
	
	@EJB
	private Baza db;

	public void onMessage(Message msg) {
		//System.out.println("RADI TEST ZA JMS");
		try {
			ObjectMessage omsg = (ObjectMessage) msg;
			try {
				ACLPoruka poruka = (ACLPoruka) omsg.getObject();
				System.out.println("Message recived. Message content: " + poruka);

				if (poruka.getReceivers() == null || poruka.getReceivers().length == 0) {
					System.out.println("No recievers found. Dummy message.");
					return;
				}

				// pronadji agente za koga je poruka
				for (int i = 0; i < poruka.getReceivers().length; i++) {
					if (db.getLokalniCentar().getAddress().equals(poruka.getReceivers()[i].getHost().getAddress())) {
						AgentInterface agent = db.getAgentWithAID(poruka.getReceivers()[i]);
						if (agent == null) {
							System.out.println("Reciever: "+poruka.getReceivers()[i]+" not found");
						} else {
							agent.handleMessage(poruka);
						}
					} else {
						ACLPoruka novaPoruka = new ACLPoruka(poruka, i);
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target("http://" + poruka.getReceivers()[i].getHost().getAddress()+ ":8096/AgentiProjekat/rest/agentskiCentar/messages");
						target.request(MediaType.APPLICATION_JSON).post(Entity.entity(novaPoruka, MediaType.APPLICATION_JSON));
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
}
	}

	
}