package gui;

import game.GameStatus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	private final JComboBox<String> agentCountDropBox = new JComboBox<String>();
	
	private final JTextField mrXTextField = new JTextField();
	private final JCheckBox mrXNetworkCheckBox = new JCheckBox("Network Player", true);
	private final LinkedList<JTextField> agentTextFields = new LinkedList<>();
	private final LinkedList<JCheckBox> agentNetworkCheckBox = new LinkedList<>();
	
	private final JPanel playerPanel = new JPanel();
	private final GridLayout playerPanelLayout = new GridLayout(1, 3);
	private int agentRows = 0;
	private final LinkedList<Component> componentStack = new LinkedList<Component>();
	
	private final JButton okButton = new JButton("OK");
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
	}
	
	private void initOkButton() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
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
		for(JTextField field : agentTextFields)
			gameStatus.addAgent(randomNumbers.removeFirst(), field.getText());
	}
	
	private void initAgentCountDropBox() {
		for(int i = 4; i <= 9; ++i)
			agentCountDropBox.addItem("Number of Agents: " + i);
		
		agentCountDropBox.setSelectedIndex(0);
		agentCountDropBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int newAgentRows = agentCountDropBox.getSelectedIndex() + 4;
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
		playerPanel.add(mrXNetworkCheckBox);
		
		setAgentRows(4);
	}
	
	private void addAgentRow() {
		++agentRows;
		playerPanelLayout.setRows(playerPanelLayout.getRows() + 1);
		
		JLabel label = new JLabel("Name of Agent " + agentRows + ": ");
		label.setPreferredSize(label.getSize());
		
		JTextField text = new JTextField("Name");
		text.setPreferredSize(text.getSize());
		text.setText("");
		agentTextFields.addLast(text);
		
		JCheckBox checkBox = new JCheckBox("Network Player");
		checkBox.setPreferredSize(checkBox.getPreferredSize());
		agentNetworkCheckBox.addLast(checkBox);
		
		componentStack.addLast(playerPanel.add(label));
		componentStack.addLast(playerPanel.add(text));
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
		agentNetworkCheckBox.removeLast();
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
			lock.unlock();
		}
		
		return gameStatus;
	}
}
