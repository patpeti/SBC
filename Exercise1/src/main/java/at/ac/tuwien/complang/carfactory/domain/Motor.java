package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

public class Motor implements Serializable, ICarPart {
	private static final long serialVersionUID = 1L;

	//Static fields
	private static long next_id = 0;
	
	//Fields
	private long id;
	private long pid; //ID of the producer
	
	public Motor(long pid) {
		next_id++;
		this.id = Motor.next_id;
		this.pid = pid;
	}

	public long getId() {
		return id;
	};
	
	public long getPid() {
		return pid;
	}
	
}
