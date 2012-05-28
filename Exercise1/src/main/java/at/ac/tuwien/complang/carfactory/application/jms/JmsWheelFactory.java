package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsWheelFactory extends JmsAbstractFactory {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;
	private ActiveMQConnectionFactory connectionFactory;

	public JmsWheelFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
		connectionFactory = new ActiveMQConnectionFactory();
	}

	private void connect() {
		try {
			connection = connectionFactory.createConnection();
			connection.start();
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
		System.out.println("Produced a wheel with ID: " + wheel.getId());
		
		System.out.println("writing wheel into jms...");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			if(connection == null) {
				connect();
			}
			if(session == null){
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
			connection = connectionFactory.createConnection();
			connection.start();
		
			Queue queue = session.createQueue(QueueConstants.WHEELQUEUE);
			MessageProducer msgProducer = session.createProducer(queue);
			//notify the GUI first, because we need to make sure that the object is in the table model, before the gui gets a notification to remove it again.
			getListener().onObjectWrittenInQueue(wheel);
			msgProducer.send(session.createObjectMessage(wheel));
//			connection.close();
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
		notifyObservers("WHEEL");
		try {
			connection.close();
			session.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session = null;
			connection = null;
		}
	}
}
