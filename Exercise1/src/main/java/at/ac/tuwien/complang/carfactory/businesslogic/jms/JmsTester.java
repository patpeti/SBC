package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.TesterType;

/**
 * This tester class contains the logic for both kinds of testers.
 * @author Sebastian Geiger
 *
 */
public class JmsTester extends JmsAbstractWorker {
	private TesterType type;
	
	private Topic defectTestedCarTopic, completenessTestedCarTopic, paintedCarTopic;
	private MessageConsumer completenessTestedConsumer, paintedCarConsumer;
	private Session session;

	public JmsTester(long pid, TesterType type) {
		super(pid);
		this.type = type;
	}

	private void doDefectTest() {
		//1. Get a car from the completeness tested topic
		Car car = null;
		try {
			ObjectMessage message = (ObjectMessage) completenessTestedConsumer.receive();
			car = (Car) message.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//2. Test the car
		boolean testOk = false;
		if(car != null) {
			testOk = testDefect(car);
			car.setDefect(this.pid, testOk);
		}
		//3. Write it to the topic for completnessTestedCars
		ObjectMessage message;
		try {
			message = session.createObjectMessage();
			message.setObject(car);
			MessageProducer producer = session.createProducer(this.defectTestedCarTopic);
			producer.send(message);
			System.out.println("[Defect Tester] Tested car " + car.getId() + " status " + (testOk ? "OK" : "DEFECT"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void doCompletenessTest() {
		//1. Get a car from the paintedCarTopic
		Car car = null;
		try {
			ObjectMessage message = (ObjectMessage) paintedCarConsumer.receive();
			car = (Car) message.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//2. Test the car
		boolean testOk = false;
		if(car != null) {
			testOk = testCompleteness(car);
			car.setComplete(this.pid, testOk);
		}
		//3. Write it to the topic for completnessTestedCars
		ObjectMessage message;
		try {
			message = session.createObjectMessage();
			message.setObject(car);
			MessageProducer producer = session.createProducer(this.completenessTestedCarTopic);
			producer.send(message);
			System.out.println("[Completeness Tester] Tested car " + car.getId() + " status " + (testOk ? "OK" : "UNCOMPLETE"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void connectToQueues() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
		policy.setQueuePrefetch(0);
		connectionFactory.setPrefetchPolicy(policy);
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("tester" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			//Read by the completeness tester
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			this.paintedCarConsumer = session.createDurableSubscriber(this.paintedCarTopic, "paintedCars");
			//Read by the defect Tester, Written to by the completeness tester
			this.completenessTestedCarTopic = session.createTopic(QueueConstants.COMPLETENESS_TESTED_TOPIC);
			this.completenessTestedConsumer = session.createDurableSubscriber(this.completenessTestedCarTopic, "completenessTester");
			//Written to by the defect tester
			this.defectTestedCarTopic = session.createTopic(QueueConstants.DEFECT_TESTED_TOPIC);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	void startWorkLoop() {
		if(type == TesterType.COMPLETETESTER) {
			// Test for completeness of the car (e.g. car has all parts and is painted)
			while(true){
				doCompletenessTest();
			}
		} else if(type == TesterType.DEFECTTESTER) {
			// Test for defects in the build in components
			while(true){
				doDefectTest();
			}
		} else {
			System.err.println("The specificed test type has not been implemented by this tester. Program will exit now.");
			System.exit(1);
		}
	}
	
	private boolean testCompleteness(Car c) {
		//all parts are set and body is painted
		boolean testOk = true;
		if(c.getBody() == null) testOk = false;
		else if(c.getBody().getColor() == null) testOk = false;
		if(c.getMotor() == null) testOk = false;
		if(c.getWheels()[0] == null) testOk = false;
		if(c.getWheels()[1] == null) testOk = false;
		if(c.getWheels()[2] == null) testOk = false;
		if(c.getWheels()[3] == null) testOk = false;
		
		return testOk;
	}

	private boolean testDefect(Car car) {
		//none of the parts is defected
		boolean testOk = true;
		if(car.getBody().isDefect()) testOk = false;
		if(car.getMotor().isDefect()) testOk = false;
		if(car.getWheels()[0].isDefect()) testOk = false;
		if(car.getWheels()[1].isDefect()) testOk = false;
		if(car.getWheels()[2].isDefect()) testOk = false;
		if(car.getWheels()[3].isDefect()) testOk = false;
		
		return testOk;
	}
}
