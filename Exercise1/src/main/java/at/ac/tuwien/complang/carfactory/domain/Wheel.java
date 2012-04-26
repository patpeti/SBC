package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

public class Wheel implements Serializable, ICarPart {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Static fields
	private static long next_id = 0;
	
	//Fields
	private long id;
	private long pid;
	
	public Wheel(long pid) {
		next_id++;
		this.id = Wheel.next_id;
		this.pid = pid;
	}

	public long getId() {
		return id;
	};
	
	public long getPid() {
		return pid;
	}
}
