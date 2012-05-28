package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsBodyFactory extends JmsAbstractFactory {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;
	private ActiveMQConnectionFactory connectionFactory;

	public JmsBodyFactory(long id, IQueueListener listener) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void produce() {
		Body body = new Body(id);
		double random = Math.random();
		if(random < errorRate) {
			body.setDefect(true);
		}
		System.out.println("Produced a body with ID: " + body.getId());
		System.out.println("writing Body into jms...");
		try {
			if(connection == null) {
				connect();
			}
			if(session == null){
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
//			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(QueueConstants.BODYQUEUE);
			MessageProducer msgProducer = session.createProducer(queue);
			//object message
			//notify the GUI first, because we need to make sure that the object is in the table model, before the gui gets a notification to remove it again.
			getListener().onObjectWrittenInQueue(body);
			msgProducer.send(session.createObjectMessage(body));
			
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
			connection.close();
			session.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connection = null;
			session = null;
		}
	}
}
