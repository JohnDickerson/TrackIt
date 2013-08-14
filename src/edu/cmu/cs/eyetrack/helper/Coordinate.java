package edu.cmu.cs.eyetrack.helper;

public class Coordinate<E extends Number> {

	private E x;
	private E y;
	
	// Since we use timestamps so much in Track-It, just roll it into the coordinate
	private long timestamp;
	
	public Coordinate(E x, E y, long timestamp) {
		this.x = x;
		this.y = y;
		this.timestamp = timestamp;
	}
	
	public Coordinate(Coordinate<E> coord, long timestamp) {
		this(coord.getX(), coord.getY(), timestamp);
	}
	
	public Coordinate(E x, E y) {
		this(x,y,0);
	}

	public E getX() {
		return x;
	}

	public void setX(E x) {
		this.x = x;
	}

	public E getY() {
		return y;
	}

	public void setY(E y) {
		this.y = y;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public double distance(Coordinate<?> coord) {
		return Math.sqrt( (getX().doubleValue() - coord.getX().doubleValue()) * (getX().doubleValue() - coord.getX().doubleValue()) +
				(getY().doubleValue() - coord.getY().doubleValue()) * (getY().doubleValue() - coord.getY().doubleValue()));
	}
	
	public static double distance(Coordinate<?> c1, Coordinate<?> c2) {
		return c1.distance(c2);
	}
	
	@Override
	public String toString() { 
		if(getTimestamp() > 0) {
			return "<" + getX() + ", " + getY() + ">@" + String.valueOf(getTimestamp());
		} else {
			return "<" + getX() + ", " + getY() + ">";
		}
	}
	
	@Override
	public boolean equals(Object o) { 
		 if((o instanceof Coordinate<?>)) {
			   
			Coordinate<?> c = (Coordinate<?>) o;
			return getX().equals(c.getX()) && getY().equals(c.getY()) && getTimestamp()==c.getTimestamp();
		 }
		 return false;
	}
	
	@Override
	public int hashCode() {
		return 41 * ((43 * (47 + getX().hashCode()) + getY().hashCode())) + (int)getTimestamp();
	}
}
