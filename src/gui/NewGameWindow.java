package gui;

import game.GameStatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import map.data.GraphData;

public class NewGameWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String[] choosableColorStrings = 
		{ "red", "orange", "yellow", "green", "blue", "puple", "cyan",  "white"};
	private static final Color[] choosableColors =
		{ Color.red, Color.orange, Color.yellow.brighter(), Color.green, 
		Color.blue, Color.magenta.darker(), Color.cyan,  Color.white};
	
	private static final int maxAgentCount = choosableColorStrings.length;
	private static final int minAgentCount = 4;
	
	private final JComboBox<String> agentCountDropBox = new JComboBox<String>();
	
	private final JTextField mrXTextField = new JTextField();
	private final JCheckBox mrXNetworkCheckBox = new JCheckBox("Local Player", true);
	private final LinkedList<JTextField> agentTextFields = new LinkedList<>();
	private final LinkedList<JComboBox<String>> agentColors = new LinkedList<>();
	private final LinkedList<JCheckBox> agentNetworkCheckBoxes = new LinkedList<>();
	
	private final JPanel playerPanel = new JPanel();
	private final GridLayout playerPanelLayout = new GridLayout(1, 4);
	private int agentRows = 0;
	private final LinkedList<Component> componentStack = new LinkedList<Component>();
	
	private final JButton okButton = new JButton("Start");
	private final Lock lock = new ReentrantLock();
	private final Condition gameStatusIsReady = lock.newCondition();
	private boolean okClicked = false;
	private GameStatus gameStatus = null;

	public NewGameWindow() {
		initAgentCountDropBox();
		initPlayerPanel();	
		initOkButton();
		this.pack();
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				lock.lock();
				try {
					okClicked = true;
					gameStatus = null;
					gameStatusIsReady.signalAll();
				} finally {
					lock.unlock();
				}
			}
		});
		
		mrXNetworkCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mrXTextField.setEditable(e.getStateChange() == ItemEvent.SELECTED);
			}		
		});
	}
	
	private void initOkButton() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isInputValid()) {
					lock.lock();
					try {
						prepareGameStatus();
						okClicked = true;
						gameStatusIsReady.signalAll();
						NewGameWindow.this.setVisible(false);
					} finally {
						lock.unlock();
					}
				} else {
					JOptionPane.showMessageDialog(NewGameWindow.this, "Input is invalid!");
				}
			}
		});
		this.add(okButton, BorderLayout.SOUTH);
	}
	
	private boolean isInputValid() {
		for(int i = 0; i < agentRows; ++i) {
			for(int j = i + 1; j < agentRows; ++j) {
				if(agentColors.get(i).getSelectedIndex() == agentColors.get(j).getSelectedIndex())
					return false;
			}
		}
		return true;
	}
	
	private void prepareGameStatus() {
		gameStatus = new GameStatus(10, 4, 8, 3, 4, 3, agentRows, 2);
		
		LinkedList<Integer> randomNumbers = new LinkedList<>();
		for(int i = 1; i <= GraphData.STATION_COUNT; ++i)
			randomNumbers.add(i);
		Collections.shuffle(randomNumbers);
		
		gameStatus.addMrX(randomNumbers.removeFirst(), mrXTextField.getText());
		
		assert(agentRows == agentTextFields.size());
		assert(agentRows == agentColors.size());
		for(int i = 0; i < agentRows; ++i) {
			String name = agentTextFields.get(i).getText();
			Color color = choosableColors[agentColors.get(i).getSelectedIndex()];
			
			gameStatus.addAgent(randomNumbers.removeFirst(), name, color);
		}
	}
	
	private void initAgentCountDropBox() {
		for(int i = minAgentCount; i <= maxAgentCount; ++i)
			agentCountDropBox.addItem("Number of Agents: " + i);
		
		agentCountDropBox.setSelectedIndex(0);
		agentCountDropBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int newAgentRows = agentCountDropBox.getSelectedIndex() + minAgentCount;
				NewGameWindow.this.setAgentRows(newAgentRows);
				NewGameWindow.this.pack();
			}
		});
		this.add(agentCountDropBox, BorderLayout.NORTH);
	}
	
	private void initPlayerPanel() {
		playerPanel.setLayout(playerPanelLayout);
		this.add(playerPanel, BorderLayout.CENTER);
		
		JLabel label = new JLabel("Name of Mr. X: ");
		label.setPreferredSize(label.getSize());
		playerPanel.add(label);
		playerPanel.add(mrXTextField);
		playerPanel.add(new JPanel());
		playerPanel.add(mrXNetworkCheckBox);
		
		setAgentRows(4);
	}
	
	private void addAgentRow() {
		++agentRows;
		playerPanelLayout.setRows(playerPanelLayout.getRows() + 1);
		
		JLabel label = new JLabel("Name of Agent " + agentRows + ": ");
		label.setPreferredSize(label.getSize());
		
		final JTextField text = new JTextField();
		text.setPreferredSize(text.getSize());
		text.setEditable(false);
		agentTextFields.addLast(text);
		
		JComboBox<String> colorDropDown = new JComboBox<>(choosableColorStrings);
		colorDropDown.setSelectedIndex(agentRows - 1);
		agentColors.addLast(colorDropDown);
		
		JCheckBox checkBox = new JCheckBox("Local Player");
		checkBox.setPreferredSize(checkBox.getPreferredSize());
		checkBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				text.setEditable(e.getStateChange() == ItemEvent.SELECTED);
			}		
		});
		agentNetworkCheckBoxes.addLast(checkBox);
		
		componentStack.addLast(playerPanel.add(label));
		componentStack.addLast(playerPanel.add(text));
		componentStack.addLast(playerPanel.add(colorDropDown));
		componentStack.addLast(playerPanel.add(checkBox));
	}
	
	private void removeAgentRow() {
		if(agentRows <= 4)
			throw new IllegalStateException("Less than 4 players are not supported.");
		
		--agentRows;
		playerPanelLayout.setRows(playerPanelLayout.getRows() - 1);
		
		for(int i = 0; i < playerPanelLayout.getColumns(); ++i)
			playerPanel.remove(componentStack.removeLast());
		
		agentTextFields.removeLast();
		agentColors.removeLast();
		agentNetworkCheckBoxes.removeLast();
	}
	
	private void setAgentRows(int newAgentRows) {
		int diff = newAgentRows - agentRows;
		
		for(int i = 0; i < diff; ++i)
			addAgentRow();
		
		for(int i = 0; i > diff; --i)
			removeAgentRow();
	}
	
	public GameStatus waitForGameStatus() {
		lock.lock();
		try {
			while(!okClicked)
				gameStatusIsReady.awaitUninterruptibly();
		} finally {
			okClicked = false;
			lock.unlock();
		}
		
		return gameStatus;
	}
}
