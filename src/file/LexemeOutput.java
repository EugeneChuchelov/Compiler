package file;

import java.io.FileWriter;
import java.io.IOException;

public class LexemeOutput {
    private FileWriter writer;

    public LexemeOutput(String path) {
        try {
            writer = new FileWriter(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void out(int table, int entry){
        StringBuilder stringBuilder = new StringBuilder(5);
        stringBuilder.append(table).append(',').append(entry).append('\n');
        try {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
