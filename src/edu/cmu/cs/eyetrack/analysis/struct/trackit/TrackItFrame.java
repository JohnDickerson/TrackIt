package edu.cmu.cs.eyetrack.analysis.struct.trackit;

import edu.cmu.cs.eyetrack.analysis.struct.Frame;
import edu.cmu.cs.eyetrack.helper.Coordinate;

public class TrackItFrame extends Frame {

	private Coordinate<Double> position;
	private long timestamp;

	public TrackItFrame(Coordinate<Double> position, long timestamp) {
		this.position = position;
		this.timestamp = timestamp;
	}

	@Override
	public Coordinate<Double> getPosition() {
		return position;
	}

	public void setPosition(Coordinate<Double> position) {
		this.position = position;
	}
	
	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "TrackItFrame [position=" + position + ", timestamp="
				+ timestamp + "]";
	}

}
