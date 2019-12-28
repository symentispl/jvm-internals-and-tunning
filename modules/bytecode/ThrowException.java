import java.io.*;

public class ThrowException {

  public static void main(String[] args){
    try{
      FileInputStream input = new FileInputStream(new File("badfilename"));
      input.read(new byte[16]);
    } catch(FileNotFoundException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
  }


}
