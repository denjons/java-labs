import java.awt.*;
import javax.swing.*;

public class SudokuMain extends JFrame {

  public SudokuMain() {
    SudokuModel model       = new MySudokuModel();
    MySudokuController ctrl = new MySudokuController(model);
    MySudokuView view       = new MySudokuView(model, ctrl);
    add(view, BorderLayout.CENTER);
    add(ctrl, BorderLayout.SOUTH); 
    setSize(420,420);
    setTitle("Sudoku");
    setLocationRelativeTo(null);
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public static void main(String[] arg) {
    new SudokuMain();
  } 
}
