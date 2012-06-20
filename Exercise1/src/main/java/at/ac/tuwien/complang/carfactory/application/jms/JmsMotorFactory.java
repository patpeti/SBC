package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsMotorFactory extends JmsAbstractFactory {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;
	private Topic topic;
	private MessageProducer messageProducer;
	private ActiveMQConnectionFactory connectionFactory;

	
	public JmsMotorFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
		connectionFactory = new ActiveMQConnectionFactory();
	}
	
	private void connect() {
		System.out.println("[MotorFactory] Connecting to Queues");
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(QueueConstants.MOTORTOPIC);
			messageProducer = session.createProducer(topic);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	

	public void produce() {
		Motor motor = new Motor(id);
		int randomMotor = (int) (Math.random() * 3);
		motor.setPower(MotorType.values()[randomMotor]);
		double random = Math.random();
		if(random < errorRate) {
			motor.setDefect(true);
		}
		try {
			if(connection == null) {
				connect();
			}
			getListener().onObjectWrittenInQueue(motor);
			//notify the GUI first, because we need to make sure that the object is in the table model, before the GUI gets a notification to remove it again.
			topic = session.createTopic(QueueConstants.MOTORTOPIC);
			messageProducer = session.createProducer(topic);
			messageProducer.send(session.createObjectMessage(motor));
			System.out.println("Produced a motor with id: " + motor.getId());
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
		notifyObservers("MOTOR");
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
