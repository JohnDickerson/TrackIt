package edu.cmu.cs.eyetrack.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.Stimulus.StimulusClass;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.helper.Util.PanelID;
import edu.cmu.cs.eyetrack.state.Settings;
import edu.cmu.cs.eyetrack.state.Settings.Experiment;
import edu.cmu.cs.eyetrack.state.Trial;

@SuppressWarnings("serial")
public class LineupScreen extends Screen {

	private Trial activeTrial;
	private List<Stimulus> stimList;
	private Stimulus stimTarget;

	// Timestamp when the lineup first displays to the user; used for RTs on click
	private long initializationTime;


	public LineupScreen(EyeTrack owner, PanelID nextScreen) {
		super(owner, nextScreen);

		addMouseListener(new LineupMouseListener());
		addKeyListener(new KeyListener() {

			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					processSelectionEvent(null, false, 0);
				}
			}
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyReleased(KeyEvent e) {}
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyTyped(KeyEvent e) {}
		});
	}

	@Override
	protected void initialize() {

		Experiment exp = owner.getGameState().getSettings().getExperiment();

		// Grab all the stimuli that were used in the real trial
		activeTrial = owner.getGameState().getActiveTrial();
		// Shuffle a copy of the stimulus list, display in this order
		//stimList = new ArrayList<Stimulus>( activeTrial.getStimList() );
		//Collections.shuffle(stimList, owner.getGameState().getRandom());
		List<Stimulus> factoryAllStims = StimulusFactory.getInstance().createOneOfEach(StimulusType.TARGET);
		stimList = new ArrayList<Stimulus>();
		stimTarget = activeTrial.getTarget();

		// Randomly line stimuli up; user tries to select the one s/he followed earlier
		int numColumns, colWidth, numRows, rowHeight;
		if(exp.getMemCheckType().equals(Settings.MemoryCheckType.m2X2)) {
			numColumns = 2;
			numRows = 2;
		} else {
			numColumns = (int) Math.ceil( Math.sqrt( factoryAllStims.size() ) );
			numRows = (int) Math.floor( Math.sqrt( factoryAllStims.size() - numColumns ) + 1);
		}
		colWidth = exp.getPixelWidth() / numColumns;
		rowHeight = exp.getPixelHeight() / numRows;	

		// Each stimulus can have only one color, and each stimulus must have a unique color
		Set<Color> safeColors = new HashSet<Color>( owner.getGameState().getRandomGen().getRandomColors());

		// If we are randomizing colors (standard CMU experiments), remove illegal colors;
		// for UColorado, they want all gray all the time so it is the only color and is always legal
		if(!owner.getGameState().getSettings().getExperiment().getStimulusClass().equals(StimulusClass.UCOLORADO)) {
			for(Stimulus stimulus : activeTrial.getStims()) {
				safeColors.remove( stimulus.getColor() );
			}
		}

		for(Stimulus stimulus : factoryAllStims) {

			// Hack to make sure colors match up
			boolean colorOkay = false;
			for(Stimulus trialStim : activeTrial.getStims()) {
				if(trialStim.isEquivalent(stimulus)) {
					stimulus.setColor( trialStim.getColor() );
					colorOkay = true;
				}
			}

			if(!colorOkay) {

				Color newColor;
				if(owner.getGameState().getSettings().getExperiment().getStimulusClass().equals(StimulusClass.UCOLORADO)) {
					newColor = owner.getGameState().getRandomGen().getRandomColors().get(0);
				} else {
					if(!safeColors.iterator().hasNext()) {
						// If we have no colors left, this behavior is undefined; return black?
						newColor = Color.BLACK;
					} else {
						// Grab the next unassigned color, not taken by previously seen stims or other random gens
						newColor = safeColors.iterator().next();
					}
				}

				// Set the stimulus to own this color; nobody else can use it
				stimulus.setColor( newColor );
				safeColors.remove( newColor );
			}

			stimList.add(stimulus);
		}

		// Randomize placement of target and distractors
		Collections.shuffle(stimList);

		// CMU only wants to display 4 stimuli.  
		if(exp.getMemCheckType().equals(Settings.MemoryCheckType.m2X2)) {

			List<Stimulus> shortenedList = new ArrayList<Stimulus>();

			// Must have target from last trial
			shortenedList.add(stimTarget);

			// Add up to three other distractors that were in the trial, if three exist
			for(Stimulus stimulus : activeTrial.getStims()) {

				boolean isUnique = true;
				for(Stimulus usedStim : shortenedList) {
					if(usedStim.isEquivalent(stimulus)) {
						isUnique = false;
					}
				}

				if(isUnique) {
					shortenedList.add(stimulus);
				}

				if(shortenedList.size() >= 4) {
					break;
				}
			}


			// If we've exhausted the trial's target and distractors, add random stimuli until we hit 4

			for(Stimulus stimulus : stimList) {

				if(shortenedList.size() >= 4) {
					break;
				}

				boolean isUnique = true;
				for(Stimulus trialStim : shortenedList) {
					if(trialStim.isEquivalent(stimulus)) {
						isUnique = false;
					}
				}

				if(isUnique) {
					shortenedList.add(stimulus);
				}
			}

			stimList = shortenedList;
		}

		// Randomize placement of target and distractors
		Collections.shuffle(stimList);

		// Place the corrected stimulus in their correct locations
		int colIdx=0, rowIdx=0;
		for(Stimulus stimulus : stimList) {

			if(colIdx >= numColumns) {
				colIdx = 0;
				rowIdx++;
			}

			// Send polygon to middle of its grid
			int xPos = exp.getInsetX() + (int) ((colIdx+0.5) * colWidth); 
			int yPos = exp.getInsetY() + (int) ((rowIdx+0.5) * rowHeight);

			//System.out.println("Moving " + stimulus.getName() + " to position <" + xPos + ", " + yPos + ">.");
			stimulus.move(xPos, yPos);

			colIdx++;
		}

		setLayout(new BorderLayout());

		initializationTime = System.currentTimeMillis();
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		// Draw each of our stimuli
		for(Stimulus stimulus : stimList) {
			stimulus.draw(g2d);
		}
	}

	protected void processSelectionEvent(Stimulus selectedStim, boolean correct, long responseTime) {

		// Determine which distractor the participant clicked on; record it
		activeTrial.setLineupStimulus(selectedStim);
		activeTrial.setLineupClickCorrect(correct);
		activeTrial.setLineupClickRT(responseTime);

		owner.switchContext(nextScreen);
	}

	private class LineupMouseListener implements MouseListener {
		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseClicked(MouseEvent e) {

			int x = e.getX(), y = e.getY();

			Stimulus selectedStimulus = null;
			boolean clickCorrect = false;

			// Which object did I click, if any?
			for(Stimulus candidate : stimList) {

				// If we click inside the bounding box of a stimulus, record this
				// and break immediately (does not handle overlapping bounding boxes)
				if( candidate.getShape().getBounds().contains(x,y) ) {
					selectedStimulus = candidate;

					// Shallow equality okay since we're comparing the same pointer
					//if(selectedStimulus.getName() == stimTarget.getName()) {
					if(selectedStimulus.isEquivalent( stimTarget )) {
						clickCorrect = true;
					}
					break;
				}
			}

			// User didn't select a stimulus at all
			if(selectedStimulus == null) {
				Util.dPrintln("User clicked <" + x + ", " + y + ">, which was not a valid stimulus.");
				return;
			}

			long responseTime = System.currentTimeMillis() - initializationTime;
			Util.dPrintln("User " + (clickCorrect ? "correctly" : "incorrectly") + " selected stimulus " + selectedStimulus.toString() + " after " + responseTime + " ms.");

			processSelectionEvent(selectedStimulus, clickCorrect, responseTime);

		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseEntered(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseExited(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mousePressed(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseReleased(MouseEvent arg0) {}
	}

	@Override
	protected void tearDown() {

		// Remove all the distractors; we want a different
		// random lineup the next time around

	}

}
