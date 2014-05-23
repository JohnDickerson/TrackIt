package edu.cmu.cs.eyetrack.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.Animator.RepeatBehavior;
import org.jdesktop.core.animation.timing.AnimatorBuilder;
import org.jdesktop.core.animation.timing.KeyFrames;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;
import edu.cmu.cs.eyetrack.gui.trial.Block;
import edu.cmu.cs.eyetrack.gui.trial.GameScreenBG;
import edu.cmu.cs.eyetrack.helper.Coordinate;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.helper.Util.PanelID;
import edu.cmu.cs.eyetrack.state.RandomGen;
import edu.cmu.cs.eyetrack.state.Settings;
import edu.cmu.cs.eyetrack.state.Settings.Experiment;
import edu.cmu.cs.eyetrack.state.Settings.TrialType;
import edu.cmu.cs.eyetrack.state.Trial;

@SuppressWarnings("serial")
public class GameScreen extends Screen {

	private enum Status { UNSTARTED, ANIM_STARTED, WAITING_FOR_CLICK, DONE };

	private static int MIN_JUMP_LENGTH = 500;
	private static int MAX_JUMP_LENGTH = 1500;

	private Experiment experiment;

	// We need to boot up the basic GameScreen only once---the grid, the background
	// images, etc.
	private boolean initialized = false;
	private Status status;
	private long trialLength = 0;    // in milliseconds
	private int trialCount = 0;

	// Blocks
	private int numColumns=3, numRows=3;
	private int blockWidth, blockHeight;
	private ArrayList<ArrayList<Block>> blocks;

	// Background Image (create once, use always)
	BufferedImage bgImage;

	// Stimuli
	private HashSet<Stimulus> stimEverything = null;
	private ArrayList<Stimulus> stimDistractors = null;
	private Stimulus stimTarget = null;

	// Controls the FPS of the game
	private Thread animUpdater;
	private long updateRate;

	// The exact timestamp when the distractors and targets disappear.  This can't
	// be just (trial state time + trial length) due to FPS
	private long disappearanceTime;
	
	// Records all relevant information for this Trial screen
	private Trial trial;

	// We care about the frequencies/ending location of the target,
	// so compute this with care once and keep across all trials
	private List<Coordinate<Integer>> stimTargetEndingPos;
	private Coordinate<Integer> stimTargetStartPos;

	public GameScreen(EyeTrack owner, PanelID nextScreen) {
		super(owner, nextScreen);

		//layers = new JLayeredPane();
		//setLayout(new BorderLayout());
		//add(layers);
	}

	private void startNewGame() {

		status = Status.ANIM_STARTED;

		// Start animated, switching frames at a rate of once per 1/FPS ms
		//animTask = new AnimationTask(this);
		//timer.scheduleAtFixedRate(animTask, 0L, updateRate);	
		animUpdater = new Thread(new AnimatorThread(this));
		animUpdater.start();

		Util.dPrintln("Started trial ###, length is " + trialLength + "ms.");

		for(Stimulus stim : stimEverything ) {
			stim.getAnimator().start();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static boolean enableOSXFullscreen(Window window) {
	    if(null == window) { return false; }
		try {
	        Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
	        Class params[] = new Class[]{Window.class, Boolean.TYPE};
	        Method method = util.getMethod("setWindowCanFullScreen", params);
	        method.invoke(util, window, true);
	        return true;
	    } catch (ClassNotFoundException e1) {
	      	Util.dPrintln("OS X 10.7+ fullscreen API not supported or failed.");
	    } catch (Exception e) {
	    	Util.dPrintln("OS X 10.7+ fullscreen API not supported or failed.");
	    }
		return false;
	}
	
	@Override
	protected void initialize() {

		this.status = Status.UNSTARTED;


		// Create the experiment from the user's desired settings
		Settings settings = owner.getGameState().getSettings();
		experiment = owner.getGameState().getSettings().getExperiment();

		// Only want to initialize the background grid, load the distractors, etc once---not 
		// each time the user sees the screen
		if(!initialized) {

			// Kill the titlebar and fullscreen for the experiment
			if( experiment.getUsesFullscreen() ) {
				Util.dPrintln("Attempting to switch to fullscreen mode for trials.");
				boolean keepTrying = true;
				if(Util.isRunningOnMacOSX()) {
					Util.dPrintln("Attempting Mac OS X native fullscreen API.");
					keepTrying = !GameScreen.enableOSXFullscreen(owner);
				}
				if(keepTrying && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported() ) {
					GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(owner);
					experiment.updateInsets(owner.getSize());
				} else {
					Util.dPrintln("getDefaultScreenDevice().isFullScreenSupported() returned False; cannot use fullscreen mode.");
				}
			}

			// CHECK ME!!!!
			TimingSource ts = new SwingTimerTimingSource();//1, TimeUnit.MILLISECONDS);
			AnimatorBuilder.setDefaultTimingSource(ts);
			ts.init();

			// 
			trialCount = 0;
			// First, layout the grid (width x height, etc)
			numColumns = settings.getExperiment().getGridXSize();
			numRows = settings.getExperiment().getGridYSize();



			bgImage = GameScreenBG.drawBG(owner);

			//JPanel blocksPanel = new JPanel();
			//blocksPanel.
			setLayout(new GridLayout(numColumns, numRows));

			// Get the expected size of a single block, based on the size of the parent JFrame
			blockWidth = (int) (experiment.getPixelWidth() / (double) numColumns);
			blockHeight = (int) (experiment.getPixelHeight() / (double) numRows);

			// If we're using background images, keep track of which ones we've used
			int bgIdx = 0;
			List<ImageIcon> bgImages = owner.getGameState().getBackgroundImages();

			// Now, create a separate block object for each block on the grid
			blocks = new ArrayList<ArrayList<Block>>();
			for(int colIdx=0; colIdx<numColumns; colIdx++) {
				ArrayList<Block> row = new ArrayList<Block>();
				for(int rowIdx=0; rowIdx<numRows; rowIdx++) {

					Block block = null;
					if(experiment.getUsesBGImages()) {
						block = new Block(blockWidth, blockHeight, bgImages.get(bgIdx));
						bgIdx = (bgIdx+1) % bgImages.size();
					} else {
						block = new Block(blockWidth, blockHeight);
					}

					// J2SE Polygon
					row.add(block);
					//blocksPanel.add(block);
					//add(block);
				}
				blocks.add(row);
			}

			//layers.add(blocksPanel, 0);

			// Set up the animation to occur at whatever user-specified FPS
			updateRate = (long) (1000.0 / settings.getExperiment().getFPS());
			if(1000 % settings.getExperiment().getFPS() != 0) {
				Util.dPrintln("Frames per second unable to be split evenly across one second.");
			}
			Util.dPrintln("Setting animation to update once per " + updateRate + " milliseconds.");

			// Add a click listener to report where (what grid box) the user clicks
			addMouseListener(new GridBoxMouseListener());

			// The user presses the Spacebar to begin the trial
			setFocusable(true);
			requestFocusInWindow();
			addKeyListener(new BasicTrialKeyListener());
			
			// Register real stimuli
			
			owner.getGameState().registerStimuli(blockWidth, blockHeight);
			
			// Compute the ending location for the target (across all trials)
			stimTargetEndingPos = owner.getGameState().getRandomGen().getRandomEndPositions(
					owner.getGameState().getSettings().getExperiment().getTrialCount() );

			initialized = true;
		}

		// Set up the mutable parts of the GUI---the distractors, movement vectors,
		// initial positions, timing elements, etc.
		setupNewTrial(settings.getExperiment().getNumDistractors());
		requestFocusInWindow();
	}


	private void setupNewTrial(int numDistractors) {


		// Make a new record for this trial
		trialCount++;
		trial = new Trial(trialCount);
		owner.getGameState().addTrial(trial);

		stimDistractors = new ArrayList<Stimulus>();
		stimEverything = new HashSet<Stimulus>();

		// If we are randomizing colors per-trial (instead of per-experiment), pick some random colors
		List<Color> stimColors = owner.getGameState().getRandomGen().getRandomColors();
		int colorIdx=0;
		//trial.setStimColors(stimColors);
		
		// Create the target, place it first---we have the requirement
		// that the ENDING grid position of the target must be uniformly
		// distributed across all grid positions, as well as the BEGINNING position
		if(owner.getGameState().getSettings().getExperiment().getUsesRandomTarget()) {
			stimTarget = StimulusFactory.getInstance().create(StimulusType.TARGET, stimColors.get(colorIdx++));
		} else {
			stimTarget = owner.getGameState().getSettings().getExperiment().getCanonicalTarget().factoryClone();
			
			// Adjust colors so that the target's color is first in line, skipped by distractors below.
			for(int idx=0; idx<stimColors.size(); idx++) {
				if(stimColors.get(idx).equals(stimTarget.getColor())) {
					stimColors.set(idx, stimColors.get(0));
					stimColors.set(0, stimTarget.getColor());
					colorIdx++;
					break;
				}
			}
		}
		
		// Create #distractors Distractors, according to user's preferences
		TrialType trialType = owner.getGameState().getSettings().getExperiment().getTrialType();
		if(trialType.equals(TrialType.ALL_SAME) || trialType.equals(TrialType.SAME_AS_TARGET)) {

			Stimulus distractorParent = null;
			if(trialType.equals(TrialType.SAME_AS_TARGET)) {
				// Distractors all look same as target
				distractorParent = stimTarget;
			} else if(trialType.equals(TrialType.ALL_SAME)) {
				// Distractors all look the same, but different than target
				do {
					distractorParent = StimulusFactory.getInstance().create(StimulusType.DISTRACTOR, stimColors.get(colorIdx));
				} while(distractorParent.isEquivalent(stimTarget));
			}

			for(int disIdx=0; disIdx < numDistractors; disIdx++) {
				stimDistractors.add(distractorParent.factoryClone());
			}

		} else if(trialType.equals(TrialType.ALL_DIFF) && numDistractors > 0) {
			// Distractors all look the same as target
			List<Stimulus> allDiff = StimulusFactory.getInstance().createOneOfEach(StimulusType.DISTRACTOR);

			int disIdx = 0;
			do {
				if( !allDiff.get(disIdx).isEquivalent(stimTarget) ) {
					stimDistractors.add( allDiff.get(disIdx).factoryClone( stimColors.get(colorIdx) ) );
					colorIdx = (colorIdx + 1) % stimColors.size();
				}
				disIdx = (disIdx + 1) % allDiff.size();
			} while(stimDistractors.size() < numDistractors);
		}


		// Sometimes, we'd like to iterate over all the Stimuli together
		stimEverything.addAll(stimDistractors);
		stimEverything.add(stimTarget);

		// Keep track of stimulus for the I/O
		trial.pushStims(stimEverything);
		trial.setTarget(stimTarget);


		RandomGen rGen = owner.getGameState().getRandomGen();
		List<Coordinate<Integer>> initialPlacements = rGen.genRandomGridPositions(stimEverything.size());
		trialLength = ((long) owner.getGameState().getSettings().getExperiment().getTrialLength());
		
		// Place the stimulus in random boxes
		// Also initialize their first movement instructions
		int idx=0;
		// First, place the target stimulus and figure out how long this trial will last
		placeAndPlanStimulus(stimTarget, initialPlacements.get(idx++));
		// Second, place all the other distractors
		for( final Stimulus stim : stimDistractors ) {
			Coordinate<Integer> gridPlacement = initialPlacements.get(idx++);
			placeAndPlanStimulus(stim, gridPlacement);
		}


		// Wait for the user to hit the spacebar
		status = Status.UNSTARTED;
	}


	private void placeAndPlanStimulus(final Stimulus stim, Coordinate<Integer> startingGrid) { 
		
		// Place stimulus in its original location
		Coordinate<Integer> gridCenter = getGridCenter(startingGrid);
		moveStimulus(stim, gridCenter);


		// Length of the trial is the minimum length specified by the user
		// plus some random small number of milliseconds---this is defined by the target's movement
		
		List<KeyFrames<Integer>> keyFramesList = new ArrayList<KeyFrames<Integer>>();
		double timeUsed = owner.getGameState().getRandomGen().calcKeyFrames(keyFramesList, 
				MIN_JUMP_LENGTH, 
				MAX_JUMP_LENGTH,
				trialLength, 
				stim.getWidth()/2,
				stim.getHeight()/2,
				stim.getCenter(), 
				stim == stimTarget ? getGridCenter(stimTargetEndingPos.get(trialCount-1)) : null);   // stimTarget must end in specific grid center


		if(stim == stimTarget) {
			// Keep track of where the target started, for the red circle
			stimTargetStartPos = startingGrid;
			// If the target used more time than allocated, update this
			trialLength = (long) timeUsed;
			trial.setLength(trialLength);
			
		}

		final KeyFrames<Integer> keyFramesX = keyFramesList.get(0);
		final KeyFrames<Integer> keyFramesY = keyFramesList.get(1);

		final TimingTarget randomMovement = new TimingTargetAdapter() {
			@Override
			public void timingEvent(Animator source, double fraction) {
				stim.move( keyFramesX.getInterpolatedValueAt(fraction),
						keyFramesY.getInterpolatedValueAt(fraction) );
			}
		};

		stim.setAnimator( new AnimatorBuilder()
		.setDuration(trialLength, TimeUnit.MILLISECONDS)
		.addTarget(randomMovement)
		.setRepeatCount(1)
		.setRepeatBehavior(RepeatBehavior.LOOP)
		//.setInterpolator(new AccelerationInterpolator(0.4, 0.4))
		.build() );

	}
	
	// Remove the stimuli, maybe call 
	@Override
	protected void tearDown() {

		// All stimuli will be recreated come next trial
		stimDistractors = null;
		stimTarget = null;
		stimEverything = null;

	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		// stimTargetEndingPos

		g.drawImage(bgImage, 0, 0, null);

		Graphics2D g2d = (Graphics2D) g;

		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw each of our stimuli
		if( status == Status.UNSTARTED || status == Status.ANIM_STARTED ) {

			for(Stimulus stimulus : stimDistractors) {
				stimulus.draw(g2d);
			}
			stimTarget.draw(g2d);
		}

		// Draw a red circle around the target stimulus
		if( status == Status.UNSTARTED) {

			float halfStroke = 2.0f;
			Shape circle = new Ellipse2D.Float(experiment.getInsetX() + stimTargetStartPos.getX()*blockWidth + halfStroke, 
					experiment.getInsetY() + stimTargetStartPos.getY()*blockHeight + halfStroke, 
					blockWidth - halfStroke, 
					blockHeight - halfStroke);

			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(2.0f * halfStroke));
			g2d.draw(circle);
			g2d.setColor(Color.BLACK);
		}
	}

	private void processFinalClick(int x, int y) {
		// Figure out which grid box the user selected
		int gridX = xToGrid(x), gridY = yToGrid(y);

		trial.setUserClickPos(new Coordinate<Double>(Double.valueOf(x),Double.valueOf(y)));
		trial.setUserClickGridPos(new Coordinate<Integer>(gridX, gridY));

		// Figure out if the user selected the *right* grid box
		boolean clickCorrect = trial.getUserClickGridPos().equals(trial.getTargetFinalGridPos());
		trial.setGridClickCorrect(clickCorrect);
		
		// How long did it take the user to click on a box, once the distractors and target disappeared?
		long responseTime = System.currentTimeMillis() - disappearanceTime;
		trial.setGridClickRT(responseTime);
		Util.dPrintln("User " + (clickCorrect ? "correctly" : "incorrectly") + " selected grid[" + gridX + ", " + gridY + "] in " + responseTime + " ms.");

		// Done with this segment; send user to a cool-down screen
		status = Status.DONE;
		owner.switchContext(PanelID.BUFFER1);
	}
	
	class GridBoxMouseListener implements MouseListener {

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5 
		public void mouseClicked(MouseEvent e) {

			// If the animation is done and we are waiting for the participant to select
			// the final grid box in which the target ended, process
			if( status == Status.WAITING_FOR_CLICK ) {
				processFinalClick(e.getX(), e.getY());
			}
		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseEntered(MouseEvent e) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseExited(MouseEvent e) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mousePressed(MouseEvent e) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseReleased(MouseEvent e) {}

	}

	class BasicTrialKeyListener implements KeyListener {

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void keyPressed(KeyEvent e) {
			// If the user presses the Spacebar, start the trial
			if(e.getKeyCode() == KeyEvent.VK_SPACE && status == Status.UNSTARTED) {
				Util.dPrintln("Spacebar pressed; starting trial.");
				startNewGame();
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE && status == Status.WAITING_FOR_CLICK) {
				processFinalClick(-1,-1);
			}

		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void keyReleased(KeyEvent e) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void keyTyped(KeyEvent e) {}
	}


	class AnimatorThread implements Runnable {

		private JPanel mainPanel;
		private long trialStart;

		public AnimatorThread(JPanel mainPanel) {
			super();
			this.mainPanel = mainPanel;
			this.trialStart = 0L;
		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void run() {

			while(Thread.currentThread() == animUpdater) {
				long frameStartTime = System.currentTimeMillis();

				if(trialStart == 0) {
					//trialStart = this.scheduledExecutionTime();
					trialStart = frameStartTime;
					trial.setStartTime(trialStart);
				}



				// Call .update() on each of the Shape objects
				for(Stimulus stimulus : stimEverything) {
					trial.pushPos(stimulus, new Coordinate<Double>(stimulus.getCenter(), frameStartTime));
				}
				trial.incFrameCount();


				//System.out.println("Target center: " + stimTarget.getCenter());
				//System.out.println("Grid center: " + getGridCenter( xToGrid(stimTarget.getCenter().getX()), yToGrid(stimTarget.getCenter().getY())));
				

				// If we've run for at least the length of the trial, 
				// quit out to the part where the user clicks on the grid
				if( trialLength < (frameStartTime - trialStart) ) {
					//this.cancel();
					
					
					// Record where the target stopped
					trial.setTargetFinalPos(stimTarget.getCenter());
					trial.setTargetFinalGridPos(new Coordinate<Integer>(
							xToGrid( stimTarget.getCenter().getX()),
							yToGrid( stimTarget.getCenter().getY())));

					disappearanceTime = frameStartTime;
					status = Status.WAITING_FOR_CLICK;
					repaint();	
					return;
				}
				
				mainPanel.repaint();

				// Tick again in "exactly" 40fps
				try {
					Thread.sleep(Math.max(0, updateRate - (System.currentTimeMillis() - frameStartTime)));
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	protected int xToGrid(int x) {
		return xToGrid((double) x);
	}

	protected int yToGrid(int y) {
		return yToGrid((double) y);
	}

	// Make these double versions more precise if we ever need it
	protected int xToGrid(double x) {
		return (int) ( (x - experiment.getInsetX() ) / blockWidth);
	}

	protected int yToGrid(double y) {
		return (int) ( (y - experiment.getInsetY() ) / blockHeight);
	}


	protected Coordinate<Integer> getGridCenter(Coordinate<Integer> gridBlock) {
		return getGridCenter(gridBlock.getX(), gridBlock.getY());
	}

	protected Coordinate<Integer> getGridCenter(int gridX, int gridY) {
		return new Coordinate<Integer>( 
				(int) (experiment.getInsetX() + (gridX * blockWidth) + (0.5*blockWidth)),
				(int) (experiment.getInsetY() + (gridY * blockHeight) + (0.5*blockHeight)));
	}

	protected void moveStimulus(Stimulus stimulus, Coordinate<Integer> coordinate) {

		stimulus.move(coordinate.getX(), coordinate.getY());
		//stimulus.getPolygon().translate(
		//		((int) -stimulus.getPolygon().getBounds().getX()) + coordinate.getX() - (int) (0.5 * stimulus.getPolygon().getBounds().getWidth()), 
		//		((int) -stimulus.getPolygon().getBounds().getY()) + coordinate.getY() - (int) (0.5 * stimulus.getPolygon().getBounds().getHeight()));
	}
}
