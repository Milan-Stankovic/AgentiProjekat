package jms;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;



@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/jms/queue/mojQueue") })

public class PrimalacQueueMDB implements MessageListener {
	
	Logger log = Logger.getLogger("Primalac MDB");


	public void onMessage(Message msg) {
		log.info("RADI TEST ZA JMS");
		
	}

	
}