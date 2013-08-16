package edu.cmu.cs.eyetrack.test;

public class SingleRunScore {

	private long frameCountOverall = 0;
	private long frameCountOnTarget = 0;
	private long fixationPointCount = 0;
	private double score;
	
	public SingleRunScore() {
		
	}

	public long getFrameCountOverall() {
		return frameCountOverall;
	}

	public void setFrameCountOverall(long frameCountOverall) {
		this.frameCountOverall = frameCountOverall;
	}

	public long getFrameCountOnTarget() {
		return frameCountOnTarget;
	}

	public void setFrameCountOnTarget(long frameCountOnTarget) {
		this.frameCountOnTarget = frameCountOnTarget;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public long getFixationPointCount() {
		return fixationPointCount;
	}

	public void setFixationPointCount(long fixationPointCount) {
		this.fixationPointCount = fixationPointCount;
	}

	public SingleRunScore fakeTheScore() {
		this.frameCountOverall = -1;
		this.frameCountOnTarget = 0;
		this.fixationPointCount = -1;
		this.score = Double.NaN;
		
		return this;
	}
}
