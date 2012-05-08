package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Motor;

public class AltAssembler {

	private Connection connection = null;
	private Session session;
	private Queue wheelQueue, motorQueue;
	private Topic carTopic, bodyTopic;
	
	public AltAssembler() {
		/**
		 * TODO:
		 * 1. Connect to all queues/topics
		 * 2. load a body (from bodyTopic)
		 * 3. load 4 wheels (from wheelQueue)
		 * 4. load a motor (from motorQueue)
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 7. mark the body, wheels and motor as already used 
		 *    (or alternatively remove them from the space)
		 *    FIXME: decide if objects should remain in the space -->all infos still available inside the car objects no need to have the original objects in the space
		 * 6. save the car object back into the space
		 */
	}
	
	public void initialize() {
		connectToQueues();
	}
	
	public void startAssemblyLoop() {
		while(true) {
			//produce some cars
		}
		//disconnect from queue
	}

	private void connectToQueues() {
		//test get Motor
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.motorQueue = session.createQueue(QueueConstants.MOTORQUEUE);
			this.wheelQueue = session.createQueue(QueueConstants.WHEELQUEUE);
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			MessageConsumer messageConsumer = session.createConsumer(motorQueue);
			ObjectMessage message = (ObjectMessage) messageConsumer.receive();
			Motor motor = (Motor) message.getObject();
			System.out.println("Motor: " + motor.getId());
			System.out.println("Message received "+ message.toString());
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
