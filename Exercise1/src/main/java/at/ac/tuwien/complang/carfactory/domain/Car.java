package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Car implements Serializable, ICarPart {
	private static final long serialVersionUID = 1L;
	private static final CarPartType type = CarPartType.CAR;

	//Static Fields
	//private static long next_id;
	
	//Fields
    private boolean isComplete; //Whether assembly is complete
    private long isComplete_pid; //ID of the supervisor
    private long id;  //ID of the Car
    private long pid; //ID of the worker which produced the car
    private Motor motor;
    private Body body;
    private PaintState paintState;
    private Wheel[] wheels = new Wheel[4];

    //Constructors
    public Car(long pid, Body body, Motor motor, Wheel[] wheels) {
    	this.paintState = PaintState.UNPAINTED;
    	this.id = AbstractFactory.carPartId;
		AbstractFactory.carPartId++;
    	this.pid = pid;
    	this.body = body;
    	this.motor = motor;
    	this.wheels = wheels;
	}
        
    //Getter / Setter
    public boolean isComplete() {
        return isComplete;
    }
    public void setComplete(long pid, boolean isComplete) {
    	this.isComplete_pid = pid;
        this.isComplete = isComplete;
    }
    
    public Color getColor() {
        return this.body.getColor();
    }
    public void setColor(long pid, Color color) {
    	this.paintState = PaintState.PAINTED;
    	this.body.setColor(pid, color);
    }

    public boolean hasColor() {
        return body.getColor() != null;
    }
	
	public long getPainterId() {
		return body.getPainterId();
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
		return isComplete_pid;
	}
	
	public long getAssemblerId() {
		return pid;
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
		return new Object[] {id, pid,
				body.getId(), body.getPid(),
				colorString, body.getPainterId(),
				motor.getId(), motor.getPid(),
				wheels[0].getId(),
				wheels[0].getPid(),
				wheels[1].getId(),
				wheels[1].getPid(),
				wheels[2].getId(),
				wheels[2].getPid(),
				wheels[3].getId(),
				wheels[3].getPid(),
			};
	}

	public CarPartType getType() {
		return Car.type;
	}

	public PaintState getPaintState() {
		return this.body.getPaintState();
	}

	public void setPaintState(PaintState paintState) {
		this.paintState = paintState;
	}
}
