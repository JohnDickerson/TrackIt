package edu.cmu.cs.eyetrack.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.toedter.calendar.JDateChooser;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.Stimulus.StimulusClass;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;
import edu.cmu.cs.eyetrack.helper.SpringUtilities;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.helper.Util.PanelID;
import edu.cmu.cs.eyetrack.io.ResourceLoader;
import edu.cmu.cs.eyetrack.io.TestRecord;
import edu.cmu.cs.eyetrack.state.ColoradoTypedTrial;
import edu.cmu.cs.eyetrack.state.GameState;
import edu.cmu.cs.eyetrack.state.RandomGen;
import edu.cmu.cs.eyetrack.state.Settings;
import edu.cmu.cs.eyetrack.state.Settings.Gender;
import edu.cmu.cs.eyetrack.state.Settings.MotionConstraintType;
import edu.cmu.cs.eyetrack.state.Settings.MotionInterpolationType;
import edu.cmu.cs.eyetrack.state.Settings.TrialType;

@SuppressWarnings("serial")
public class StartMenuScreen extends Screen {

	private String[] genderList = { "Male", "Female", "N/A" };
	private String[] trialTypes = { "All Same", "All Different", "Same as Target" };
	private String dateFormat = "MM/dd/yyyy";


	private JTextField txtName, txtTestLocation, txtImageDirectory;
	private JDateChooser datBirthdate, datTestDate;
	private JComboBox cbxGender, cbxTrialType, cbxTargetType, cbxTargetColor;
	private JSpinner spnDistractors, spnTrialCount, spnObjectSpeed, spnMinTrialLength, spnFramesPerSecond, spnSeed, spnGridXSize, spnGridYSize;
	private JCheckBox chkRandomTarget, chkRandomWithReplacement, chkUseBackgroundImages, chkUseFullscreen;
	private JRadioButton rdoMemCheckNone, rdoMemCheck2x2, rdoMemCheckNxN, rdoMotionPixel, rdoMotionGrid, rdoMotionLinear, rdoMotionRandom, rdoShapeTypeCMU, rdoShapeTypeUColorado;

	public StartMenuScreen(EyeTrack owner, PanelID nextScreen) {
		super(owner, nextScreen);
	}

	@Override
	protected void initialize() {

		ImageIcon iiLogoCMU = ResourceLoader.getInstance().getImageIcon("images/cmu_thumb_75.png");
		JLabel lblCMU = new JLabel(iiLogoCMU);

		//
		// Initialize all the "ask for user's data" components
		//
		// User's name:
		JLabel lblName = new JLabel("Name:", JLabel.TRAILING);
		txtName = new JTextField(50);
		txtName.setText("Default User");
		txtName.addFocusListener(new SelectOnceFocusListener());		
		lblName.setLabelFor(txtName);

		// User's gender, limited selection, defaults to Male
		JLabel lblGender = new JLabel("Gender:", JLabel.TRAILING);
		cbxGender = new JComboBox(genderList);
		cbxGender.setSelectedIndex(0);
		lblGender.setLabelFor(cbxGender);

		// User's birthday (i.e., age). Defaults to Unix time 0
		JLabel lblBirthdate = new JLabel("Birthdate:", JLabel.TRAILING);
		datBirthdate = new JDateChooser();
		datBirthdate.setDateFormatString(dateFormat);
		datBirthdate.setDate(new Date(0));
		lblBirthdate.setLabelFor(datBirthdate);

		// When did this test occur? Defaults to today's date.
		JLabel lblTestDate = new JLabel("Test Date:", JLabel.TRAILING);
		datTestDate = new JDateChooser();
		datTestDate.setDateFormatString(dateFormat);
		datTestDate.setDate(new Date());
		lblTestDate.setLabelFor(datTestDate);

		// Where did this test occur? Defaults to CMU's labs.
		JLabel lblTestLocation = new JLabel("Test Location:", JLabel.TRAILING);
		txtTestLocation = new JTextField(50);
		txtTestLocation.setText("Cognitive Development Lab");
		txtTestLocation.addFocusListener(new SelectOnceFocusListener());
		lblTestLocation.setLabelFor(txtTestLocation);




		//
		// Initialize all the components that deal with this specific test
		//
		// How many distractors are on the screen?
		// Default value: 4; Minimum value: 0; Maximum value: 8; Increment: 1
		JLabel lblDistractors = new JLabel("Distractors:", JLabel.TRAILING);
		spnDistractors = new JSpinner(new SpinnerNumberModel(6, 0, 8, 1));
		lblDistractors.setLabelFor(spnDistractors);

		// What do the distractors look like (e.g., same shape as main object)?
		JLabel lblTrialType = new JLabel("Trial Type:", JLabel.TRAILING);
		cbxTrialType = new JComboBox(trialTypes);
		cbxTrialType.setSelectedIndex(0);
		lblTrialType.setLabelFor(cbxTrialType);

		// How many trials (in a row) should we run?
		// Default value: 4; Minimum value: 1; Maximum value: none; Increment: 1
		JLabel lblTrialCount = new JLabel("Trial Count:", JLabel.TRAILING);
		spnTrialCount = new JSpinner(new SpinnerNumberModel(3, 1, null, 1));
		lblTrialCount.setLabelFor(spnTrialCount);

		// Do we want a random target type/color per trial, or user-set? 
		// Need a different model for CMU, UColorado's shapes 
		final Map<Stimulus.StimulusClass, ComboBoxModel> nameModels = new HashMap<Stimulus.StimulusClass, ComboBoxModel>();
		final Map<Stimulus.StimulusClass, ComboBoxModel> colorModels = new HashMap<Stimulus.StimulusClass, ComboBoxModel>();


		GameState gameState = owner.getGameState();
		for(Stimulus.StimulusClass stimulusClass : Arrays.asList( Stimulus.StimulusClass.values() )) {
			// Load this subset of shapes and colors
			StimulusFactory.getInstance().reset();
			gameState.registerStimuli(stimulusClass);
			// Make a model for the JComboBoxes based on these shapes
			Map<String, Stimulus> nameMap = StimulusFactory.getInstance().getAllOfType(StimulusType.TARGET);
			String[] targetTypes = new String[nameMap.size()];
			nameMap.keySet().toArray(targetTypes);
			ComboBoxModel cbxModel = new DefaultComboBoxModel(targetTypes);
			nameModels.put(stimulusClass, cbxModel);

			// Make a model for the JComboBoxes based on colors (Gray for UC, rainbow for CMU)
			if(stimulusClass.equals(StimulusClass.UCOLORADO)) {
				colorModels.put(stimulusClass, new DefaultComboBoxModel(new Color[] { Color.gray } ));
			} else {
				Color[] targetColors = new Color[RandomGen.getColorList().size()];
				RandomGen.getColorList().toArray(targetColors);
				colorModels.put(stimulusClass, new DefaultComboBoxModel(targetColors));
			}
		}

		// UColorado now wants "Type 1" and "Type 2" trials
		final ComboBoxModel uColoradoTypedTrialModel = new DefaultComboBoxModel(ColoradoTypedTrial.TRIAL_TYPE.values());

		final JLabel lblTargetType = new JLabel("Type:", JLabel.TRAILING);
		cbxTargetType = new JComboBox( nameModels.get(Stimulus.StimulusClass.CMU) );
		cbxTargetType.setSelectedIndex((new Random()).nextInt(cbxTargetType.getItemCount()));
		final JLabel lblTargetColor = new JLabel("Color:", JLabel.TRAILING);
		cbxTargetColor = new JComboBox(colorModels.get(Stimulus.StimulusClass.CMU));
		cbxTargetColor.setSelectedIndex((new Random()).nextInt(cbxTargetColor.getItemCount()));
		cbxTargetColor.setRenderer(new ColorCellRenderer());

		boolean targetEnabled = false; //Util.CMU_ONLY;
		lblTargetType.setEnabled(targetEnabled); lblTargetColor.setEnabled(targetEnabled);
		cbxTargetType.setEnabled(targetEnabled); cbxTargetColor.setEnabled(targetEnabled);

		// Random Targets selected -> no dropdowns for specific target color/types; and vice versa
		final JLabel lblRandomTarget = new JLabel("Target:", JLabel.TRAILING);
		chkRandomTarget = new JCheckBox("Random?");
		chkRandomTarget.setSelected( !cbxTargetType.isEnabled() );
		chkRandomTarget.addItemListener(new ItemListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void itemStateChanged(ItemEvent e) {
				cbxTargetType.setEnabled(!chkRandomTarget.isSelected());
				lblTargetType.setEnabled(!chkRandomTarget.isSelected());
				cbxTargetColor.setEnabled(!chkRandomTarget.isSelected() && !rdoShapeTypeUColorado.isSelected());
				lblTargetColor.setEnabled(!chkRandomTarget.isSelected() && !rdoShapeTypeUColorado.isSelected());
				chkRandomWithReplacement.setEnabled(chkRandomTarget.isSelected());
				sanitizeNumTrials();
			}
		});
		lblRandomTarget.setLabelFor(chkRandomTarget);

		// If we're doing random target, sample with or without replacement?
		chkRandomWithReplacement = new JCheckBox("Replace?");
		chkRandomWithReplacement.setSelected( true );
		chkRandomWithReplacement.addItemListener(new ItemListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void itemStateChanged(ItemEvent e) {
				sanitizeNumTrials();
			}
		});

		// Use either the original CMU shapes or the new ones from Sabine
		final JLabel lblShapeType = new JLabel(
				"<html>Stimuli:</html>",
				JLabel.TRAILING);
		ButtonGroup shapeTypeGroup = new ButtonGroup();
		rdoShapeTypeCMU = new JRadioButton("CMU");
		rdoShapeTypeUColorado = new JRadioButton("Colorado");
		shapeTypeGroup.add(rdoShapeTypeCMU);
		shapeTypeGroup.add(rdoShapeTypeUColorado);
		rdoShapeTypeCMU.setSelected(true);
		lblShapeType.setLabelFor(rdoShapeTypeCMU);
		// Adjust dropdown full of shapes to be either CMU-only or Colorado-only
		rdoShapeTypeCMU.addItemListener(new ItemListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void itemStateChanged(ItemEvent e) {
				cbxTargetType.setModel( nameModels.get(Stimulus.StimulusClass.CMU) );
				cbxTargetColor.setModel( colorModels.get(Stimulus.StimulusClass.CMU) );
				if(chkRandomTarget.isSelected()) {
					cbxTargetType.setSelectedIndex((new Random()).nextInt(cbxTargetType.getItemCount()));
				}
				cbxTargetColor.setEnabled(true);
				lblTargetColor.setEnabled(true);
			}
		});
		rdoShapeTypeUColorado.addItemListener(new ItemListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void itemStateChanged(ItemEvent e) {
				//cbxTargetType.setModel( nameModels.get(Stimulus.StimulusClass.UCOLORADO) );
				cbxTargetType.setModel(uColoradoTypedTrialModel);
				cbxTargetColor.setModel( colorModels.get(Stimulus.StimulusClass.UCOLORADO) );
				if(chkRandomTarget.isSelected()) {
					cbxTargetType.setSelectedIndex((new Random()).nextInt(cbxTargetType.getItemCount()));
				}
				cbxTargetColor.setEnabled(false);  // can't select colors for UColorado case
				lblTargetColor.setEnabled(false);
			}
		});


		// How fast should the objects (distractors and main object) move?
		// Pixels per second
		JLabel lblObjectSpeed = new JLabel(
				"<html>Object Speed:<br><em>Pixels per second</em></html>", 
				JLabel.TRAILING);
		spnObjectSpeed = new JSpinner(new SpinnerNumberModel(500, 1, null, 50));
		lblObjectSpeed.setLabelFor(spnObjectSpeed);

		// Trial lengths are randomized, but must be at least this long
		// Measured in milliseconds
		JLabel lblMinTrialLength = new JLabel(
				"<html>Min. Trial<br>Length <em>(ms)</em>:</html>", 
				JLabel.TRAILING);
		spnMinTrialLength = new JSpinner(new SpinnerNumberModel(10000, 1, null, 1000));
		lblMinTrialLength.setLabelFor(spnMinTrialLength);

		// How many frames per second should we update?  This might need to be changed
		// for different eyetrackers, eventually
		JLabel lblFramesPerSecond = new JLabel(
				"<html>Frames Per<br>Second:</html>", 
				JLabel.TRAILING);
		spnFramesPerSecond = new JSpinner(new SpinnerNumberModel(30, 1, 120, 1));
		lblFramesPerSecond.setLabelFor(spnFramesPerSecond);
		// For now, we won't use this.
		//spnFramesPerSecond.setEnabled(false);
		//lblFramesPerSecond.setEnabled(false);

		// The random movement of each object is controlled by a seed; for repetition's
		// sake, allow the user to manually set a seed if desired.
		JLabel lblSeed = new JLabel("Seed:", JLabel.TRAILING);
		spnSeed = new JSpinner(new SpinnerNumberModel(System.currentTimeMillis(), 0, Long.MAX_VALUE, 1));
		lblSeed.setLabelFor(spnSeed);

		// Number of width boxes x height boxes on our testing grid
		JLabel lblGridXSize = new JLabel("Grid Width:", JLabel.TRAILING);
		JLabel lblGridYSize = new JLabel("Grid Height:", JLabel.TRAILING);
		spnGridXSize = new JSpinner(new SpinnerNumberModel(3,1,10,1));
		spnGridXSize.addChangeListener(new ChangeListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void stateChanged(ChangeEvent arg0) {
				sanitizeNumDistractors();
			}
		});
		spnGridYSize = new JSpinner(new SpinnerNumberModel(3,1,10,1));
		spnGridYSize.addChangeListener(new ChangeListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void stateChanged(ChangeEvent arg0) {
				sanitizeNumDistractors();
			}
		});
		lblGridXSize.setLabelFor(spnGridXSize);
		lblGridYSize.setLabelFor(spnGridYSize);

		sanitizeNumDistractors();

		// Should we use images in the background?
		// If yes, enable the directory selection; if no, disable it.
		JLabel lblUseBackgroundImages = new JLabel("Background:", JLabel.TRAILING);
		final JLabel lblImageDirectory = new JLabel("Image Directory:", JLabel.TRAILING);
		chkUseBackgroundImages = new JCheckBox("Use Images", false);
		chkUseBackgroundImages.addChangeListener(new ChangeListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void stateChanged(ChangeEvent e) {
				txtImageDirectory.setEnabled(chkUseBackgroundImages.isSelected());
				txtImageDirectory.setForeground(Color.BLACK);
				lblImageDirectory.setEnabled(chkUseBackgroundImages.isSelected());

				if(txtImageDirectory.isEnabled() && txtImageDirectory.getText().isEmpty()) {
					//txtImageDirectory.setText("<html><font color=#FF0000>Click to Set.</font></html>");\
					txtImageDirectory.setText("Click to Set.");
				}
			}
		});
		lblUseBackgroundImages.setLabelFor(chkUseBackgroundImages);

		// Load images for trials from a user-specified folder
		// When the user selects the text box, pop up a directory browser to specify 
		// which directory to use for images
		txtImageDirectory = new JTextField();
		txtImageDirectory.setEnabled(chkUseBackgroundImages.isSelected());
		txtImageDirectory.setEditable(false);
		txtImageDirectory.addMouseListener(new DirectorySelectionListener(txtImageDirectory));
		lblImageDirectory.setEnabled(chkUseBackgroundImages.isSelected());
		lblImageDirectory.setLabelFor(txtImageDirectory);

		//
		JLabel lblUseMemoryCheck = new JLabel(
				"<html>Memory<br>Check:</html>", 
				JLabel.TRAILING);
		ButtonGroup memCheckGroup = new ButtonGroup();
		rdoMemCheckNone = new JRadioButton("None");
		rdoMemCheck2x2 = new JRadioButton("2x2");
		rdoMemCheckNxN = new JRadioButton("NxN");
		memCheckGroup.add(rdoMemCheckNone);
		memCheckGroup.add(rdoMemCheck2x2);
		memCheckGroup.add(rdoMemCheckNxN);
		rdoMemCheck2x2.setSelected(true);
		lblUseMemoryCheck.setLabelFor(rdoMemCheckNone);



		JLabel lblUseFullscreen = new JLabel("", JLabel.TRAILING);
		chkUseFullscreen = new JCheckBox("Fullscreen Trials", true);
		lblUseFullscreen.setLabelFor(chkUseFullscreen);


		// Constrain targets/distractors to trajectory changes on pixel-level, or center-of-grid level
		JLabel lblMotionConstraint = new JLabel(
				"<html>Motion<br>Constraint:</html>",
				JLabel.TRAILING);
		ButtonGroup motionConstraintGroup = new ButtonGroup();
		rdoMotionPixel = new JRadioButton("Pixel");
		rdoMotionGrid = new JRadioButton("Grid Center");
		motionConstraintGroup.add(rdoMotionPixel);
		motionConstraintGroup.add(rdoMotionGrid);
		rdoMotionGrid.setSelected(true);
		lblMotionConstraint.setLabelFor(rdoMotionPixel);


		// Constrain targets/distractors to trajectory changes on pixel-level, or center-of-grid level
		JLabel lblMotionInterpolation = new JLabel(
				"<html>Motion<br>Interpolation:</html>",
				JLabel.TRAILING);
		ButtonGroup motionInterpGroup = new ButtonGroup();
		rdoMotionLinear = new JRadioButton("Linear");
		rdoMotionRandom = new JRadioButton("Random");
		motionInterpGroup.add(rdoMotionLinear);
		motionInterpGroup.add(rdoMotionRandom);
		rdoMotionLinear.setSelected(true);
		lblMotionInterpolation.setLabelFor(rdoMotionLinear);


		//
		// Exits on press; later in the experiment, Escape exits.  Escape does not exit 
		// here because of all the spinners and text boxes taking focus.
		//
		JButton btnExit = new JButton("Exit");
		btnExit.setVerticalTextPosition(AbstractButton.CENTER);
		btnExit.setHorizontalTextPosition(AbstractButton.CENTER);
		btnExit.setMnemonic(KeyEvent.VK_E);
		btnExit.setToolTipText("Click to exit the program.");
		btnExit.addActionListener(new ActionListener() {
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void actionPerformed(ActionEvent arg0) {
				owner.killAndQuit();
			}
		});



		//
		// Upon press, button sets parameters and starts the test.
		//
		JButton btnStart = new JButton("Begin");
		btnStart.setVerticalTextPosition(AbstractButton.CENTER);
		btnStart.setHorizontalTextPosition(AbstractButton.CENTER);
		btnStart.setMnemonic(KeyEvent.VK_B);
		btnStart.setToolTipText("Click to begin testing.");
		btnStart.addActionListener(new BeginButtonListener());




		//
		// After initializing all the objects, we can deal with how
		// and where to place them on this screen
		//
		// One segment of the GUI deals only with the user's data; this is its own panel
		JPanel userDataPanel = new JPanel(new SpringLayout());
		userDataPanel.setBorder(BorderFactory.createTitledBorder("User Data"));

		int numRows = 0;
		userDataPanel.add(lblName); userDataPanel.add(txtName); numRows++;
		userDataPanel.add(lblGender); userDataPanel.add(cbxGender); numRows++;
		userDataPanel.add(lblBirthdate); userDataPanel.add(datBirthdate); numRows++;
		userDataPanel.add(lblTestDate); userDataPanel.add(datTestDate); numRows++;
		userDataPanel.add(lblTestLocation); userDataPanel.add(txtTestLocation); numRows++;

		SpringUtilities.makeCompactGrid(userDataPanel,
				numRows, 2,	//rows, cols
				6, 6,		//initX, initY
				6, 6);		//xPad, yPad


		// Another segment of the GUI deals only with the test trial's data; this is its own panel
		JPanel trialDataPanel = new JPanel(new SpringLayout());
		trialDataPanel.setBorder(BorderFactory.createTitledBorder("Trial Data"));

		numRows = 0;
		trialDataPanel.add(lblDistractors); trialDataPanel.add(spnDistractors); numRows++;
		trialDataPanel.add(lblObjectSpeed); trialDataPanel.add(spnObjectSpeed); numRows++;
		trialDataPanel.add(lblTrialType); trialDataPanel.add(cbxTrialType); numRows++;
		trialDataPanel.add(lblTrialCount); trialDataPanel.add(spnTrialCount); numRows++;
		if(Util.CMU_ONLY) {
			trialDataPanel.add(new JPanel(new GridLayout(0,1)){{
				add(lblRandomTarget); add(lblShapeType); 
			}}); 
			trialDataPanel.add(new JPanel(new GridLayout(0,1)){{
				add(new JPanel(new GridLayout(1,0)) {{ 
					add(chkRandomTarget); add(chkRandomWithReplacement);
				}});
				add(new JPanel(new GridLayout(1,0)){{ add(rdoShapeTypeCMU); add(rdoShapeTypeUColorado);}});
			}}); 
			numRows++;
			trialDataPanel.add(new JPanel(new GridLayout(0,1)){{ 
				add(lblTargetType); add(lblTargetColor);
			}}); 
			trialDataPanel.add(new JPanel(new GridLayout(0,1)){{ 
				add(cbxTargetType); add(cbxTargetColor);
			}}); numRows++;
		}
		trialDataPanel.add(lblMinTrialLength); trialDataPanel.add(spnMinTrialLength); numRows++;
		trialDataPanel.add(lblFramesPerSecond); trialDataPanel.add(spnFramesPerSecond); numRows++;
		trialDataPanel.add(lblGridXSize); trialDataPanel.add(spnGridXSize); numRows++;
		trialDataPanel.add(lblGridYSize); trialDataPanel.add(spnGridYSize); numRows++;
		trialDataPanel.add(lblUseBackgroundImages); trialDataPanel.add(chkUseBackgroundImages); numRows++;
		trialDataPanel.add(lblImageDirectory); trialDataPanel.add(txtImageDirectory); numRows++;
		trialDataPanel.add(lblUseMemoryCheck); trialDataPanel.add(new JPanel(new GridLayout(1,0)){{ add(rdoMemCheckNone); add(rdoMemCheck2x2); if(Util.CMU_ONLY) { add(rdoMemCheckNxN); } }}); numRows++;
		trialDataPanel.add(lblUseFullscreen); trialDataPanel.add(chkUseFullscreen); numRows++;
		if(Util.CMU_ONLY) {
			trialDataPanel.add(lblMotionConstraint); trialDataPanel.add(new JPanel(new GridLayout(1,0)){{ add(rdoMotionPixel); add(rdoMotionGrid);}}); numRows++;
		}
		if(Util.CMU_ONLY) {
			trialDataPanel.add(lblMotionInterpolation); trialDataPanel.add(new JPanel(new GridLayout(1,0)){{ add(rdoMotionLinear); add(rdoMotionRandom);}}); numRows++;
		}



		// We're dividing an integer here, so add some spacers if numRows is not
		// properly divisible.  Always need exactly param2 * param3 = numItems.
		if(numRows % 2 != 0) {
			trialDataPanel.add(new JPanel()); trialDataPanel.add(new JPanel()); numRows++;
		}

		SpringUtilities.makeCompactGrid(trialDataPanel,
				numRows/2, 4,	//rows, cols
				6, 6,		//initX, initY
				6, 6);		//xPad, yPad



		// One final segment of the GUI deals with bookkeeping and extraneous stuff
		//JPanel extraDataPanel = new JPanel(new SpringLayout());
		//extraDataPanel.setBorder(BorderFactory.createTitledBorder("Bookkeeping"));
		//
		//numRows = 0;
		//extraDataPanel.add(lblSeed); extraDataPanel.add(spnSeed); numRows++;
		//
		//SpringUtilities.makeCompactGrid(extraDataPanel,
		//		numRows, 2,	//rows, cols
		//        6, 6,		//initX, initY
		//        6, 6);		//xPad, yPad


		//
		// All user input is grouped together into a single panel
		JPanel inputDataPanel = new JPanel(new GridBagLayout());
		GridBagConstraints inputCons = new GridBagConstraints();
		inputCons.fill = GridBagConstraints.BOTH;
		inputCons.weighty = 0.33;
		inputCons.gridx = 0;
		inputCons.gridy = 0;
		inputDataPanel.add(userDataPanel, inputCons);
		inputCons.weighty = 0.67;
		inputCons.gridx = 0;
		inputCons.gridy = 1;
		inputDataPanel.add(trialDataPanel, inputCons);
		//inputDataPanel.add(extraDataPanel);


		//
		// All the buttons are grouped together in a panel (at the bottom of the screen)
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints buttonCons = new GridBagConstraints();
		buttonCons.fill = GridBagConstraints.HORIZONTAL;
		buttonCons.weightx = 0.10;
		buttonPanel.add(btnExit, buttonCons);
		buttonCons.weightx = 0.90;
		buttonPanel.add(btnStart, buttonCons);



		// Finally, put all the different GUI segments together into one panel
		this.setLayout(new BorderLayout());
		this.add(inputDataPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		//this.add(lblCMU, BorderLayout.NORTH);
	}

	private void sanitizeNumDistractors() {

		// Maximum number of distractors is {width} x {height} - 1 (for the target)
		int newMax = Integer.valueOf(spnGridXSize.getValue().toString()) * Integer.valueOf(spnGridYSize.getValue().toString()) - 1;

		SpinnerNumberModel model = (SpinnerNumberModel) spnDistractors.getModel();
		model.setMaximum(newMax);

		if( Integer.valueOf(spnDistractors.getValue().toString()) > newMax ) {
			model.setValue(newMax);
		}
	}

	private void sanitizeNumTrials() {

		// Maximum number of trials is infinite if we're sampling random targets with replacement,
		// but it's only |# Stimulus| if we're sampling with replacement		
		Integer newMax = (chkRandomTarget.isSelected() && !chkRandomWithReplacement.isSelected()) ? cbxTargetType.getItemCount() : null;

		SpinnerNumberModel model = (SpinnerNumberModel) spnTrialCount.getModel();
		model.setMaximum(newMax);

		if( null != newMax && Integer.valueOf(spnTrialCount.getValue().toString()) > newMax ) {
			model.setValue(newMax);
		}
	}

	@Override
	protected void tearDown() {

	}

	protected class DirectorySelectionListener implements MouseListener {

		private JTextField textField = null;

		public DirectorySelectionListener(JTextField textField) {
			super();
			this.textField = textField;
		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseClicked(MouseEvent arg0) {

			// Clicking on this shouldn't do anything if we're not using images
			if(!textField.isEnabled()) {
				return;
			}

			JFileChooser fileChooser = new JFileChooser("data/stimuli/winnie_the_pooh");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			String selectedDirectory = textField.getText();

			// User selects a directory from which to draw background images
			int response = fileChooser.showDialog(owner, "Select");
			if (response == JFileChooser.APPROVE_OPTION) {
				selectedDirectory = fileChooser.getSelectedFile().getAbsolutePath();
				Util.dPrintln("Background images will be drawn from " + selectedDirectory);
			} else {
				return;
			}



			// Notify the user that we are loading images from his selected directory
			textField.setText(selectedDirectory);
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


	protected class BeginButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// If the user clicks on the Begin button, initialize
			// the testing suite according to the user's parameters,
			// and switch contexts to begin testing

			// Grab user-specified details about the user from the GUI elements
			String name = txtName.getText();
			Gender gender = Gender.getGender(cbxGender.getSelectedItem().toString());
			Date birthdate = datBirthdate.getDate();
			Date testDate = datTestDate.getDate();
			String testLocation = txtTestLocation.getText();

			// Check to make sure the user wants to submit possibly ill-formed data
			if(name.isEmpty() || testLocation.isEmpty()) {

				String message = "<html>Some <em>non-critical</em> user data is incomplete.</html>\n" + 
						(name.isEmpty() ? "     The user's name is blank.\n" : "") +
						(testLocation.isEmpty() ? "     The testing location is blank.\n" : "") +
						"Are you sure you would like to continue?";

				String[] options = {"Continue to testing.", "Cancel."};

				int response = JOptionPane.showInternalOptionDialog(owner,
						message,
						"Non-Critical Error: User Confirmation Requested",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]
						);

				// If the user wants to fix this non-critical errors, short circuit
				if(response == 1) { return; }
			}

			// Load the correct set of stimuli, according to user input
			StimulusClass stimulusClass = rdoShapeTypeCMU.isSelected() ? Stimulus.StimulusClass.CMU : Stimulus.StimulusClass.UCOLORADO;
			StimulusFactory.getInstance().reset();
			owner.getGameState().registerStimuli(stimulusClass);

			Dimension trialDim = owner.getSize();
			double insetX = 0;
			double insetY = 0;
			if(owner.getSize().getWidth() > owner.getSize().getHeight()) {
				trialDim.setSize(trialDim.getHeight(), trialDim.getHeight());
				insetX = 0.5*(owner.getSize().getWidth() - trialDim.getWidth());
			} else if(owner.getSize().getWidth() < owner.getSize().getHeight()) {
				trialDim.setSize(trialDim.getWidth(), trialDim.getWidth());
				insetY = 0.5*(owner.getSize().getHeight() - trialDim.getHeight());
			}

			// Grab user-specified details about the trial from the GUI elements
			int numDistractors = Integer.valueOf(spnDistractors.getValue().toString());
			double objectSpeed = Double.valueOf(spnObjectSpeed.getValue().toString());
			TrialType trialType = TrialType.getTrialType(cbxTrialType.getSelectedItem().toString());
			int trialCount = Integer.valueOf(spnTrialCount.getValue().toString());
			double trialLength = Double.valueOf(spnMinTrialLength.getValue().toString());
			boolean usesRandomTarget = chkRandomTarget.isSelected();
			boolean usesSamplingWithReplacement = chkRandomWithReplacement.isSelected();
			Color specificTargetColor = (Color) cbxTargetColor.getSelectedItem();
			double fps = Double.valueOf(spnFramesPerSecond.getValue().toString());
			//long seed = Long.valueOf(spnSeed.getValue().toString());
			long seed = System.currentTimeMillis();
			int gridX = Integer.valueOf(spnGridXSize.getValue().toString());
			int gridY = Integer.valueOf(spnGridYSize.getValue().toString());
			int pixelWidth = (int) trialDim.getWidth();
			int pixelHeight=  (int) trialDim.getHeight();
			boolean usesBackgroundImages = chkUseBackgroundImages.isSelected();
			String backgroundImageDirectory = txtImageDirectory.getText();
			Settings.MemoryCheckType memCheckType = rdoMemCheckNone.isSelected() ? Settings.MemoryCheckType.mNONE :
				rdoMemCheck2x2.isSelected() ? Settings.MemoryCheckType.m2X2 :
					Settings.MemoryCheckType.mNXN;
			boolean usesFullscreen = chkUseFullscreen.isSelected();
			MotionConstraintType motionConstraintType = MotionConstraintType.GRID;
			if(Util.CMU_ONLY) {
				motionConstraintType = rdoMotionPixel.isSelected() ? MotionConstraintType.PIXEL : MotionConstraintType.GRID;
			}
			MotionInterpolationType motionInterpolationType = MotionInterpolationType.LINEAR;
			if(Util.CMU_ONLY) {
				motionInterpolationType = rdoMotionLinear.isSelected() ? MotionInterpolationType.LINEAR : MotionInterpolationType.RANDOM;
			}

			// If the user selected a canonical, specific target, make it
			// UColorado has a specific list per type I or II; deal with that now 
			Stimulus canonicalTarget = null;
			ColoradoTypedTrial coloradoTypedTrial = null;
			if(stimulusClass == Stimulus.StimulusClass.UCOLORADO) {
				ColoradoTypedTrial.TRIAL_TYPE ucTrialType = ColoradoTypedTrial.TRIAL_TYPE.valueOf(cbxTargetType.getSelectedItem().toString());
				coloradoTypedTrial = new ColoradoTypedTrial(ucTrialType);
				usesSamplingWithReplacement = true;
				trialCount = coloradoTypedTrial.getNumTrials();
				trialType = TrialType.getTrialType("All Different");
			} else {
				Stimulus specificTargetType = StimulusFactory.getInstance().getAllOfType(StimulusType.TARGET).get( cbxTargetType.getSelectedItem().toString() );
				canonicalTarget = specificTargetType.factoryClone(specificTargetColor);
			}

			// If we are using background images, load them now (before the trial starts)
			ArrayList<ImageIcon> backgroundImages = null;
			if(usesBackgroundImages) {

				backgroundImages = ResourceLoader.getInstance().loadImagesFromDirectory(backgroundImageDirectory);

				// Were any images found?  If not, error out.
				if(backgroundImages == null || backgroundImages.size() == 0) {
					JOptionPane.showMessageDialog(owner,
							"Could not find any valid images in this directory:\nDirectory: \"" + backgroundImageDirectory + "\"",
							"Error: No images found.", 
							JOptionPane.ERROR_MESSAGE);
					return;
				} 

			}


			// Finally, ask the user where he or she would like to save the data we gather
			// We save to .csv file, so filter only those
			FileWriter saveFile = null;
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File(seed + ".csv"));
			fileChooser.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {

					// Always let the user see directories
					if(f.isDirectory()) {
						return true;
					} 

					// Filter all non-.csv files
					String filetype = Util.getExtension(f);
					if(filetype == null || filetype.equals("csv")) {
						return true;
					}

					return false;
				}

				@Override
				public String getDescription() {
					return "CSV Files Only";
				}

			});

			// The user enters a file, or he cancels; if he cancels, then return.
			try {
				int response = fileChooser.showSaveDialog(owner);
				if(response == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					saveFile = new FileWriter(selectedFile);
					Util.dPrintln("Data will be saved to file " + selectedFile.getAbsolutePath());
				} else {
					return;
				}
			} catch(IOException ioException) {
				JOptionPane.showMessageDialog(owner,
						"Could not create or open save file.  Debug information follows.\n" + ioException.getMessage(),
						"Error: Could open or create save file.", 
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// One part of the game state is "Settings," which covers all the initial
			// parameters and experimental setup
			Settings settings = new Settings();
			settings.setUser(settings.new User(name, gender, birthdate, testDate, testLocation));	
			settings.setExperiment(settings.new Experiment(numDistractors, objectSpeed, trialType, trialCount, trialLength, usesRandomTarget, usesSamplingWithReplacement, stimulusClass, canonicalTarget, coloradoTypedTrial, fps, seed, gridX, gridY, pixelWidth, pixelHeight, (int) insetX, (int) insetY, usesBackgroundImages, backgroundImageDirectory, memCheckType, usesFullscreen, motionConstraintType, motionInterpolationType));

			// Alert the greater game state to our initialization parameters
			GameState gameState = owner.getGameState();
			gameState.setSettings(settings);
			gameState.setIO(saveFile);
			gameState.setBackgroundImages(backgroundImages);


			// Start off the record of our full experiment
			try {
				TestRecord.getInstance().updateLog(gameState);
			} catch(IOException ioException) {
				JOptionPane.showMessageDialog(owner,
						"Could not write to file.  Debug information follows.\n" + ioException.getMessage(),
						"Error: Could not write to save file.", 
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// If the user does not want to use the Memory Check screens, we need
			// to skip over them.
			if( memCheckType.equals(Settings.MemoryCheckType.mNONE)) {
				owner.getPanelMap().get(Util.PanelID.BUFFER1).setNextScreen(Util.PanelID.GAME);
			}

			// We're all set; switch to a game representing the values
			// the user entered on this screen
			owner.switchContext(nextScreen);
		}
	}


	/*
	 * Only add to JTextFields!
	 */
	protected class SelectOnceFocusListener implements FocusListener {

		boolean selectedBefore = false;

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void focusGained(FocusEvent e) {

			// We only want to select the entire text field upon the first
			// click; this is so the user can easily override default values.
			// After the first click, the user is probably just trying to edit
			// his or her input, not completely replace it.
			if(!selectedBefore) {
				((JTextField) e.getSource()).selectAll();
				selectedBefore = true;
			}
		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void focusLost(FocusEvent e) {

		}
	}


	static class ColorCellRenderer implements ListCellRenderer {
		// Taken from http://www.java2s.com/Code/Java/Swing-JFC/ColorComboBoxComboBoxEditorDemo.htm

		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		//private final static Dimension preferredSize = new Dimension(0, 20);

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
			if (value instanceof Color) {
				renderer.setBackground((Color) value);
			}
			//renderer.setPreferredSize(preferredSize);
			return renderer;
		}
	}

}
