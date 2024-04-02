// You can experiment here, it won’t be checked

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class Task {
  public static void main(String[] args) throws IOException {
    // put your code here
    try (
            Writer writer = new FileWriter("file.txt", true);
            Scanner scanner = new Scanner(System.in)
            ) {
      while (true) {
        String data = scanner.nextLine();
        try {
          if (Integer.parseInt(data) == 0) {
            break;
          } else {
            writer.write(data);
          }
        } catch (NumberFormatException e) {
          writer.write(data);
        }
      }
    }
  }
}
