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
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class JmsSupervisor extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic defectTestedCarTopic, bodyTopic, paintedBodyTopic, motorTopic, wheelTopic;
	private Queue finishedCarQueue, defectCarQueue;
	private MessageConsumer defectTestedCarConsumer;
	private MessageProducer bodyProducer, paintedBodyProducer, motorProducer, wheelProducer, finishedCarProducer, defectCarProducer;
	
	public JmsSupervisor(long pid) {
		super(pid);
	}
	
	@Override
	protected void connectToQueues() {
		//test get Motor
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("supervisor_" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			//ICarPart Topics
			bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			bodyProducer = session.createProducer(this.bodyTopic);
			paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			paintedBodyProducer = session.createProducer(this.paintedBodyTopic);
			motorTopic = session.createTopic(QueueConstants.MOTORTOPIC);
			motorProducer = session.createProducer(this.motorTopic);
			wheelTopic = session.createTopic(QueueConstants.WHEELTOPIC);
			wheelProducer = session.createProducer(this.wheelTopic);
			//Car Topics/Queues
			defectTestedCarTopic = session.createTopic(QueueConstants.DEFECT_TESTED_TOPIC);
			defectTestedCarConsumer = session.createDurableSubscriber(this.defectTestedCarTopic, "defectTestedCars");
			finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
			finishedCarProducer = session.createProducer(finishedCarQueue);
			defectCarQueue = session.createQueue(QueueConstants.DEFECTCARQUEUE);
			defectCarProducer = session.createProducer(defectCarQueue);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startWorkLoop() {
		while(running) {
			try {
				//retrieve a car
				ObjectMessage objectMessage = (ObjectMessage) defectTestedCarConsumer.receive();
				if(objectMessage == null) throw new IllegalStateException("Connection was closed.");
				Car car = (Car) objectMessage.getObject();
				handleCar(car);
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

	private void handleCar(Car car) throws JMSException {
		boolean isTested = (car.getCompletenessTesterId() != -1 || car.getDefectTesterId() != -1);
		boolean hasProblem = (!car.isComplete() || car.isDefect());
		if(isTested && hasProblem) {
			// 1a. If the car has any kind of error, then we need to disassemble it and write the parts back to the right queues
			for(ICarPart part : car.getParts()) {
				if(!part.isDefect()) {
					if(part instanceof Body) {
						Body body = (Body) part;
						if(body.hasColor()) {
							paintedBodyProducer.send(session.createObjectMessage(part));
						} else {
							bodyProducer.send(session.createObjectMessage(part));
						}
					} else if(part instanceof Motor) {
						motorProducer.send(session.createObjectMessage(part));
					} else if(part instanceof Wheel) {
						wheelProducer.send(session.createObjectMessage(part));
					}
				}
			}
			//set car state to not finished
			car.setFinished(pid, true);
			// 1b. Write the car to the defect cars queue
			defectCarProducer.send(session.createObjectMessage(car));
			System.out.println("[Supervisor] Car " + car.getId() + " has defects and was disassembled. Defect remainders are send to the defectCarQueue.");
		} else {
			// 2. If the car is complete and without defects we can set it to finished and write it to the finished car queue
			//set car state to finished
			car.setFinished(pid, true);
			//write car to completed queue
			finishedCarProducer.send(session.createObjectMessage(car));
			System.out.println("[Supervisor] Car " + car.getId() + " is complete and was send to finishedCarQueue.");
		}
	}
}
