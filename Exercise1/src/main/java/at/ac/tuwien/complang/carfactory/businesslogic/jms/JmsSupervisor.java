package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Car;

public class JmsSupervisor extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic paintedCarTopic;
	private Queue finishedCarQueue;
	private MessageConsumer paintedCarConsumer;
	
	public JmsSupervisor(long id) {
		super(id);
	}
	
	@Override
	protected void connectToQueues() {
		//test get Motor
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			this.paintedCarConsumer = session.createConsumer(paintedCarTopic);
			this.finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startWorkLoop() {
		while(true) {
			try {
				//retrieve a car
				ObjectMessage objectMessage = (ObjectMessage) paintedCarConsumer.receive();
				Car car = (Car) objectMessage.getObject();
				System.out.println("[Supervisor] Received painted car " + car.getId() + ", check: OK.");
				//set car to complete
				car.setComplete(pid, true);
				//write car to completed queue
				MessageProducer messageProducer = session.createProducer(finishedCarQueue);
				messageProducer.send(session.createObjectMessage(car));
				System.out.println("[Supervisor] Car " + car.getId() + " send to finishedCarQueue.");
				//TODO: notify UI
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
