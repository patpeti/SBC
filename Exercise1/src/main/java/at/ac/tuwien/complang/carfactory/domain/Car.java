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
    private boolean isComplete;
    private long id;
    private Motor motor;
    private Body body;
    private Wheel[] wheels = new Wheel[4];
    //Add id of the worker which produced the car;
    
    //Constructors
    public Car() {
    	next_id++;
    	this.id = Car.next_id;
	}
    
    //Getter / Setter
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public boolean isComplete() {
        return isComplete;
    }
    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
    public long getId() {
        return id;
    }
    public boolean hasColor() {
        return color != null;
    }
}
