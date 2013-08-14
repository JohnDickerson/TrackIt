package edu.cmu.cs.eyetrack.test;

public class SingleRunScore {

	private long frameCountOverall;
	private long frameCountOnTarget;
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
	
	public SingleRunScore fakeTheScore() {
		this.frameCountOverall = -1;
		this.frameCountOnTarget = 0;
		this.score = Double.NaN;
		
		return this;
	}
}
