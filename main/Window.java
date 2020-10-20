package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * 
 * @author Mario Velez
 * 
 *
 */
public class Window extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3494605708565768482L;
	public static final int WIDTH = 950;
	public static final int HEIGHT = 650;
	
	public static Window window;
	
	public Window(String name)
	{
		super(name);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Dimension windowSize = new Dimension(WIDTH, HEIGHT);
		this.setSize(windowSize);
	}
	public static void main(String[] args) throws Exception
	{
		window = new Window("Pac-Man NEAT Algorithm");
		
		window.setLayout(new BorderLayout());
		
		Field field = new Field(window.getSize());
		window.add(field, BorderLayout.CENTER);
		
		JPanel pane = new JPanel();
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();
		
		pane.setLayout(layout);
		
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.weighty = 0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = new Insets(10, 10, 10, 10);
		
		JLabel slider_label = new JLabel("Ms b/w Frames: ");
		pane.add(slider_label, gc);
		
		gc.gridx = 1;
		
		JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(64);
		slider.setValue(field.refreshTime);
		slider.setMajorTickSpacing(8);
		slider.setMinorTickSpacing(4);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		slider.addChangeListener(e -> field.refreshTime = slider.getValue());
		pane.add(slider, gc);
		
		gc.gridy = 1;
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.insets = new Insets(5, 10, 5, 10);
		
		JCheckBox show_path = new JCheckBox("Show Ghost AI");
		show_path.setSelected(field.getShowPaths());
		show_path.addActionListener(e -> field.setShowPaths(show_path.isSelected()));
		pane.add(show_path, gc);
		
		gc.gridy = 2;
		
		JCheckBox show_target = new JCheckBox("Show Ghost Targets");
		show_target.setSelected(field.getShowTargets());
		show_target.addActionListener(e -> field.setShowTargets(show_target.isSelected()));
		pane.add(show_target, gc);
		
		gc.gridy = 3;
		
		JCheckBox show_grid = new JCheckBox("Show Grid");
		show_grid.setSelected(field.getShowGrid());
		show_grid.addActionListener(e -> field.setShowGrid(show_grid.isSelected()));
		pane.add(show_grid, gc);
		
		gc.weighty = 1;
		gc.gridy = 4;
		
		JCheckBox input_ghosts = new JCheckBox("Train With Ghosts");
		input_ghosts.setSelected(field.getTrainGhostInput());
		input_ghosts.addActionListener(e -> field.setTrainGhostInput(input_ghosts.isSelected()));
		pane.add(input_ghosts, gc);
		
		window.add(pane, BorderLayout.EAST);
				
		window.setVisible(true);
		window.setLocation(150, 50);
	}
	public static void close()
	{
		window.dispose();
	}
}
