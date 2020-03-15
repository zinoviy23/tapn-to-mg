package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.SortConstantsCommand;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.gui.components.ConstantsListModel;
import dk.aau.cs.gui.components.NonsearchableJList;

public class ConstantsPane extends JPanel {
	private static final long serialVersionUID = -7883351020889779067L;
	private JPanel constantsPanel;
	private JScrollPane constantsScroller;
	private JPanel buttonsPanel;

	private JList constantsList;
	private ConstantsListModel listModel;
	private JButton editBtn;
	private JButton removeBtn;

	private TabContent parent;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton sortButton;

	private static final String toolTipEditConstant = "Edit the value of the selected constant";
	private static final String toolTipRemoveConstant = "Remove the selected constant";
	private static final String toolTipNewConstant = "Create a new constant";
	private static final String toolTipSortConstants = "Sort the constants alphabetically";
	private final static String toolTipMoveUp = "Move the selected constant up";
	private final static String toolTipMoveDown = "Move the selected constant down";
	//private static final String toolTipGlobalConstantsLabel = "Here you can define a global constant for reuse in different places.";


	public ConstantsPane(boolean enableAddButton, TabContent currentTab) {
		parent = currentTab;

		constantsPanel = new JPanel(new GridBagLayout());
		buttonsPanel = new JPanel(new GridBagLayout());

		listModel = new ConstantsListModel(parent.network());
		listModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent arg0) {				
			}

			public void intervalAdded(ListDataEvent arg0) {
				constantsList.setSelectedIndex(arg0.getIndex0());
				constantsList.ensureIndexIsVisible(arg0.getIndex0());
			}

			public void intervalRemoved(ListDataEvent arg0) {
				int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
				constantsList.setSelectedIndex(index);
				constantsList.ensureIndexIsVisible(index);
			}
			
		});

		constantsList = new NonsearchableJList(listModel);
		constantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		constantsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!(e.getValueIsAdjusting())) {
					if (constantsList.getSelectedIndex() == -1) {
						editBtn.setEnabled(false);
						removeBtn.setEnabled(false);
					} else {
						removeBtn.setEnabled(true);
						editBtn.setEnabled(true);						
					}
					
					if (constantsList.getModel().getSize() >= 2) {
						sortButton.setEnabled(true);
					} else
						sortButton.setEnabled(false);

					int index = constantsList.getSelectedIndex();
					if(index > 0)
						moveUpButton.setEnabled(true);
					else
						moveUpButton.setEnabled(false);


					if(index < parent.network().constants().size() - 1)
						moveDownButton.setEnabled(true);
					else
						moveDownButton.setEnabled(false);
				}
			}
		});

		constantsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!constantsList.isSelectionEmpty()) {
					
					int index = constantsList.locationToIndex(arg0.getPoint());
					ListModel dlm = constantsList.getModel();
					Constant c = (Constant) dlm.getElementAt(index);
					constantsList.ensureIndexIsVisible(index);
					
					highlightConstant(index);
					
					if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
						showEditConstantDialog(c,index);						
					}
				}
			}
		});
		
		constantsList.addKeyListener(new KeyAdapter() {
		
			public void keyPressed(KeyEvent arg0) {				
				ListModel model = constantsList.getModel();
				if (model.getSize()>0) {
					Constant c = (Constant) model.getElementAt(constantsList.getSelectedIndex());
					if (c != null) {
						if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {										
							if (!(c.lowerBound() == c.value())){
								Command edit = parent.network().updateConstant(c.name(), new Constant(
										c.name(), c.value()-1));
								CreateGui.getDrawingSurface().getUndoManager().addNewEdit(edit);
								parent.network().buildConstraints();
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
							if (!(c.upperBound() == c.value())){
								Command edit = parent.network().updateConstant(c.name(), new Constant(
										c.name(), c.value()+1));
								CreateGui.getDrawingSurface().getUndoManager().addNewEdit(edit);
								parent.network().buildConstraints();
							}
						} 
						else if (arg0.getKeyCode() == KeyEvent.VK_UP) {
							if(constantsList.getSelectedIndex() > 0){
								highlightConstant(constantsList.getSelectedIndex()-1);
							}
						}
						else if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
							if(constantsList.getSelectedIndex() < constantsList.getModel().getSize()-1){
								highlightConstant(constantsList.getSelectedIndex()+1);
							}
						}
					}
				}
			}
		});
		
		addConstantsComponents();
		addConstantsButtons(enableAddButton);
		
		constantsList.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				removeConstantHighlights();
			}
		});

		setLayout(new BorderLayout());
		this.add(constantsPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.PAGE_END);

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Global Constants"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3))
				);
		this.setToolTipText("Declaration of global constants that can be used in intervals and age invariants");

		//this.setToolTipText(toolTipGlobalConstantsLabel);
		//showConstants();
		
		this.addComponentListener(new ComponentListener() {
			int minimumHegiht = ConstantsPane.this.getMinimumSize().height;
			public void componentShown(ComponentEvent e) {
			}
			
			
			public void componentResized(ComponentEvent e) {
				if(ConstantsPane.this.getSize().height <= minimumHegiht){
					sortButton.setVisible(false);
				} else {
					sortButton.setVisible(true);
				}
			}
			
			
			public void componentMoved(ComponentEvent e) {
			}
			
			
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		this.setMinimumSize(new Dimension(this.getMinimumSize().width, this.getMinimumSize().height - sortButton.getMinimumSize().height));

	}
	
	private void highlightConstant(int index){
		ListModel model = constantsList.getModel();
		Constant c = (Constant) model.getElementAt(index);
		
		if(c != null && !c.hasFocus()){
			for(int i = 0; i < model.getSize(); i++){
				((Constant) model.getElementAt(i)).setFocused(false);
			}
			c.setFocused(true);
			CreateGui.getCurrentTab().drawingSurface().repaintAll();
		}
	}
	
	public void removeConstantHighlights(){
		ListModel model = constantsList.getModel();
		for(int i = 0; i < model.getSize(); i++){
			((Constant) model.getElementAt(i)).setFocused(false);
		}
		try{
			CreateGui.getCurrentTab().drawingSurface().repaintAll();
		}catch(Exception e){
			// It is okay, the tab has just been closed
		}
	}

	private void addConstantsButtons(boolean enableAddButton) {
		editBtn = new JButton("Edit");
		editBtn.setEnabled(false);
		editBtn.setToolTipText(toolTipEditConstant);
		editBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Constant c = (Constant) constantsList.getSelectedValue();				
				showEditConstantDialog(c,constantsList.getSelectedIndex());
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(editBtn, gbc);

		removeBtn = new JButton("Remove");
		removeBtn.setEnabled(false);
		removeBtn.setToolTipText(toolTipRemoveConstant);
		removeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String constName = ((Constant) constantsList.getSelectedValue()).name();
				removeConstant(constName);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(removeBtn, gbc);

		JButton addConstantButton = new JButton("New");
		addConstantButton.setToolTipText(toolTipNewConstant);
		addConstantButton.setEnabled(enableAddButton);
		addConstantButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showEditConstantDialog(null);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.WEST;
		buttonsPanel.add(addConstantButton, gbc);
	}

	public void showConstants() {
		TimedArcPetriNetNetwork model = parent.network();
		if (model == null)
			return;

		listModel.updateAll();

	}

	private void addConstantsComponents() {
		constantsScroller = new JScrollPane(constantsList);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		constantsPanel.add(constantsScroller, gbc);

		moveUpButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Up.png")));
		moveUpButton.setEnabled(false);
		moveUpButton.setToolTipText(toolTipMoveUp);
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = constantsList.getSelectedIndex();

				if(index > 0) {
					parent.swapConstants(index, index-1);
					showConstants();
					constantsList.setSelectedIndex(index-1);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTH;
		constantsPanel.add(moveUpButton,gbc);

		moveDownButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Down.png")));
		moveDownButton.setEnabled(false);
		moveDownButton.setToolTipText(toolTipMoveDown);
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = constantsList.getSelectedIndex();

				if(index < parent.network().constants().size() - 1) {
					parent.swapConstants(index, index+1);
					showConstants();
					constantsList.setSelectedIndex(index+1);
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(moveDownButton,gbc);

		//Sort button
		sortButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("resources/Images/Sort.png")));
		sortButton.setToolTipText(toolTipSortConstants);
		sortButton.setEnabled(false);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command sortConstantsCommand = new SortConstantsCommand(parent, ConstantsPane.this);
				CreateGui.getDrawingSurface().getUndoManager().addNewEdit(sortConstantsCommand);
				sortConstantsCommand.redo();
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		constantsPanel.add(sortButton,gbc);
	}

	private void showEditConstantDialog(Constant constant) {	
		ConstantsDialogPanel panel = null;
		if (constant != null)
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network(), constant);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		panel.showDialog();
		showConstants();
	}

	private void showEditConstantDialog(Constant constant, int selectedIndex) {	
		ConstantsDialogPanel panel = null;
		if (constant != null)
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network(), constant);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			try {
				panel = new ConstantsDialogPanel(new JRootPane(),
						parent.network());
			} catch (IOException e) {
				e.printStackTrace();
			}
		panel.showDialog();
		showConstants();
	}

	protected void removeConstant(String name) {
		TimedArcPetriNetNetwork model = parent.network();
		Command edit = model.removeConstant(name);
		if (edit == null) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You cannot remove a constant that is used in the net.\nRemove all references "
							+ "to the constant in the net and try again.",
							"Constant in use", JOptionPane.ERROR_MESSAGE);
		} else
			parent.drawingSurface().getUndoManager().addNewEdit(edit);

		//showConstants();
	}

	public void setNetwork(TimedArcPetriNetNetwork tapnNetwork) {
		listModel.setNetwork(tapnNetwork);
	}

	public void selectFirst() {
		constantsList.setSelectedIndex(0);

	}

}
