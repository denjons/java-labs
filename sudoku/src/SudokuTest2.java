import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class TempCtrl extends JPanel implements SudokuController {
  private JButton fill  = new JButton("Fill"); 
  private JButton clear = new JButton("Clear");
  private SudokuModel model;
  
  public TempCtrl(SudokuModel m) {
    model = m;
    setLayout(new FlowLayout());
    add(fill); add(clear);
    fill.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fill();
      }
     
    });
    clear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.clear();
        r = 0; c = 0;
      }
    });   
  }
  
  private final int [][] a = 
    { {5,3,8,1,2,7,9,4,6},
      {6,2,4,8,3,9,7,5,1},
      {7,1,9,6,4,5,3,8,2},
      {9,6,5,3,1,4,8,2,7},
      {3,8,1,7,6,2,5,9,4},
      {2,4,7,5,9,8,1,6,3},
      {4,9,3,2,8,1,6,7,5},
      {8,5,6,4,7,3,2,1,9},
      {1,7,2,9,5,6,4,3,8}};
                   
  private int r = 0, c = 0;
  
  private void fill() {
    model.setBoard(r, c, a[r][c]);
    if (++c > 8) {
      c = 0;
      if (++r > 8) {
        r = 0;
      }
    }   
  }
  
  public boolean input(int row, int col, char value) {
    System.out.println("The user typed: '" + value + "' in square [" + row + "][" + col +"].");
    return value == ' ' || (value >='1' && value <= '9');
  }
}  

public class SudokuTest2 extends JFrame {
  private SudokuModel model;

  public SudokuTest2() {
    setTitle("Sudoku");
    model = new MySudokuModel();
    TempCtrl ctrl = new TempCtrl(model);
    MySudokuView view = new MySudokuView(model, ctrl);
    add(view, BorderLayout.CENTER);
    add(ctrl, BorderLayout.SOUTH);                  
    setSize(420,420);
    setLocationRelativeTo(null);  // centrera
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
   
  public static void main(String[] arg) {
    new SudokuTest2();
  }      
}