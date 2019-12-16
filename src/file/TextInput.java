package file;

import java.io.FileInputStream;
import java.io.IOException;

public class TextInput {
    private FileInputStream fileInputStream;

    public TextInput(String fileName) {
        try {
            fileInputStream = new FileInputStream(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char getChar(){
        char nextChar = 0;
        try {
             nextChar = (char) fileInputStream.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextChar;
    }

    public void close(){
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
