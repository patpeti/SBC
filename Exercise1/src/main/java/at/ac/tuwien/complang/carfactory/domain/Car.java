package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Car implements Serializable, ICarPart {

	//Static Fields
	private static final long serialVersionUID = 1L;
	private static final CarPartType type = CarPartType.CAR;

	//Fields
	private long id;  //ID of the Car

	private long isComplete_pid = -1; //ID of the completeness tester
	private long isDefectTested_pid = -1; //ID of the defect tester
	private long isFinished_pid = -1; //ID of the supervisor
	private long pid; //ID of the assembler which produced the car
	private long taskId; //ID of the task for which this car was produced
	private Motor motor;
	private Body body;
	private Wheel[] wheels = new Wheel[4];

	private boolean isComplete = false; //Set by the completeness tester. True if all parts are there and the body is painted.
	private boolean defect = false; //If any of the parts has a defect
	private boolean isFinished = false; //Set by the supervisor to indicate that the car is finished and can be delivered

	//Constructors
	public Car(long pid, Body body, Motor motor, Wheel[] wheels) {
		this.id = AbstractFactory.carPartId;
		AbstractFactory.carPartId++;
		this.pid = pid;
		this.body = body;
		this.motor = motor;
		this.wheels = wheels;
	}

	//Getter / Setter
	/**
	 * The color is derived from the body
	 */
	public Color getColor() {
		if(body == null) return null;
		return this.body.getColor();
	}
	
	/**
	 * The color is derived from the body
	 */
	public void setColor(long pid, Color color) {
		this.body.setColor(pid, color);
	}

	/**
	 * The hasColor field is derived from the body
	 * @return
	 */
	public boolean hasColor() {
		if(body == null) return false;
		return body.getColor() != null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Motor getMotor() {
		return motor;
	}

	public Body getBody() {
		return body;
	}

	public Wheel[] getWheels() {
		return wheels;
	}

	public long getSupervisorId() {
		return isFinished_pid;
	}

	public long getAssemblerId() {
		return pid;
	}
	
	public long getCompletenessTesterId() {
		return isComplete_pid;
	}
	
	public long getDefectTesterId() {
		return isDefectTested_pid;
	}
	
	/**
	 * Derived from the body
	 */
	public long getPainterId() {
		return body.getPainterId();
	}
	

	public long getPid() {
		return getAssemblerId();
	}

	public Object[] getObjectData() {
		String colorString;
		Color color = getColor();
		if(color == null) {
			colorString = "NONE";
		} else if(color.equals(Color.RED)) {
			colorString = "RED";
		} else if(color.equals(Color.BLUE)) {
			colorString = "BLUE";
		} else if(color.equals(Color.GREEN)) {
			colorString = "GREEN";
		} else {
			colorString = String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
		}
		return new Object[] {
				id,
				pid, //Assembler
				this.isComplete_pid, //Completeness Tester
				this.isDefectTested_pid, //Defect Tester
				this.isFinished_pid, //Supervisor
				body.getId(), body.getPid(),
				colorString, body.getPainterId(), //Painter
				motor.getId(), motor.getPid(),
				wheels[0].getPid(),
				wheels[0].getId(),
				//wheels[1].getPid(), //Comment out the other three ids, because in our setup there is only one wheel producer.				
				wheels[1].getId(),
				//wheels[2].getPid(),
				wheels[2].getId(),
				//wheels[3].getPid(),
				wheels[3].getId(),
		};
	}

	public CarPartType getType() {
		return Car.type;
	}

	public PaintState getPaintState() {
		return this.body.getPaintState();
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	//For the defect tester
	public boolean isDefect() {
		return defect;
	}

	public void setDefect(long pid, boolean defect) {
		this.isDefectTested_pid = pid;
		this.defect = defect;
	}
	
	//For the completeness tester
	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(long pid, boolean isComplete) {
		this.isComplete_pid = pid;
		this.isComplete = isComplete;
	}

	//For the supervisor
	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(long pid, boolean isFinished) {
		this.isFinished_pid = pid;
		this.isFinished = isFinished;
	}
	
	public List<ICarPart> getParts() {
		List<ICarPart> parts = new ArrayList<ICarPart>();
		if(body != null) parts.add(body);
		if(motor != null) parts.add(motor);
		for(Wheel wheel : wheels) {
			if(wheel != null) parts.add(wheel);
		}
		return parts;
	}
}
