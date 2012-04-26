package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

public class Body implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Static fields
	private static long next_id = 0;
	
	//Fields
	private long id;
	private long pid; //ID of the producer
	
	public Body(long pid) {
		next_id++;
		this.id = Body.next_id;
		this.pid = pid;
	}

	public long getId() {
		return id;
	};
	
	public long getPid() {
		return pid;
	}
}
