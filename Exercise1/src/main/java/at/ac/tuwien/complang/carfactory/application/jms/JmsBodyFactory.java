package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.application.xvsm.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsBodyFactory extends JmsAbstractFactory implements IProducer {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;

	public JmsBodyFactory(long id, IQueueListener listener) {
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
			MessageProducer msgProducer = session.createProducer(t);
			//object message
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
	
	@Override
	public int timeInSec() {
		return TIME_IN_SEC;
	}
}
