package edu.cmu.cs.eyetrack.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus.StimulusClass;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.io.ResourceLoader;
import edu.cmu.cs.eyetrack.io.TestRecord;
import edu.cmu.cs.eyetrack.state.GameState;

@SuppressWarnings("serial")
public class EyeTrack extends JFrame {

	private HashMap<Util.PanelID, Screen> panelMap;

	private JPanel cardPanel;
	private CardLayout cardLayout;
	private Util.PanelID currentPanelID;

	private GameState gameState;

	public EyeTrack(int width, int height) {

		setTitle("EyeTracker v" + Util.getTrackItVersion() + "\u2014Carnegie Mellon University");
		setUndecorated(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Size the window's size appropriately and center it
		setSize(width,height);
		setResizable(false);
		setLocationRelativeTo(null);


		
		getContentPane().setBackground(Color.WHITE);

		// Initialize the wrapper container for all our different screens
		cardPanel = new JPanel();
		//cardPanel.setPreferredSize(new Dimension(500,600));
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		add(cardPanel);

		// Set up the shell for the game state (all the details about where we
		// are in the trials, settings, et cetera)
		gameState = new GameState();
		// Create and register target types and colors
		gameState.registerStimuli(StimulusClass.CMU);
				
		// Add shells for each of the separate screens; we'll need a central
		// body to facilitate communication between the various parts.
		panelMap = new HashMap<Util.PanelID, Screen>();
		addComponent(Util.PanelID.START_MENU, new StartMenuScreen(this, Util.PanelID.GAME));
		addComponent(Util.PanelID.GAME, new GameScreen(this, Util.PanelID.BUFFER1));
		addComponent(Util.PanelID.BUFFER1, new BufferScreen(this, 
				Util.PanelID.DISTRACTOR_LINEUP, 
				ResourceLoader.getInstance().getImageIcon("images/smiley_150.png")));
		addComponent(Util.PanelID.DISTRACTOR_LINEUP, new LineupScreen(this, Util.PanelID.BUFFER2));
		addComponent(Util.PanelID.BUFFER2, new BufferScreen(this, 
				Util.PanelID.GAME, 
				ResourceLoader.getInstance().getImageIcon("images/smiley_150.png")));


		// When the user exits, make sure we flush the state to our record file
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				killAndQuit();
			}
		});

		// Show the Main Menu and begin!
		switchContext(Util.PanelID.START_MENU);
		setVisible(true);
	}

	protected Map<Util.PanelID, Screen> getPanelMap() {
		return panelMap;
	}

	private void addComponent(Util.PanelID id, Screen guiPanel) {
		cardPanel.add(guiPanel, id.toString());
		panelMap.put(id, guiPanel);
	}

	protected final void killAndQuit() {
		Util.dPrintln("Flushing output and quitting.");
		try {
			TestRecord.getInstance().updateLog(gameState, true);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
		}
		
		System.exit(0);
	}

	protected boolean switchContext(Util.PanelID id) {

		// If the new context exists, switch the current context to the desired screen	
		if(panelMap.containsKey(id)) {

			// If the old context exists, tear it down before switching GUI over
			if(panelMap.containsKey(currentPanelID)) {
				panelMap.get(currentPanelID).tearDown();
			}

			// Initialize the new context before switching, then switch
			panelMap.get(id).initialize();
			cardLayout.show(cardPanel, id.toString());
			panelMap.get(id).requestFocusInWindow();

			currentPanelID = id;
			return true;
		}

		// If we ask to switch to a panel that doesn't exist, don't try.
		return false;
	}

	public GameState getGameState() { return gameState; }


	public static void main(final String args[]) {
		Runnable createAndShow = new Runnable() {
			public void run() { 
				
				int width = 700, height = 600;
				if(args.length == 2) {
					width = Integer.valueOf(args[0]);
					height = Integer.valueOf(args[1]);
					System.out.println("Setting width to " + width + " and height to " + height);
				}
				
				new EyeTrack(width, height);
			}
		};
		SwingUtilities.invokeLater(createAndShow);
	}
}
