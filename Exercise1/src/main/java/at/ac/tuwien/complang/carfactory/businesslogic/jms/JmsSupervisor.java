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

public class JmsSupervisor extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic defectTestedCarTopic;
	private Queue finishedCarQueue;
	private MessageConsumer defectTestedCarConsumer;
	
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
			this.defectTestedCarTopic = session.createTopic(QueueConstants.DEFECT_TESTED_TOPIC);
			this.defectTestedCarConsumer = session.createDurableSubscriber(this.defectTestedCarTopic, "defectTestedCars");
			this.finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
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
		if(!car.isComplete() || car.isDefect()) {
			// 1a. If the car has any kind of error, then we need to disassemble it and write the parts back to the right queues
			for(ICarPart part : car.getParts()) {
				if(!part.isDefect()) {
					//MessageProducer producer car = session.createProducer(null);
				}
			}
			// 1b. Write the car to the defect cars queue
		} else {
			// 2. If the car is complete and without defects we can set it to finished and write it to the finished car queue
			//set car state to finished
			car.setFinished(pid, true);
			System.out.println("[Supervisor] Received painted car " + car.getId() + ", check: OK.");
			//write car to completed queue
			MessageProducer messageProducer = session.createProducer(finishedCarQueue);
			messageProducer.send(session.createObjectMessage(car));
			System.out.println("[Supervisor] Car " + car.getId() + " send to finishedCarQueue.");
		}
	}
}
