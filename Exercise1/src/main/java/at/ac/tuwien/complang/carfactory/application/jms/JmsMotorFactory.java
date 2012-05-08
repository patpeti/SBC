package at.ac.tuwien.complang.carfactory.application.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.application.xvsm.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsMotorFactory extends JmsAbstractFactory  implements IProducer {
	
	
	private Connection connection = null;
	private Session session;

	//Fields
	private long id; //The ID of this producer

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
			msgProducer.send(session.createObjectMessage(motor));
			connection.close();
			getListener().onObjectWrittenInQueue(motor);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
