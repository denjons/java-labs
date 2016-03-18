
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;


public class MySudokuView extends JPanel implements PropertyChangeListener,KeyListener{

	private MySudokuModel model;
	private SudokuController controller;
	public MySudokuView(SudokuModel model, SudokuController controller){
		this.model = (MySudokuModel)model;
		this.controller = controller;
		ini();
		model.addPropertyChangeListener(this);
	}
	
	private void ini(){
		
		this.setMinimumSize(new Dimension(350, 350));
		this.setBackground(Color.black);
		this.setSize(350, 400);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.setLayout(new GridLayout(3, 3));
		setSudokuGrid();
		
	}
	
	private void setSudokuGrid(){
		int row=1;
		int col=1;
		int column=0;
        for(int i=1;i<=9;i++){
        	JPanel squarePanel = new JPanel();
        	squarePanel.setBackground(Color.black);
        	squarePanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        	squarePanel.setLayout(new GridLayout(3, 3));
        	col=1;
        	row=1;
        	for(int j=1;j<=9;j++){
        		Square input = new Square((int)(Math.floor((i-1)/3))*3+row,column*3+col);
        		col++;
        		input.setBorder(BorderFactory.createLineBorder(Color.black,1));
        		input.setDisabledTextColor(Color.black);
        		input.setSize(new Dimension(50, 50));
        		input.addKeyListener(this);
        		squarePanel.add(input);
        		if(j%3==0){
            		row++;
            		col=1;
            	}
        	}
        	column++;
        	if((i)%3==0){
    			column=0;
    		}
        	this.add(squarePanel);
        }
	}
	
	// Updates all or one cell. Updates all cells that for some reason contains 
	// more than one char.
	private void updateCells(int row, int col, int val, boolean all){
		row++;
		col++;
		for(Component comp : this.getComponents()){
			JPanel panel = (JPanel)comp;
			for(Component cell : panel.getComponents()){
				if(cell.getClass().equals(Square.class)){
					Square sq = (Square)cell;
					int sqRow = sq.getRow()-1;
					int sqCol = sq.getCol()-1;
					int value = this.model.getBoard(sqRow, sqCol);
					if(all){
						this.updateSquare(sq, value);
						sq.setEnabled(value==0);
					}else{
						if(sq.getText().length()>1 || !sq.getText().matches("[1-9]")){
							this.updateSquare(sq, value);
						}
						else if(sq.getRow()==row&&sq.getCol()==col){
							this.updateSquare(sq, val);
						}
					}
				}
			}
		}
	}
	
	private void updateSquare(Square sq, int value){
		if(value==0){
			sq.setText("");
		}
		else{
			sq.setText(""+value);
		}
	}
	
	// Listens to changes within the SudokuModel??
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt instanceof IndexedPropertyChangeEvent){
			IndexedPropertyChangeEvent ipce = (IndexedPropertyChangeEvent)evt;
			Integer newVal = (Integer)ipce.getNewValue();
			int row = (int)Math.floor(ipce.getIndex()/9);
			int col = ipce.getIndex()%9;
			this.updateCells(row, col, newVal, false);
		}else{
			updateCells(0, 0, 0, true);
		}
	}
	

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Object comp = e.getComponent();
		if(comp.getClass().equals(Square.class)){
			Square sq = (Square)comp;
			char val = e.getKeyChar();
			if(!controller.input(sq.getRow()-1, sq.getCol()-1, val)){
				Toolkit.getDefaultToolkit().beep();
				updateCells(sq.getRow()-1,sq.getCol()-1,0,false);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}
