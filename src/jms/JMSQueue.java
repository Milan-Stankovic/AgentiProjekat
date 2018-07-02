package jms;

import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import model.ACLPoruka;

public class JMSQueue {
	Logger log = Logger.getLogger("JSMQUEUE");
	
	public JMSQueue(ACLPoruka message) {
		try {
			Context context = new InitialContext();
			
			ConnectionFactory cf = (ConnectionFactory) context.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
		    final Queue queue = (Queue) context.lookup("java:jboss/exported/jms/queue/mojQueue");
		    context.close();
				   
			Connection connection = cf.createConnection();//"guest", "guestguest");
			final Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			connection.start();

		    ObjectMessage msg = session.createObjectMessage(message);
		    long sent = System.currentTimeMillis();
		    msg.setLongProperty("sent", sent);
		    
			MessageProducer producer = session.createProducer(queue);

			log.info("Saljem poruku na queue: " + msg.getObject());
			producer.send(msg);
			
			producer.close();
			session.close();
			connection.close();
		    
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public JMSQueue(ACLPoruka aclMessage, long delay) {
		try {
			Context context = new InitialContext();
			
			ConnectionFactory cf = (ConnectionFactory) context.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
		    final Queue queue = (Queue) context.lookup("java:jboss/exported/jms/queue/mojQueue");
		    context.close();
				   
			Connection connection = cf.createConnection();//"guest", "guestguest");
			final Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			connection.start();

		    ObjectMessage msg = session.createObjectMessage(aclMessage);
		    long sent = System.currentTimeMillis();
		    msg.setLongProperty("sent", sent);
		    
			MessageProducer producer = session.createProducer(queue);

			log.info("Saljem poruku na queue: " + msg.getObject());
			producer.setDeliveryDelay(delay);
			producer.send(msg);
			
			producer.close();
			session.close();
			connection.close();
		    
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
}

}
