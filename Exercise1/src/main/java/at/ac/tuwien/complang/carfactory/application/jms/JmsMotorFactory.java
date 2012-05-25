package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsMotorFactory extends JmsAbstractFactory {

	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer
	private Connection connection = null;
	private Session session;

	public JmsMotorFactory(long id,  IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	public void produce() {
		Motor motor = new Motor(id);
		System.out.println("Produced a motor with id: " + motor.getId());
		
		System.out.println("writing Motor into jms...");
		ActiveMQConnectionFactory conFac = new ActiveMQConnectionFactory();
		try {
			connection = conFac.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue q = session.createQueue(QueueConstants.MOTORQUEUE);
			MessageProducer msgProducer = session.createProducer(q);
			getListener().onObjectWrittenInQueue(motor);
			//notify the GUI first, because we need to make sure that the object is in the table model, before the gui gets a notification to remove it again.
			msgProducer.send(session.createObjectMessage(motor));
			connection.close();
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
	}
}
