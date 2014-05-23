package edu.cmu.cs.eyetrack.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.helper.Coordinate;
import edu.cmu.cs.eyetrack.io.CSVWritable;

public class Trial implements CSVWritable{

	private int id;
	private long length;
	private long startTime;
	private long frameCount;
	private Stimulus target = null;
	private Coordinate<Double> targetFinalPos;
	private Coordinate<Integer> targetFinalGridPos;
	private Coordinate<Double> userClickPos;
	private Coordinate<Integer> userClickGridPos;
	private boolean gridClickCorrect = false;
	private long gridClickRT;
	private Stimulus lineupStimulus = null;
	private boolean lineupClickCorrect = false;
	private long lineupClickRT;
	
	private Set<Stimulus> stimList;
	private Map<Stimulus, List<Coordinate<Double>>> positionList;
	
	public Trial() {
		this(0);
	}

	public Trial(int id) {
		this.id = id;
		stimList = new HashSet<Stimulus>();
		positionList = new HashMap<Stimulus, List<Coordinate<Double>>>();
		frameCount = 0;
		
		targetFinalPos = new Coordinate<Double>(-1.0, -1.0);
		targetFinalGridPos = new Coordinate<Integer>(-1, -1);
		userClickPos = new Coordinate<Double>(-1.0, -1.0);
		userClickGridPos = new Coordinate<Integer>(-1, -1);
	}
	
	//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
	public String[] getCSVHeader() {
		return new String[] {
			"BEGIN New Trial"
		};
	}

	//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
	public List<String[]> getCSVData() {
		
		List<String[]> data = new ArrayList<String[]>();
		
		data.add(new String[] {
				"id",
				"length",
				"startTime",
				"target",
				"targetX",
				"targetY",
				"targetGridX",
				"targetGridY",
				"userX",
				"userY",
				"userGridX",
				"userGridY",
				"gridClickCorrect",
				"gridClickRT",
				"lineupStimulus",
				"lineupClickCorrect",
				"lineupClickRT",
			});
		
		// Fill in the basic part 
		data.add(new String[] {
			String.valueOf(id),
			String.valueOf(length),
			String.valueOf(startTime),
			String.valueOf(target),   // target might be null early in the Trial, but who cares.
			String.valueOf(targetFinalPos.getX()),
			String.valueOf(targetFinalPos.getY()),
			String.valueOf(targetFinalGridPos.getX()),
			String.valueOf(targetFinalGridPos.getY()),
			String.valueOf(userClickPos.getX()),
			String.valueOf(userClickPos.getY()),
			String.valueOf(userClickGridPos.getX()),
			String.valueOf(userClickGridPos.getY()),
			String.valueOf(gridClickCorrect),
			String.valueOf(gridClickRT),
			String.valueOf(lineupStimulus),
			String.valueOf(lineupClickCorrect),
			String.valueOf(lineupClickRT),
		});
		
		// Data for each distractor/target location, for each frame
		// Each target has its own column
		String[] isATargetHeader = new String[1 + stimList.size() * 2];
		String[] targetTrackingHeader = new String[1 + stimList.size() * 2];
		int colIdx=0;
		targetTrackingHeader[colIdx++] = "Frame Timestamp";
		for(Stimulus stimulus : stimList) {
			isATargetHeader[colIdx] = stimulus == target ? "target" : "";
			targetTrackingHeader[colIdx++] = stimulus.toString() + "_x";
			isATargetHeader[colIdx] = stimulus == target ? "target" : "";
			targetTrackingHeader[colIdx++] = stimulus.toString() + "_y";
		}
		data.add(isATargetHeader);
		data.add(targetTrackingHeader);
		
		// Each frame has its own row
		int rowIdx=0;
		while(true) {
			
			if(rowIdx >= frameCount) {
				break;
			}
			
			String[] frame = new String[1 + stimList.size() * 2];
			colIdx=0;
			frame[colIdx++] = String.valueOf(
					positionList.get(stimList.iterator().next()).get(rowIdx).getTimestamp() -
					startTime );
			for( Stimulus stimulus : stimList ) {
				frame[colIdx++] = String.valueOf(positionList.get(stimulus).get(rowIdx).getX());
				frame[colIdx++] = String.valueOf(positionList.get(stimulus).get(rowIdx).getY());
			}
			data.add(frame);
			
			rowIdx++;
		}
		
		data.add(new String[] {
			"END New Trial"
		});
		
		return data;
	}

	public void pushStims(Collection<Stimulus> stimuli) {
		for(Stimulus stimulus : stimuli) {
			pushStim(stimulus);
		}
	}
	
	public boolean pushStim(Stimulus stimulus) {
		if(positionList.get(stimulus) != null) {
			return false;
		}
		stimList.add(stimulus);
		positionList.put(stimulus, new ArrayList<Coordinate<Double>>());
		return true;
	}
	
	public void pushPos(Stimulus stimulus, Coordinate<Double> center) {
		// Explicitly do not check for null?  Maybe change later
		positionList.get(stimulus).add(center);
	}
	
	public void incFrameCount() {
		frameCount++;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(long frameCount) {
		this.frameCount = frameCount;
	}

	public Stimulus getTarget() {
		return target;
	}

	public void setTarget(Stimulus target) {
		this.target = target;
	}

	public Coordinate<Double> getTargetFinalPos() {
		return targetFinalPos;
	}

	public void setTargetFinalPos(Coordinate<Double> targetFinalPos) {
		this.targetFinalPos = targetFinalPos;
	}

	public Coordinate<Integer> getTargetFinalGridPos() {
		return targetFinalGridPos;
	}

	public void setTargetFinalGridPos(Coordinate<Integer> targetFinalGridPos) {
		this.targetFinalGridPos = targetFinalGridPos;
	}

	public Coordinate<Double> getUserClickPos() {
		return userClickPos;
	}

	public void setUserClickPos(Coordinate<Double> userClickPos) {
		this.userClickPos = userClickPos;
	}

	public Coordinate<Integer> getUserClickGridPos() {
		return userClickGridPos;
	}

	public void setUserClickGridPos(Coordinate<Integer> userClickGridPos) {
		this.userClickGridPos = userClickGridPos;
	}

	public boolean isGridClickCorrect() {
		return gridClickCorrect;
	}

	public void setGridClickCorrect(boolean gridClickCorrect) {
		this.gridClickCorrect = gridClickCorrect;
	}

	public Set<Stimulus> getStims() {
		return stimList;
	}

	public void setStims(Set<Stimulus> stimList) {
		this.stimList = stimList;
	}

	public Map<Stimulus, List<Coordinate<Double>>> getPositionList() {
		return positionList;
	}

	public void setPositionList(
			HashMap<Stimulus, List<Coordinate<Double>>> positionList) {
		this.positionList = positionList;
	}

	public Stimulus getLineupStimulus() {
		return lineupStimulus;
	}

	public void setLineupStimulus(Stimulus lineupStimulus) {
		this.lineupStimulus = lineupStimulus;
	}

	public boolean isLineupClickCorrect() {
		return lineupClickCorrect;
	}

	public void setLineupClickCorrect(boolean lineupClickCorrect) {
		this.lineupClickCorrect = lineupClickCorrect;
	}

	public long getGridClickRT() {
		return gridClickRT;
	}

	public void setGridClickRT(long gridClickRT) {
		this.gridClickRT = gridClickRT;
	}

	public long getLineupClickRT() {
		return lineupClickRT;
	}

	public void setLineupClickRT(long lineupClickRT) {
		this.lineupClickRT = lineupClickRT;
	}

}
