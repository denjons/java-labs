import java.awt.Font;

import javax.swing.JTextField;


public class Square extends JTextField {
	private int row,col;
	
	
	public Square() {
		ini();
	}

	public Square( int row, int col){
		// To simplify error handling
		this.setCol(col);
		this.setRow(row);
		ini();
	}
	
	private void ini(){
		Font font = new Font("Verdana", Font.BOLD, 20);
		this.setHorizontalAlignment(JTextField.CENTER);
		this.setFont(font);
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		if(row>=1||row<=9){
			this.row=row;
		}else{
			throw new IllegalArgumentException("Row value "+row+" is out of range.");
		}
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		if(col>=1||col<=9){
			this.col=col;
		}else{
			throw new IllegalArgumentException("Column value "+col+" is out of range.");
		}
	}
	
}
