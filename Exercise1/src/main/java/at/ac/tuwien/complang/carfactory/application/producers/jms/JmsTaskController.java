package at.ac.tuwien.complang.carfactory.application.producers.jms;

import java.awt.Color;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.producers.ITaskController;
import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsTaskController implements ITaskController {
	
	//Fields
	private static long next_id;
	
	private Connection connection = null;
	private Session session;
	private ActiveMQConnectionFactory connectionFactory;
	private IQueueListener listener;
	private Topic taskTopic;
	
	public JmsTaskController(IQueueListener listener) {
		this.listener = listener;
		connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			taskTopic = session.createTopic(QueueConstants.TASKQUEUE);
			connection.start();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createTask(MotorType type, Color color, int amount) {
		Task task = new Task(next_id);
		next_id++;
		task.setMotortype(type);
		task.setColor(color);
		task.setAmount(amount);
		listener.onTaskWrittenInQueue(task);
		MessageProducer messageProducer;
		try {
			messageProducer = session.createProducer(taskTopic);
			ObjectMessage message = session.createObjectMessage(task);
			message.setStringProperty("motorType", type.toString().split(" ")[0]);
			message.setStringProperty("color", color.toString());
			messageProducer.send(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnect() {
		try {
			session.close();
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
