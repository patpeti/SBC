package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.IllegalStateException;
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
	
	public JmsSupervisor(long pid) {
		super(pid);
	}
	
	@Override
	protected void connectToQueues() {
		//test get Motor
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("supervisor" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			this.paintedCarConsumer = session.createDurableSubscriber(this.paintedCarTopic, "supervisor" + this.pid);
			this.finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startWorkLoop() {
		running = true;
		while(running) {
			try {
				//retrieve a car
				ObjectMessage objectMessage = (ObjectMessage) paintedCarConsumer.receive();
				if(objectMessage == null) throw new IllegalStateException("Connection was closed.");
				Car car = (Car) objectMessage.getObject();
				System.out.println("[Supervisor] Received painted car " + car.getId() + ", check: OK.");
				//set car to complete
				car.setFinished(pid, true);
				//write car to completed queue
				MessageProducer messageProducer = session.createProducer(finishedCarQueue);
				messageProducer.send(session.createObjectMessage(car));
				System.out.println("[Supervisor] Car " + car.getId() + " send to finishedCarQueue.");
			} catch (JMSException e) {
				if(e instanceof IllegalStateException) break;
				e.printStackTrace();
			}
		}
		//disconnect from queue
		disconnect();
		shutdownComplete = true;
		synchronized(runningMutex) {
			runningMutex.notifyAll();
		}
	}
}
