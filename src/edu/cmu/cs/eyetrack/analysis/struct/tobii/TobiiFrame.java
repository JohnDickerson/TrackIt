package edu.cmu.cs.eyetrack.analysis.struct.tobii;

import edu.cmu.cs.eyetrack.analysis.struct.Frame;
import edu.cmu.cs.eyetrack.helper.Coordinate;

public class TobiiFrame extends Frame {

	private long timestamp;
	private Coordinate<Double> gazeLeftPt;
	private int validityLeft;
	private Coordinate<Double> gazeRightPt;
	private int validityRight;
	private Coordinate<Double> gazePt;
	
	private Integer fixationIdx;
	private Coordinate<Double> fixationPt;
	private Integer fixationDuration;
	
	public TobiiFrame() {
		
	}

	@Override
	public Coordinate<Double> getPosition() {
		return gazePt;
	}
	
	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Coordinate<Double> getGazeLeftPt() {
		return gazeLeftPt;
	}

	public void setGazeLeftPt(Coordinate<Double> gazeLeftPt) {
		this.gazeLeftPt = gazeLeftPt;
	}

	public Coordinate<Double> getGazeRightPt() {
		return gazeRightPt;
	}

	public void setGazeRightPt(Coordinate<Double> gazeRightPt) {
		this.gazeRightPt = gazeRightPt;
	}

	public Coordinate<Double> getFixationPt() {
		return fixationPt;
	}

	public void setFixationPt(Coordinate<Double> fixationPt) {
		this.fixationPt = fixationPt;
	}

	public int getValidityLeft() {
		return validityLeft;
	}

	public void setValidityLeft(int validityLeft) {
		this.validityLeft = validityLeft;
	}

	public int getValidityRight() {
		return validityRight;
	}

	public void setValidityRight(int validityRight) {
		this.validityRight = validityRight;
	}

	public Coordinate<Double> getGazePt() {
		return gazePt;
	}

	public void setGazePt(Coordinate<Double> gazePt) {
		this.gazePt = gazePt;
	}

	public Integer getFixationIndex() {
		return fixationIdx;
	}

	public void setFixationIndex(Integer fixationIdx) {
		this.fixationIdx = fixationIdx;
	}

	public Integer getFixationDuration() {
		return fixationDuration;
	}

	public void setFixationDuration(Integer fixationDuration) {
		this.fixationDuration = fixationDuration;
	}

	@Override
	public String toString() {
		return "TobiiFrame [timestamp=" + timestamp + ", gazeLeftPt="
				+ gazeLeftPt + ", validityLeft=" + validityLeft
				+ ", gazeRightPt=" + gazeRightPt + ", validityRight="
				+ validityRight + ", fixationPt=" + fixationPt + ", gazePt="
				+ gazePt + "]";
	}

}
