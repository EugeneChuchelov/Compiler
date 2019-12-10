package app;

import analysis.LexicalAnalysis;
import file.FileInput;
import file.FileOut;

public class App {
    public static void main(String[] args) {
        FileInput fileInput = new FileInput("test.txt");
        FileOut fileOut = new FileOut("out.txt");

        LexicalAnalysis.run(fileInput, fileOut);
    }
}
