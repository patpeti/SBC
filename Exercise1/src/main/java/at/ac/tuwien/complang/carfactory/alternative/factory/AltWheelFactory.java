package at.ac.tuwien.complang.carfactory.alternative.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.alternative.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.application.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class AltWheelFactory extends AltAbstractFactory implements IProducer  {

	
	private Connection connection = null;
	private Session session;
	//Fields
	private long id; //The ID of this producer

	public AltWheelFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	public void produce() {
		Wheel wheel = new Wheel(id);
		System.out.println("Produced a wheel with ID: " + wheel.getId());
		
		System.out.println("writing wheel into jms...");
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(QueueConstants.WHEELQUEUE);
			MessageProducer msgProducer = session.createProducer(queue);
			msgProducer.send(session.createObjectMessage(wheel));
			connection.close();
			getListener().onObjectWrittenInQueue(wheel); //notify GUI
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
