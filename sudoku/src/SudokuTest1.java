
import java.io.*;
import java.util.*;

public class SudokuTest1 {   
  public static void main(String[] arg) throws FileNotFoundException {
    while (true) {
      SudokuModel mod = new MySudokuModel();
      Scanner sc = new Scanner(System.in);
      System.out.print("File name? ");
      String filename = sc.nextLine();
      if (filename == null)
        return;
      sc = new Scanner(new File(filename));
      String input = "";
      while (sc.hasNextLine())
        input += sc.nextLine() + '\n'; 
      System.out.println("Problem:\n" + input);   
      try {
        mod.setBoard(input);
        if (!mod.isSolvable()) 
          System.out.println("Illegal Sudoku puzzle. Not solvable");
        else if (!mod.isUnique()) 
          System.out.println("Illegal Sudoku puzzle. Not Unique");
        else {     
          mod.solve();
          System.out.println("Solution:\n" + mod); 
        }
      }
      catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }      
  }
}