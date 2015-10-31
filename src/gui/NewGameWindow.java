package gui;

import game.GameStatus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NewGameWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JComboBox<String> agentCountDropBox = new JComboBox<String>();
	
	private final JTextField mrXTextField = new JTextField();
	private final LinkedList<JTextField> agentTextFields = new LinkedList<JTextField>();
	
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
	}
	
	private void initOkButton() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				validateInput();
				lock.lock();
				try {
					prepareGameStatus();
					okClicked = true;
					gameStatusIsReady.signalAll();
					NewGameWindow.this.setVisible(false);
				} finally {
					lock.unlock();
				}
			}
		});
		this.add(okButton, BorderLayout.SOUTH);
	}
	
	private void validateInput() {
		// TODO
	}
	
	private void prepareGameStatus() {
		gameStatus = new GameStatus(10, 4, 8, 3, 4, 3, agentRows, 2);
		gameStatus.addMrX(0, mrXTextField.getText());
		
		assert(agentRows == agentTextFields.size());
		for(JTextField field : agentTextFields)
			gameStatus.addAgent(0, field.getText());
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
		
		playerPanel.add(new JLabel("Name of Mr. X:       "));
		playerPanel.add(mrXTextField);
		playerPanel.add(new JCheckBox("local Player"));
		
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
		JCheckBox checkBox = new JCheckBox("local Player");
		checkBox.setPreferredSize(checkBox.getPreferredSize());
		
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
