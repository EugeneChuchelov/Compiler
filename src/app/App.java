package app;

import analysis.LexicalAnalysis;
import analysis.SyntaxAnalysis;
import file.LexemeInput;
import file.TextInput;
import file.LexemeOutput;

public class App {
    private static final String LEXEME_FILE = "lexeme.txt";

    private static String lexical(String sourcePath){
        TextInput textInput = new TextInput(sourcePath);
        LexemeOutput lexOut = new LexemeOutput(LEXEME_FILE);
        String result = LexicalAnalysis.run(textInput, lexOut);
        System.out.print("Lexical: ");
        System.out.println(result);
        return result;
    }

    private static String syntax(){
        LexemeInput lexemeInput = new LexemeInput(LEXEME_FILE);
        String result = SyntaxAnalysis.run(lexemeInput);
        System.out.print("Syntax:  ");
        System.out.println(result);
        return result;
    }

    public static void main(String[] args) {
        if(args.length!=1){
            System.out.println("Mandatory argument: text file with code");
            System.exit(-1);
        }

        if(!lexical(args[0]).equals("Ok"))
            System.exit(1);
        if(!syntax().equals("Ok"))
            System.exit(2);
    }
}
