package at.ac.tuwien.complang.carfactory.application.producers.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.TimeConstants;
import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsWheelFactory extends JmsAbstractFactory {

	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;
	private Topic topic;
	private MessageProducer messageProducer;
	private ActiveMQConnectionFactory connectionFactory;

	public JmsWheelFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
		connectionFactory = new ActiveMQConnectionFactory();
	}

	private void connect() {
		System.out.println("[WheelFactory] Connecting to Queues");
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(QueueConstants.WHEELTOPIC);
			messageProducer = session.createProducer(topic);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void produce() {
		Wheel wheel = new Wheel(id);
		double random = Math.random();
		if(random < errorRate) {
			wheel.setDefect(true);
		}
		try {
			if(connection == null) {
				connect();
			}
			//notify the GUI first, because we need to make sure that the object is in the table model, before the gui gets a notification to remove it again.
			getListener().onObjectWrittenInQueue(wheel);
			messageProducer.send(session.createObjectMessage(wheel));
			System.out.println("Produced a wheel with ID: " + wheel.getId());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
	
	@Override
	public double timeInSec() {
		return TimeConstants.WHEEL_TIME_IN_SEC;
	}
	
	@Override
	public void finished() {
		setChanged();
		notifyObservers("WHEEL");
		try {
			messageProducer.close();
			session.close();
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session = null;
			connection = null;
			topic = null;
			messageProducer = null;
		}
	}
}
