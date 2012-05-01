package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.AbstractFactory;
import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.enums.PaintState;

public class Body implements Serializable, ICarPart {
	private static final long serialVersionUID = 1L;

	//Static fields
	//private static long next_id = 0;
	private static final CarPartType type = CarPartType.BODY;
	
	//Fields
	private long id;
	private long pid; //ID of the producer    
    private Color color;
    private long color_pid;
    private PaintState paintState;

	public Body(long pid) {
//		next_id++;
//		this.id = Body.next_id;
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
		return new Object[] {id, this.getType(), pid};
	}

	public CarPartType getType() {
			return this.type;
	}

	public PaintState getPaintState() {
		return paintState;
	}

	public void setPaintState(PaintState paintState) {
		this.paintState = paintState;
	}
	
}
