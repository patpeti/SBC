package at.ac.tuwien.complang.carfactory.alternative.businesslogic;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.alternative.constants.QueueConstants;

public class AltAssembler {

	private Connection connection = null;
	private Session session;
	
	public AltAssembler() {
	
		
		//test get Motor
		
		ActiveMQConnectionFactory conFac = new ActiveMQConnectionFactory();
        try {
        	
        	
		connection = conFac.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		//createQueue connects to a queue if it exists otherwise creates it
		Queue q = session.createQueue(QueueConstants.MOTORQUEUE);
		MessageConsumer myMsgConsumer = session.createConsumer(q);
		
		Message msg = myMsgConsumer.receive();
		
		System.out.println("Message received "+ msg.toString());
	
		connection.close();
        } catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	

}
