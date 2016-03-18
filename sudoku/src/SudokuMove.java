
public class SudokuMove {
	
	private int row, col ,val;

	public SudokuMove(int row, int col, int val) {
		this.row = row;
		this.col = col;
		this.val = val;
	}
	

	public SudokuMove(int row, int col) {
		this.row = row;
		this.col = col;
		this.val = 0;
	}


	public SudokuMove() {
	}


	public int getRow() {
		return row;
	}


	public void setRow(int row) {
		this.row = row;
	}


	public int getCol() {
		return col;
	}


	public void setCol(int col) {
		this.col = col;
	}


	public int getVal() {
		return val;
	}


	public void setVal(int val) {
		this.val = val;
	}


	
	
	
}
