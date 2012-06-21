package at.ac.tuwien.complang.carfactory.application.workers.jms;

import java.awt.Color;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;

import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class JmsAssembler extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic carTopic, paintedBodyTopic, paintedCarTopic, wheelTopic, motorTopic, bodyTopic;
	private MessageConsumer bodyConsumer, wheelConsumer, motorConsumer, paintedBodyConsumer;
	
	public JmsAssembler(long pid) {
		super(pid);
		/**
		 * Workflow:
		 * 1. Connect to all queues/topics
		 * 2. receive a body (from bodyTopic)
		 * 3. receive 4 wheels (from wheelTopic)
		 * 4. receive a motor (from motorTopic)
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 6. save the car object into the right queue (depending on whether it is painted or not)
		 */
	}
	
	
	

	public void startWorkLoop() {
		while(running) {
			//produce some cars
			try {
				Task task = readFirstTask();
				if(task == null){
					normalLoop();
				}else{
					preferredLoop(task);
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
	
	private Task readFirstTask() {
		//TODO read first task from topic
			return null;
	}




	private void preferredLoop(Task task) throws JMSException{
		
			Body body = getOnePreferredBody(task.getColor());
			Motor motor = getOnePreferredMotor(task.getMotortype());
			Wheel[] wheels = getPreferredFourWheels();
			
			if(body == null || motor == null || wheels[0] == null || wheels[1] == null || wheels[2] == null || wheels[3] == null){
				//read next task
				readNextTask(task);
			}else{
				//Everything Found -- creating auto:
				
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
				
				//TODO update Task
				//TODO write task in the queue
			}
	}


	private void readNextTask(Task task) throws JMSException {
		//TODO try to read next Task
		Task t = null;
		
		
		if(t == null){
			normalLoop();
			return;
		}else{
			preferredLoop(t);
		}
		
		
	}


	private void normalLoop() throws JMSException{
		
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
			this.motorTopic = session.createTopic(QueueConstants.MOTORTOPIC);
			this.motorConsumer = session.createDurableSubscriber(this.motorTopic, "motorSubscriber");
			this.wheelTopic = session.createTopic(QueueConstants.WHEELTOPIC);
			this.wheelConsumer = session.createDurableSubscriber(this.wheelTopic, "wheelSubscriber");
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.bodyConsumer = session.createDurableSubscriber(this.bodyTopic, "bodySubscriber");
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			this.paintedBodyConsumer = session.createDurableSubscriber(this.paintedBodyTopic, "paintedBodySubscriber");
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC); //Write only
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC); //Write only
			//TODO connect to TaskTopic
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
	
	private Wheel[] getPreferredFourWheels() throws JMSException {
		ObjectMessage message;
		Wheel[] wheels = new Wheel[4];
		for(int i=0; i<4; i++) {
			message = (ObjectMessage) wheelConsumer.receive();
			if(message == null) return null;
			Wheel wheel = (Wheel) message.getObject();
			System.out.println("Received Wheel: " + wheel.getId());
			wheels[i] = wheel;
		}
		return wheels;
	}




	private Body getOnePreferredBody(Color color) throws JMSException {
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




	private Motor getOnePreferredMotor(MotorType motortype) throws JMSException {
		//TODO add filter
		ObjectMessage message;
		message = (ObjectMessage) motorConsumer.receive(10);
		if(message == null) return null;
		Motor motor = (Motor) message.getObject();
		System.out.println("Received Motor: " + motor.getId());
		return motor;
	}
}
