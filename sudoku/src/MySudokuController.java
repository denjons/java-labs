
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class MySudokuController extends JPanel 
	implements SudokuController, ActionListener {

	private MySudokuModel model;
	private MySudokuModel initialModel;
	private JButton newBtn, solveBtn, redoBtn, undoBtn;
	private final JFileChooser fc;
	private BufferedReader br;
	private LinkedList <SudokuMove> newValues;
	private LinkedList <SudokuMove> oldValues;
	private int moveIndex;
	
	public MySudokuController(Model model){
		this.model = (MySudokuModel)model;
		initialModel = new MySudokuModel();
		this.fc = new JFileChooser();
		setControlPanel();
		reset();
	}
	
	// Sets the controller GUI.
	private void setControlPanel(){
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.setSize(350, 25);
		
		newBtn = new JButton("New");
		newBtn.addActionListener(this);
		controlPanel.add(newBtn);
		
		solveBtn = new JButton("Solve");
		solveBtn.addActionListener(this);
		controlPanel.add(solveBtn);
		
		redoBtn = new JButton("Redo");
		redoBtn.addActionListener(this);
		redoBtn.setEnabled(false);
		controlPanel.add(redoBtn);
		
		undoBtn = new JButton("Undo");
		undoBtn.addActionListener(this);
		undoBtn.setEnabled(false);
		controlPanel.add(undoBtn);
	
		this.add(controlPanel);
	}
	
	// resets the history
	private void reset(){
		this.newValues = new LinkedList<SudokuMove>();
		this.oldValues = new LinkedList<SudokuMove>();
		undoBtn.setEnabled(false);
		redoBtn.setEnabled(false);
		this.solveBtn.setEnabled(true);
		moveIndex=0;
	}
	
	// Undoes the most recent move.
	private void undo(){
		if(this.moveIndex>0){
			moveIndex--;
			SudokuMove sm = this.oldValues.get(this.moveIndex);
			this.model.setCell(sm.getRow(),sm.getCol(),sm.getVal());
			this.redoBtn.setEnabled(true);
		}
		if(moveIndex<=0){
			this.undoBtn.setEnabled(false);
		}
	}
	
	// Resets previous move.
	private void redo(){
		if(this.moveIndex<this.newValues.size()){
			this.moveIndex++;
			SudokuMove sm = this.newValues.get(this.moveIndex-1);
			this.model.setCell(sm.getRow(),sm.getCol(),sm.getVal());
			this.undoBtn.setEnabled(true);
		}
		if(this.moveIndex==this.newValues.size()){
			this.redoBtn.setEnabled(false);
		}
	}
	
	// Flushes all subsequent history if a new move is made.
	private void flush(){

		while(this.moveIndex<this.newValues.size()){
			this.newValues.removeLast();
			this.oldValues.removeLast();
		}
	}
	
	// Updates history if the new value not is equal to the old one.
	private void updateHistory(int row, int col, int val, int oldVal){
		this.flush();
		if(oldVal!=val){
			this.oldValues.add(new SudokuMove(row, col, oldVal));
			this.newValues.add(new SudokuMove(row, col, val));
			this.moveIndex++;
			this.undoBtn.setEnabled(true);
		}
		if(this.moveIndex==this.newValues.size()){
			this.redoBtn.setEnabled(false);
		}
	}
	
	// Handles input from the view
	@Override
	public boolean input(int row, int col, char value) {
		int val;
		if(value=='\b'){
			val=0;
		}else{
			val = Character.getNumericValue(value);
		}
		int oldVal = this.model.getBoard(row,col);
		if(this.model.setCell(row, col, val)){
			this.updateHistory(row, col, val, oldVal);
			if(this.model.isSolved()){
				JOptionPane.showMessageDialog(this.getParent(), 
        				"Congrats, you solved it.",
        				"Sudoku Solved", JOptionPane.OK_OPTION);
			}
			return true;
		}else{
			return false;
		}
	}
	
	// Handles button events
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.newBtn) {
			this.readSudokuFile();
	    }
		else if(e.getSource() == this.solveBtn){
			this.solveSudoku();
		}
		else if(e.getSource() == this.redoBtn){
			this.redo();
		}
		else if(e.getSource() == this.undoBtn){
			this.undo();
		}
	}
	
	// Solves the sudoku
	public void solveSudoku(){
		if(this.model.isSolvable()){
			this.model.solve();
			reset();
		}else{
			this.model.setBoard(initialModel.getBoard());
			if(this.model.solve()){
				reset();	
			}else{
				JOptionPane.showMessageDialog(this.getParent(), 
        				"Sudoku sheet is not solvable",
        				"Sudoku error", JOptionPane.WARNING_MESSAGE);
			}
		}
		this.solveBtn.setEnabled(false);
	}
	
	// Reads Sudoku from file.
	private boolean readSudokuFile(){
		int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String name = file.getName();
            int i = name.lastIndexOf(".");
            String extension = name.substring(i);
            if (extension != null && (extension.equals(".txt")||
            		extension.equals(".sud"))){
            	String content="";
            	try{
            		String line="";
            		this.br = new BufferedReader(
            				new FileReader(file.getAbsolutePath()));
            		while ((line=br.readLine()) != null) {
            			line = line.replaceAll(System.getProperty("line.separator"), "");
            			line = line.trim();
            			if(line!=""){
            				content+=line+"\n";
            			}
        			}
            		try{
                		this.model.setBoard(content);
                		this.initialModel.setBoard(content);
                	}catch(Exception ex2){
                		this.model.clear();
                		JOptionPane.showMessageDialog(this.getParent(), 
                				"Sudoku is not legal in file: "+name,
                				"Sudoku error", JOptionPane.ERROR_MESSAGE);
                		return false;
                	}
            	}catch(Exception ex){
            		JOptionPane.showMessageDialog(this.getParent(), 
            				"Failed to open file: "+name,
            				"File error", JOptionPane.ERROR_MESSAGE);
            		return false;
            	}
            }else{
            	JOptionPane.showMessageDialog(this.getParent(), 
            			"Invalid sudoku file: "+name,
            			"File error", JOptionPane.ERROR_MESSAGE);
            	return false;
            }
        } 
        this.reset();
        return true;
	}

}
