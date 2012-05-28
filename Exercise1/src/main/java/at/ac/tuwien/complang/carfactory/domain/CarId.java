package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

public class CarId implements Serializable {


	private static final long serialVersionUID = -5846685097600422181L;
	private long carID;
	

	public CarId() {
		super();
	}

	public long getCarID() {
		return carID;
	}

	public void setCarID(long carID) {
		this.carID = carID;
	}
	
}
