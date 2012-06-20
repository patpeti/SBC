package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Body implements Serializable, ICarPart {

	private static final long serialVersionUID = 217392531648766282L;

	//Static fields
	private static final CarPartType type = CarPartType.BODY;
	
	//Fields
	private long id;
	private long pid; //ID of the producer    
    private Color color;
    private long color_pid;
    private PaintState paintState;
    private boolean isDefect;

	public Body(long pid) {
		this.paintState = PaintState.UNPAINTED;
		this.id = AbstractFactory.carPartId;
		AbstractFactory.carPartId++;
		this.pid = pid;
	}
    
    //Getter/Setter
    public Color getColor() {
        return color;
    }
    public void setColor(long pid, Color color) {
    	this.color_pid = pid;
        this.color = color;
    }

    public boolean hasColor() {
        return color != null;
    }
	
	public long getPainterId() {
		return color_pid;
	}

	public long getId() {
		return id;
	};
	
	public long getPid() {
		return pid;
	}

	public Object[] getObjectData() {
		String note = (color != null) ? "Painted" : "No Color";
		note += isDefect ? ", Defect" : "";
		return new Object[] {id, this.getType(), pid, note};
	}

	public CarPartType getType() {
			return Body.type;
	}

	public PaintState getPaintState() {
		return paintState;
	}

	public void setPaintState(PaintState paintState) {
		this.paintState = paintState;
	}

	@Override
	public boolean isDefect() {
		return isDefect;
	}

	public void setDefect(boolean isDefect) {
		this.isDefect = isDefect;
	}
	
	@Override
	public String toString() {
		return this.id + "";
	}
}
