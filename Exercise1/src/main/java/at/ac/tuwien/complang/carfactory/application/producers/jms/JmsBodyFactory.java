package at.ac.tuwien.complang.carfactory.application.producers.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsBodyFactory extends JmsAbstractFactory {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;
	private Topic topic;
	private MessageProducer messageProducer;
	private ActiveMQConnectionFactory connectionFactory;

	public JmsBodyFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
		connectionFactory = new ActiveMQConnectionFactory();
	}
	
	private void connect() {
		System.out.println("[BodyFactory] Connecting to Queues");
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(QueueConstants.BODYTOPIC);
			messageProducer = session.createProducer(topic);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void produce() {
		Body body = new Body(id);
		double random = Math.random();
		if(random < errorRate) {
			body.setDefect(true);
		}
		try {
			if(connection == null) {
				connect();
			}
			//notify the GUI first, because we need to make sure that the object is in the table model, before the GUI gets a notification to remove it again.
			getListener().onObjectWrittenInQueue(body);
			messageProducer.send(session.createObjectMessage(body));
			System.out.println("Produced a body with ID: " + body.getId());
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
	
	@Override
	public void finished() {
		setChanged();
		notifyObservers("BODY");
		try {
			messageProducer.close();
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			connection = null;
			session = null;
			topic = null;
			messageProducer = null;
		}
	}
}
