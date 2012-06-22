package at.ac.tuwien.complang.carfactory.application.workers.jms;

import java.awt.Color;
import java.util.Enumeration;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
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

	// Fields
	private Session session;
	private Topic carTopic, paintedBodyTopic, paintedCarTopic, wheelTopic,
			motorTopic, bodyTopic, taskTopic;
	private MessageConsumer bodyConsumer, wheelConsumer, motorConsumer,
			paintedBodyConsumer, motorConsumer80, motorConsumer100,
			motorConsumer160, taskConsumer;
	private MessageProducer taskProducer;

	public JmsAssembler(long pid) {
		super(pid);
		/**
		 * Workflow: 1. Connect to all queues/topics 2. receive a body (from
		 * bodyTopic) 3. receive 4 wheels (from wheelTopic) 4. receive a motor
		 * (from motorTopic) 5. assemble them into a car object (create a new
		 * car object and set the parts) 6. save the car object into the right
		 * queue (depending on whether it is painted or not)
		 */
	}

	public void startWorkLoop() {
		while (running) {
			// produce some cars
			try {
				Task task = readFirstTask();
				if (task == null) {
					normalLoop();
				} else {
					preferredLoop(task);
				}

			} catch (JMSException e) {
				if (e instanceof IllegalStateException)
					break;
				e.printStackTrace();
			}
		}
		// disconnect from queue
		disconnect();
		shutdownComplete = true;
		synchronized (runningMutex) {
			runningMutex.notifyAll();
		}
	}

	private Task readFirstTask() {
		try {
			ObjectMessage object =  (ObjectMessage) taskConsumer.receive(1);
			if(object != null) return (Task) object.getObject();
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
			return null;
	}

	private void preferredLoop(Task task) throws JMSException {

		Body body = getOnePreferredBody(task.getColor());
		Motor motor = getOnePreferredMotor(task.getMotortype());
		Wheel[] wheels = getFourWheels();

		if (body == null || motor == null || wheels[0] == null	|| wheels[1] == null || wheels[2] == null || wheels[3] == null) {
			// read next task
//			readNextTask(task);
			normalLoop();
		} else {
			// Everything Found -- creating auto:

			Car car = new Car(pid, body, motor, wheels);
			// write the car to the queue
			MessageProducer messageProducer;
			if (car.hasColor()) {
				messageProducer = session.createProducer(paintedCarTopic);
				messageProducer.send(session.createObjectMessage(car));
				System.out.println("One painted car produced with id: "
						+ car.getId());
			} else {
				messageProducer = session.createProducer(carTopic);
				messageProducer.send(session.createObjectMessage(car));
				System.out.println("One unpainted car produced with id: "
						+ car.getId());
			}

			//  update Task
			//  write task in the queue
			updateTask(task,car);
		}
	}

	private void updateTask(Task task,Car car) throws JMSException {
		Task updatedTask = null;
		while(true){
			ObjectMessage msg = (ObjectMessage) taskConsumer.receive(0);
			if((Task) msg.getObject() == task) {
				updatedTask = (Task) msg.getObject();
				break;
			}
		}
		if(updatedTask == null) throw new IllegalStateException("wtf");
		
		updatedTask.increaseCarAmount(1);
		if(car.hasColor()) {
			updatedTask.increasePaintAmount(1);
			updatedTask.setAmountCompleted(updatedTask.getAmountCompleted()+1);
		}
		
		if(updatedTask.isFinished()) return; //do not send it if it finished
		
		ObjectMessage message = session.createObjectMessage();
		message.setObject(updatedTask);
		message.setStringProperty("motorType", updatedTask.getMotortype().toString().split(" ")[0]);
		message.setStringProperty("color", updatedTask.getColor().toString());
		taskProducer.send(message);
		
	}

//	private void readNextTask(Task task) throws JMSException {
////		QueueBrowser browser = session.createBrowser(taskQueue);
//	
//		Task t = null;
//		for (Enumeration<Task> tasks =  browser.getEnumeration(); tasks.hasMoreElements();){
//			if((Task) tasks.nextElement() == task ) {
//				if(tasks.hasMoreElements()) t = tasks.nextElement();
//			}
//		}
//
//		if (t == null) {
//			normalLoop();
//			return;
//		} else {
//			preferredLoop(t);
//		}

//	}

	private void normalLoop() throws JMSException {

		Body body = getOneBody();
		Motor motor = getOneMotor();
		Wheel[] wheels = getFourWheels();
		Car car = new Car(pid, body, motor, wheels);
		// write the car to the queue
		MessageProducer messageProducer;
		if (car.hasColor()) {
			messageProducer = session.createProducer(paintedCarTopic);
			messageProducer.send(session.createObjectMessage(car));
			System.out.println("One painted car produced with id: "
					+ car.getId());
		} else {
			messageProducer = session.createProducer(carTopic);
			messageProducer.send(session.createObjectMessage(car));
			System.out.println("One unpainted car produced with id: "
					+ car.getId());
		}

	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.complang.carfactory.application.workers.jms.JmsAbstractWorker#connectToQueues()
	 */
	/* (non-Javadoc)
	 * @see at.ac.tuwien.complang.carfactory.application.workers.jms.JmsAbstractWorker#connectToQueues()
	 */
	@Override
	protected void connectToQueues() {
		// Connect to all queues that are required by the assembler
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
		policy.setQueuePrefetch(0); // Do not previously fetch any messages from
									// the queue
		connectionFactory.setPrefetchPolicy(policy);
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("assembler_" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// createQueue connects to a queue if it exists otherwise creates it
			this.motorTopic = session.createTopic(QueueConstants.MOTORTOPIC);
			this.motorConsumer = session.createDurableSubscriber(
					this.motorTopic, "motorSubscriber");
			this.motorConsumer80 = session
					.createDurableSubscriber(this.motorTopic,
							"motorSubscriber80", "motorType='80'", false);
			this.motorConsumer100 = session.createDurableSubscriber(
					this.motorTopic, "motorSubscriber100", "motorType='100'",
					false);
			this.motorConsumer160 = session.createDurableSubscriber(
					this.motorTopic, "motorSubscriber160", "motorType='160'",
					false);
			this.wheelTopic = session.createTopic(QueueConstants.WHEELTOPIC);
			this.wheelConsumer = session.createDurableSubscriber(
					this.wheelTopic, "wheelSubscriber");
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.bodyConsumer = session.createDurableSubscriber(this.bodyTopic,
					"bodySubscriber");
			this.paintedBodyTopic = session
					.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			this.paintedBodyConsumer = session.createDurableSubscriber(
					this.paintedBodyTopic, "paintedBodySubscriber");
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC); // Write
																			// only
			this.paintedCarTopic = session
					.createTopic(QueueConstants.PAINTEDCARTOPIC); // Write only
			this.taskTopic = session.createTopic(QueueConstants.TASKQUEUE);
			this.taskConsumer = session.createDurableSubscriber(taskTopic,"taskSubscriber");
			this.taskProducer = session.createProducer(taskTopic);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private Body getOneBody() throws JMSException {
		ObjectMessage message = null;
		while (message == null) {
			message = (ObjectMessage) bodyConsumer.receive(10);
			if (message == null) {
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
		for (int i = 0; i < 4; i++) {
			message = (ObjectMessage) wheelConsumer.receive();
			if (message == null)
				throw new IllegalStateException("Connection was closed.");
			Wheel wheel = (Wheel) message.getObject();
			System.out.println("Received Wheel: " + wheel.getId());
			wheels[i] = wheel;
		}
		return wheels;
	}

	private Motor getOneMotor() throws JMSException {
		ObjectMessage message;
		message = (ObjectMessage) motorConsumer.receive();
		if (message == null)
			throw new IllegalStateException("Connection was closed.");
		Motor motor = (Motor) message.getObject();
		System.out.println("Received Motor: " + motor.getId());
		return motor;
	}

	private Body getOnePreferredBody(Color color) throws JMSException {
		ObjectMessage message = null;
		while (message == null) {
			message = (ObjectMessage) paintedBodyConsumer.receive(10);
			if (message == null) {
				message = (ObjectMessage) bodyConsumer.receive(10);
			}
		}
		Body body = (Body) message.getObject();
		System.out.println("Received Body: " + body.getId());
		return body;
	}

	private Motor getOnePreferredMotor(MotorType motortype) throws JMSException {
		//add filter
		MessageConsumer consumer = null;

		switch (motortype) {
		case KW_80:
			consumer = this.motorConsumer80;
			break;
		case KW_100:
			consumer = this.motorConsumer100;
			break;
		case KW_160:
			consumer = this.motorConsumer160;
			break;
		default:
			System.out.println("ERROR");
			break;
		}

		ObjectMessage message;
		message = (ObjectMessage) consumer.receive(10);
		if (message == null)
			return null;
		Motor motor = (Motor) message.getObject();
		System.out.println("Received Motor: " + motor.getId());
		return motor;
	}
}
