package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class JmsAssembler extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Queue wheelQueue, motorQueue;
	private Topic carTopic, bodyTopic;
	private MessageConsumer bodyConsumer, wheelConsumer, motorConsumer, carConsumer;
	
	public JmsAssembler(long pid) {
		super(pid);
		/**
		 * TODO:
		 * 1. Connect to all queues/topics
		 * 2. load a body (from bodyTopic)
		 * 3. load 4 wheels (from wheelQueue)
		 * 4. load a motor (from motorQueue)
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 7. mark the body, wheels and motor as already used 
		 *    (or alternatively remove them from the space)
		 *    FIXME: decide if objects should remain in the space -->all infos still available inside the car objects no need to have the original objects in the space
		 * 6. save the car object back into the space
		 */
	}

	public void startAssemblyLoop() {
		while(true) {
			//produce some cars
			Body body = getOneBody();
			Motor motor = getOneMotor();
			Wheel[] wheels = getFourWheels();
			Car car = new Car(pid, body, motor, wheels);
			//write the car to the queue
			MessageProducer messageProducer;
			try {
				messageProducer = session.createProducer(carTopic);
				messageProducer.send(session.createObjectMessage(car));
				System.out.println("One car produced with id: " + car.getId());
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//disconnect from queue
		//disconnect(); //TODO: implement the conditions for which the assembler terminates
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
			this.motorQueue = session.createQueue(QueueConstants.MOTORQUEUE);
			this.motorConsumer = session.createConsumer(motorQueue);
			this.wheelQueue = session.createQueue(QueueConstants.WHEELQUEUE);
			this.wheelConsumer = session.createConsumer(wheelQueue);
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.bodyConsumer = session.createConsumer(bodyTopic);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			this.carConsumer = session.createConsumer(carTopic);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private Body getOneBody() {
		ObjectMessage message;
		try {
			message = (ObjectMessage) bodyConsumer.receive();
			Body body = (Body) message.getObject();
			System.out.println("Received Body: " + body.getId());
			return body;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Wheel[] getFourWheels() {
		ObjectMessage message;
		Wheel[] wheels = new Wheel[4];
		try {
			for(int i=0; i<4; i++) {
				message = (ObjectMessage) wheelConsumer.receive();
				Wheel wheel = (Wheel) message.getObject();
				System.out.println("Received Wheel: " + wheel.getId());
				wheels[i] = wheel;
			}
			return wheels;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Motor getOneMotor() {
		ObjectMessage message;
		try {
			message = (ObjectMessage) motorConsumer.receive();
			Motor motor = (Motor) message.getObject();
			System.out.println("Received Motor: " + motor.getId());
			return motor;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
