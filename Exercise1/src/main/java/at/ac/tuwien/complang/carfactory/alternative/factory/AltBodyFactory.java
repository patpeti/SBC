package at.ac.tuwien.complang.carfactory.alternative.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.alternative.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.application.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Body;

public class AltBodyFactory extends AltAbstractFactory implements IProducer {
	private Connection connection = null;
	private Session session;
	//Fields
	private long id; //The ID of this producer

	public AltBodyFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	

	public void produce() {
		Body body = new Body(id);
		System.out.println("Produced a body with ID: " + body.getId());
		
		System.out.println("writing Body into jms...");
		
		ActiveMQConnectionFactory conFac = new ActiveMQConnectionFactory();
        try {
        	
        	
		connection = conFac.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic t = session.createTopic(QueueConstants.BODYTOPIC);
//		Queue q = session.createQueue(QueueConstants.MOTORQUEUE);
		MessageProducer msgProducer = session.createProducer(t);
		msgProducer.send(session.createObjectMessage(body));
		
		
		connection.close();
		getListener().onObjectWrittenInQueue(body);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
