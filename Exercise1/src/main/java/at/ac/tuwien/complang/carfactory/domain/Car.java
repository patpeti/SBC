package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;

public class Car implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Static Fields
	private static long next_id;
	
	//Fields
    private Color color;
    private long color_pid;
    private boolean isComplete; //Whether assembly is complete
    private long isComplete_pid; //ID of the supervisor
    private long id;  //ID of the Car
    private long pid; //ID of the worker which produced the car
    private Motor motor;
    private Body body;
    private Wheel[] wheels = new Wheel[4];

    //Constructors
    public Car(long pid, Body body, Motor motor, Wheel[] wheels) {
    	next_id++;
    	this.id = Car.next_id;
    	this.pid = pid;
    	this.body = body;
    	this.motor = motor;
    	this.wheels = wheels;
	}
    
    //Getter / Setter
    public Color getColor() {
        return color;
    }
    public void setColor(long pid, Color color) {
    	this.color_pid = pid;
        this.color = color;
    }
    public boolean isComplete() {
        return isComplete;
    }
    public void setComplete(long pid, boolean isComplete) {
    	this.isComplete_pid = pid;
        this.isComplete = isComplete;
    }
    public long getId() {
        return id;
    }
    public boolean hasColor() {
        return color != null;
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
	
	public Object[] getDetails() {
		return new Object[] {id, };
	}

	public long getPainterId() {
		return color_pid;
	}
	
	public long getSupervisorId() {
		return isComplete_pid;
	}
	
	public long getAssemblerId() {
		return pid;
	}
}
