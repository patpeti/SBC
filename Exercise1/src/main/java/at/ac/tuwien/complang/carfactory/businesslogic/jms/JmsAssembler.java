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
import org.apache.activemq.ActiveMQPrefetchPolicy;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class JmsAssembler extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Queue wheelQueue, motorQueue, bodyQueue;
	private Topic carTopic, paintedBodyTopic, paintedCarTopic;
	private MessageConsumer bodyConsumer, wheelConsumer, motorConsumer, paintedBodyConsumer;
	
	public JmsAssembler(long pid) {
		super(pid);
		/**
		 * Workflow:
		 * 1. Connect to all queues/topics
		 * 2. receive a body (from bodyTopic)
		 * 3. receive 4 wheels (from wheelQueue)
		 * 4. receive a motor (from motorQueue)
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 6. save the car object into the right queue (depending on whether it is painted or not)
		 */
	}

	public void startWorkLoop() {
		running = true;
		while(running) {
			//produce some cars
			try {
				Body body = getOneBody();
				Motor motor = getOneMotor();
				Wheel[] wheels = getFourWheels();
				Car car = new Car(pid, body, motor, wheels);
				//write the car to the queue
				MessageProducer messageProducer;
				if(car.hasColor()) {
					messageProducer = session.createProducer(paintedCarTopic);
					messageProducer.send(session.createObjectMessage(car));
					System.out.println("One painted car produced with id: " + car.getId());
				} else {
					messageProducer = session.createProducer(carTopic);
					messageProducer.send(session.createObjectMessage(car));
					System.out.println("One unpainted car produced with id: " + car.getId());
				}
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

	@Override
	protected void connectToQueues() {
		//Connect to all queues that are required by the assembler
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
		policy.setQueuePrefetch(0); //Do not previously fetch any messages from the queue
		connectionFactory.setPrefetchPolicy(policy);
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("assembler_" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.motorQueue = session.createQueue(QueueConstants.MOTORQUEUE);
			this.motorConsumer = session.createConsumer(this.motorQueue);
			this.wheelQueue = session.createQueue(QueueConstants.WHEELQUEUE);
			this.wheelConsumer = session.createConsumer(this.wheelQueue);
			this.bodyQueue = session.createQueue(QueueConstants.BODYQUEUE);
			this.bodyConsumer = session.createConsumer(this.bodyQueue);
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			this.paintedBodyConsumer = session.createDurableSubscriber(this.paintedBodyTopic, "paintedBodySubscriber");
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC); //Write only
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC); //Write only
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private Body getOneBody() throws JMSException {
		ObjectMessage message = null;
		while(message == null) {
			message = (ObjectMessage) bodyConsumer.receive(10);
			if(message == null) {
				message = (ObjectMessage) paintedBodyConsumer.receive(10);
			}
		}
		Body body = (Body) message.getObject();
		System.out.println("Received Body: " + body.getId());
		return body;
	}

	private Wheel[] getFourWheels() throws JMSException {
		ObjectMessage message;
		Wheel[] wheels = new Wheel[4];
		for(int i=0; i<4; i++) {
			message = (ObjectMessage) wheelConsumer.receive();
			if(message == null) throw new IllegalStateException("Connection was closed.");
			Wheel wheel = (Wheel) message.getObject();
			System.out.println("Received Wheel: " + wheel.getId());
			wheels[i] = wheel;
		}
		return wheels;
	}
	
	private Motor getOneMotor() throws JMSException {
		ObjectMessage message;
		message = (ObjectMessage) motorConsumer.receive();
		if(message == null) throw new IllegalStateException("Connection was closed.");
		Motor motor = (Motor) message.getObject();
		System.out.println("Received Motor: " + motor.getId());
		return motor;
	}
}
