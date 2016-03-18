import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;


public class MySudokuModel implements SudokuModel {
	
	private final PropertyChangeSupport pcs = 
			new PropertyChangeSupport(this);
	private boolean seekingUnique;
	private int [][] board = new int[9][9];
	private boolean fireEvent=true;
	
	public MySudokuModel(){
		clear();
	}
	
	public MySudokuModel(int[][] b){
		int len = b.length;
		if(len==9&&b[len-1].length==9){
			for(int i=0;i<b.length;i++){
				for(int j=0;j<b[i].length;j++){
					this.board[i][j]=b[i][j];
				}
			}
		}else{
			throw new IllegalArgumentException("Illeagl size of sudoku array.");
		}
	}
	
	// Sets all cells to 0.
	// Fires PropertyChange.
	@Override
	public void clear() {
		this.fireEvent=false;
		for(int n=0;n<board.length;n++){
			for(int i=0;i<board[n].length;i++){
				this.board[n][i]=0;
			}
		}
		this.fireEvent=true;
		pcs.firePropertyChange("board",null,null);
	}
	
	// Sets a cell at a given position with a given value.
	@Override
	public void setBoard(int row, int col, int val) {
		if(isLegal(row,col,val)){
			board[row][col]=val;
			if(this.fireEvent){
				pcs.fireIndexedPropertyChange("board", (row)*9+(col), false, val);
			}
		}else{
			throw new IllegalArgumentException("Illeagal operation");
		}
	}
	
	// Does the same as setBoard, without generating an exception for duplicate values
	// Fires IndexedPropertyChange
	public boolean setCell(int row, int col, int val){
		if(((row>=0&&row<=8)&&(col>=0&&col<=8))&&(val>=0&&val<=9)){
			board[row][col]=val;
			pcs.fireIndexedPropertyChange("board", (row)*9+(col), false, val);
			return true;
		}else{
			return false;
		}
	}
	
	// reads and parses a sudoku string
	// Fires PropertyChange
	@Override
	public void setBoard(String input) {
		input = input.trim();
		this.fireEvent=false;
		if(legalString(input)){
			this.clear();
			String [] strs=input.split("\\n");
			int r=0;
			for(String str : strs ){
				for(int c=0;c<str.length();c++){
					String elm = str.substring(c, c+1);
					if(elm.matches("[1-9]")){
						setBoard(r, c,Integer.parseInt(elm));
					}else{
						setBoard(r, c,0);
					}
				}
				r++;
			}
			this.fireEvent=true;
			pcs.firePropertyChange("board", true, false);
		}else{
			throw new IllegalArgumentException("Illegal sudoku string in setBoard");
		}
		
	}
	
	// Checks if a sudoku string is legal.
	public boolean legalString(String sudoku){
		String [] strs = sudoku.split("\\n");
		if(strs.length != 9){
			return false;
		}
		for(String s : strs){
			if(!s.matches("^[\\d '.']{9}$")){
				return false;
			}
		}
		return true;
	}
	
	// Returns the value of a cell on a given position.
	@Override
	public int getBoard(int row, int col) {
		if((row>=0&&row<9)&&(col>=0&&col<9)){
			return board[row][col];
		}else{
			throw new IllegalArgumentException("getBoard: index out of range.");
		}
	}

	// Returns the current state of the sudoku array as a string.
	@Override
	public String getBoard() {
		String result="";
		for(int[]block:board){
			for(int i:block){
				if(i==0){
					result+=".";
				}else{
					result+=i;
				}
			}
			result+="\n";
		}
		return result;
	}
	
	// Checks if f a given input is legal.
	@Override
	public boolean isLegal(int row, int col, int val) {
		if((row>=0&&row<=8)&&(col>=0&&col<=8)){
			int [][] copy = this.board;
			copy[row][col]=val;
			if(noDuplicates(new MySudokuModel(copy))){
				return true;
			}
		}
		return false;
	}
	
	// Checks that no row, column or square contains duplicate values.
	private boolean noDuplicates(MySudokuModel s){
		if(s!=null){
			return (noDups(s.getRows())&&noDups(s.getCols()))&&noDups(s.getSquares());
		}else{
			return false;
		}
	}
	
	// Returns false if an array contains duplicate values.
	private boolean noDups(int [][] blocks ){
		for(int[]block:blocks){
			for(int i=0;i<block.length;i++){
				if(block[i]>0){
					for(int a=(i+1);a<block.length;a++){
						if(block[i]==block[a]){
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	// Returns all rows in a sudoku.
	private int[][] getRows(){
		return this.board;
	}
	
	// Returns all columns in a sudoku.
	private int[][] getCols(){
		int[][] cols = new int[9][9];
		for(int n=0;n<9;n++){
			for(int i=0;i<9;i++){
				cols[n][i]=board[i][n];
			}
		}
		return cols;
	}
	
	// Returns all squares in a sudoku.
	private int[][] getSquares(){
		int [][]squares= new int[9][9];
		int row=0;
		int col=0;
		for(int i=0;i<9;i=i+3){
			for(int i2=0;i2<9;i2++){
				for(int i3=0;i3<3;i3++){
					squares[row][col]=this.board[i2][i+i3];
					col++;
				}
				if((i2+1)%3==0){
					row++;
					col=0;
				}
			}
		}
		return squares;
	}
	
	// Solves the current sudoku if possible.
	// Fires PropertyChange
	public boolean solve(){
		this.fireEvent=false;
		if(this.trySolve()){
			pcs.firePropertyChange("board", true, false);
			return true;
		}
		this.fireEvent=true;
		return false;
	}
	
	// Tries to solve a sudoku.
	private boolean trySolve(){
        int x = firstX();
        int y = firstY();
        int val = 1;
        if(y==-1||x==-1){
        	if(this.isSolved()){
        		return true;
        	}
        }
        for (int i=val;i<=9;++i) {
            if (this.isLegal(y,x,i)) {
            	setBoard(y, x,i);
                if(this.solve()){
                	if(!seekingUnique){
            			return true;
            		}
            		else {
            			seekingUnique=false;
            		}
                }
            }
        }
        this.board[y][x]=0;
        return false;
	}
	
	// Returns the index of the first row containing empty cells.
	// It otherwise returns -1
	public int firstY(){
		for(int y=0;y<this.board.length;y++){
			for(int x=0;x<this.board[y].length;x++){
				if(this.board[y][x]==0){
					return y;
				}
			}
		}
		return -1;
	}
	
	// Returns the index of the first empty column of the first row containing empty cells.
	// It otherwise returns -1
	public int firstX(){
		int y=firstY();
		if(y==-1){
			return -1;
		}
		for(int x=0;x<this.board[y].length;x++){
			if(this.board[y][x]==0){
				return x;
			}
		}
		return -1;
	}
	
	// Checks if the current sudoku is solved.
	public boolean isSolved(){
		return this.noDuplicates(this)&&this.isFilledOut();	
	}
	
	// Checks if the current sudoku is filled out.
	private boolean isFilledOut(){
		return firstX()==-1&&firstY()==-1;
	}

	// Checks if the current sudoku is solvable.
	@Override
	public boolean isSolvable() {
		MySudokuModel sud = new MySudokuModel(this.board);
		return sud.solve();
	}
	
	// Returns true if the sudoku only has one unique solution.
	@Override
	public boolean isUnique() {
		int [][]copy=this.board;
		seekingUnique=true;
		boolean result=this.solve();
		seekingUnique=false;
		this.board=copy;
		return !result;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		this.pcs.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		 this.pcs.removePropertyChangeListener(l);
	}

	@Override
	public String toString() {
		return this.getBoard();
	}
	
	
	
}
