package edu.cmu.cs.eyetrack.state;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.ImageIcon;

import edu.cmu.cs.eyetrack.gui.shapes.BoxStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.CircleStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.CrescentStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.CrossStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.DiamondStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.HeartStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.OctagonStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.StarStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.Stimulus.StimulusClass;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;
import edu.cmu.cs.eyetrack.gui.shapes.TriangleStimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part1.Sabine2Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part1.Sabine6Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part1.Sabine8Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew1Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew3Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew4Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew5Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew6Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew7Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew8Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.SabineNew9Stimulus;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.io.TestRecord;

public class GameState {

	private Settings settings;
	private History history;

	private FileWriter file;
	private LinkedList<String[]> outputBuffer;

	private Trial activeTrial;

	private ArrayList<ImageIcon> backgroundImages;
	
	// Keeps track of the set of Target stimuli we've used
	private Set<Stimulus> previousStimTargets;

	// Maintain a single Random instance, always draw from this;
	// given a unique seed, should be able to reproduce experiment exactly
	private Random random;
	private RandomGen randomGen;

	private int currentTrialCount = 0;

	public GameState() {
		outputBuffer = new LinkedList<String[]>();
		previousStimTargets = new HashSet<Stimulus>();
	}

	public void registerStimuli(int blockWidth, int blockHeight, StimulusClass stimulusClass) {

		// Kill everything that's been inserted before.
		StimulusFactory.getInstance().reset();

		// Color each stimulus a different, randomly generated (someday...) color 
		List<Color> stimColors;
		switch(stimulusClass) {
		case CMU:
			// CMU wants a random rainbow of colors
			int totalNumberOfStimuli = 9;
			if(getRandomGen() == null) {
				stimColors = new ArrayList<Color>();
				for(int idx=0; idx<totalNumberOfStimuli; idx++) {
					stimColors.add(Color.BLACK);
				}
			} else {
				stimColors = getRandomGen().getRandomColors( totalNumberOfStimuli );
			}
			break;
		case UCOLORADO:
			// UColorado wants only gray
			totalNumberOfStimuli = 11;
			stimColors = new ArrayList<Color>();
			for(int idx=0; idx<totalNumberOfStimuli; idx++) {
				stimColors.add(Color.GRAY);
			}
			break;
		default:
			throw new IllegalArgumentException("Cannot understand StimulusClass type " + stimulusClass);
		}

		// Register some Stimuli as targets, some as distractors
		int colorIdx = 0;
		double stimScale = 0.5;
		double minBlockDim = Math.min(blockHeight, blockWidth) * stimScale;

		switch(stimulusClass) {
		case CMU:
			StimulusFactory.getInstance().registerStimulus(new BoxStimulus("Box", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new TriangleStimulus("Triangle", stimColors.get(colorIdx++), (int) minBlockDim), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new CrossStimulus("Cross", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new OctagonStimulus("Octagon", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new CircleStimulus("Circle", stimColors.get(colorIdx++),(int) minBlockDim), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new HeartStimulus("Heart", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new StarStimulus("Star", stimColors.get(colorIdx++), 5, minBlockDim / 4.0, minBlockDim / 2.0), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new DiamondStimulus("Diamond", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			StimulusFactory.getInstance().registerStimulus(new CrescentStimulus("Crescent", stimColors.get(colorIdx++), (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			break;
		case UCOLORADO:
			// Older stimuli before UC wanted an update to less-recognizable shapes
			//StimulusFactory.getInstance().registerStimulus(new Sabine1Stimulus("Stimulus 1", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			//StimulusFactory.getInstance().registerStimulus(new Sabine2Stimulus("Stimulus 2", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			//StimulusFactory.getInstance().registerStimulus(new Sabine3Stimulus("Stimulus 3", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			//StimulusFactory.getInstance().registerStimulus(new Sabine4Stimulus("Stimulus 4", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine5Stimulus("Stimulus 5", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine6Stimulus("Stimulus 6", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine7Stimulus("Stimulus 7", stimColors.get(colorIdx++),  (int) minBlockDim), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine8Stimulus("Stimulus 8", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine9Stimulus("Stimulus 9", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new Sabine10Stimulus("Stimulus 10", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);	
			//StimulusFactory.getInstance().registerStimulus(new SabineNew2Stimulus("Stimulus 2", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);
			//StimulusFactory.getInstance().registerStimulus(new SabineNew10Stimulus("Stimulus 10", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.BOTH);		
			
			// Newer stimuli (Sept 2015)
			/*	1 --> SabineNewStimulus4
				2 --> SabineNewStimulus8
				3 --> SabineNewStimulus5
				4 --> SabineNewStimulus3
				5 --> SabineNewStimulus1 */
			StimulusFactory.getInstance().registerStimulus(new SabineNew4Stimulus("Target 1", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.TARGET);
			StimulusFactory.getInstance().registerStimulus(new SabineNew8Stimulus("Target 2", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.TARGET);		
			StimulusFactory.getInstance().registerStimulus(new SabineNew5Stimulus("Target 3", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.TARGET);
			StimulusFactory.getInstance().registerStimulus(new SabineNew3Stimulus("Target 4", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.TARGET);
			StimulusFactory.getInstance().registerStimulus(new SabineNew1Stimulus("Target 5", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.TARGET);
			/*	6 --> SabineNewStimulus6
				7 --> SabineNewStimulus7
				8 --> SabineStimulus6
				9 --> SabineStimulus8
				10 -> SabineNewStimulus9 
				11 -> SabineStimulus2 */
			StimulusFactory.getInstance().registerStimulus(new SabineNew6Stimulus("Distractor 6", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);
			StimulusFactory.getInstance().registerStimulus(new SabineNew7Stimulus("Distractor 7", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);
			StimulusFactory.getInstance().registerStimulus(new Sabine6Stimulus("Distractor 8", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);	
			StimulusFactory.getInstance().registerStimulus(new Sabine8Stimulus("Distractor 9", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);	
			StimulusFactory.getInstance().registerStimulus(new SabineNew9Stimulus("Distractor 10", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);		
			StimulusFactory.getInstance().registerStimulus(new Sabine2Stimulus("Distractor 11", stimColors.get(colorIdx++),  (int) (blockWidth * stimScale), (int) (blockHeight * stimScale)), StimulusType.DISTRACTOR);
			break;
		default:
			throw new IllegalArgumentException("Cannot understand StimulusClass type " + stimulusClass);
		}
	}

	public void registerStimuli(StimulusClass stimulusClass) {
		registerStimuli(1, 1, stimulusClass);
	}

	public boolean hasMoreToWrite() {
		return !outputBuffer.isEmpty();
	}

	public String[] nextDataLine() {
		return outputBuffer.removeFirst();
	}

	public void addTrial(Trial trial) {
		// If we've never added a Trial before, we need to tack on CSV headers
		if(history == null) {
			history = new History();
		}

		// Add the trial to our history of all trials, and push its data to the write buffer
		history.addTrial(trial);
		outputBuffer.add(trial.getCSVHeader());

		// Assume that this is our current trial now
		activeTrial = trial;
	}

	public void endActiveTrial() {
		outputBuffer.addAll(activeTrial.getCSVData());

		try {
			TestRecord.getInstance().updateLog(this);
		} catch(IOException ex) {
			Util.dPrintln("ERROR: Cannot write data to file!");
			ex.printStackTrace();
		}

	}

	public RandomGen getRandomGen() {
		return randomGen;
	}

	public void setBackgroundImages(ArrayList<ImageIcon> backgroundImages) {
		this.backgroundImages = backgroundImages;
	}

	public ArrayList<ImageIcon> getBackgroundImages() {
		return backgroundImages;
	}

	public Settings getSettings() {
		return settings;
	}

	public Random getRandom() {
		return random;
	}

	public Trial getActiveTrial() {
		return activeTrial;
	}

	public void setActiveTrial(Trial activeTrial) {
		this.activeTrial = activeTrial;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;

		// Set up the global random counter
		random = new Random(settings.getExperiment().getSeed());
		StimulusFactory.getInstance().setRandom(random);
		randomGen = new RandomGen(random, settings.getExperiment());

		// We only add Settings once, so add the header and the data to the write buffer
		outputBuffer.add(settings.getCSVHeader());

		List<String[]> settingsData = settings.getCSVData();
		for(String[] row : settingsData) {
			outputBuffer.add(row);
		}
	}

	public void setIO(FileWriter file) {
		this.file = file;
	}

	public FileWriter getIO() {
		return file;
	}

	public int getCurrentTrialCount() {
		return currentTrialCount;
	}

	public void incCurrentTrialCount() {
		this.currentTrialCount++;
	}

	public Set<Stimulus> getPreviousStimTargets() {
		return previousStimTargets;
	}
}
