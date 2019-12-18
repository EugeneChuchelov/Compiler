package analysis;

import exception.NumberFormatException;
import exception.UnexpectedSymbolException;
import exception.UnknownDelimiterException;
import file.LexemeOutput;
import file.Table;
import file.TextInput;

public class LexicalAnalysis {

    private static char next;

    private static StringBuilder buffer;

    private static TextInput textInput;

    private static LexemeOutput lexemeOutput;

    private static Table delimiters;

    private static Table numbers;

    private static int line = 1;

    private static int column = 0;

    public static String run(TextInput textInput, LexemeOutput lexemeOutput) {
        LexicalAnalysis.textInput = textInput;
        LexicalAnalysis.lexemeOutput = lexemeOutput;
        String result;

        Table words = new Table("tables/1.txt", 16);
        words.load();
        delimiters = new Table("tables/2.txt", 19);
        delimiters.load();
        numbers = new Table("tables/3.txt");
        Table identifiers = new Table("tables/4.txt");
        buffer = new StringBuilder();

        getChar();
        try {
            while (next != 65535) {
                buffer.append(next);
                if (isWhiteSpace()) {
                    getChar();
                } else if(next == '/'){
                    getChar();
                    if(next == '*'){
                        getChar();
                        while (next != '/'){
                            while(next != '*'){
                                getChar();
                            }
                            getChar();
                        }
                        getChar();
                    } else {
                        lexemeOutput.out(2, delimiters.look(buffer));
                    }
                } else if (isLetter()) {
                    getChar();
                    while (isLetter() || isDigit()) {
                        buffer.append(next);
                        getChar();
                    }
                    int number = words.look(buffer);
                    if (number != -1) {
                        lexemeOutput.out(1, number);
                    } else {
                        lexemeOutput.out(4, identifiers.add(buffer.toString()));
                    }
                } else if (isDigit() || next == '.') {
                    checkNumber();
                } else if (next == '|') {
                    checkDoubleSymbol('|');
                } else if (next == '=') {
                    checkDoubleSymbol('=');
                } else if (next == '&') {
                    checkDoubleSymbol('&');
                } else if (next == '!' || next == '<' || next == '>' || next == ':') {
                    getChar();
                    if (next == '=') {
                        buffer.append(next);
                        lexemeOutput.out(2, delimiters.look(buffer));
                        getChar();
                    } else {
                        lexemeOutput.out(2, delimiters.look(buffer));
                    }
                } else if (delimiters.look(buffer) != -1) {
                    lexemeOutput.out(2, delimiters.look(buffer));
                    getChar();
                }
                buffer.delete(0, buffer.length());
            }
            result = "Ok";
        } catch (UnknownDelimiterException e) {
            result = "Unknown delimiter at " + line + ":" + column;
        } catch (NumberFormatException e){
            result = "Unexpected symbol in number at " + line + ":" + column;
        } catch (Exception e){
            result = "General error";
        }
        identifiers.out();
        numbers.out();
        textInput.close();
        lexemeOutput.close();
        return result;
    }

    private static void getChar(){
        column++;
        next = textInput.getChar();
    }

    private static void checkDoubleSymbol(char symbol) throws UnexpectedSymbolException {
        getChar();
        if (next != symbol) {
            throw new UnknownDelimiterException();
        }
        buffer.append(next);
        lexemeOutput.out(2, delimiters.look(buffer));
        getChar();
        buffer.delete(0, buffer.length());
    }

    private static void checkNumber() throws UnexpectedSymbolException {
        if (next == '0' || next == '1') {
            binary();
        } else if (next == '2' || next == '3' || next == '4' || next == '5' || next == '6' || next == '7') {
            octal();
        } else if (next == '8' || next == '9') {
            decimal();
        } else if (next == '.') {
            buffer.insert(0, 0);
            real();
        }
    }

    private static void binary() throws UnexpectedSymbolException {
        getChar();
        while (next == '0' || next == '1') {
            buffer.append(next);
            getChar();
        }
        if (next == '2' || next == '3' || next == '4' || next == '5' || next == '6' || next == '7') {
            octal();
        } else if (next == '8' || next == '9') {
            decimal();
        } else if (next == '.') {
            real();
        } else if (next == 'e' || next == 'E') {
            exponentialInt();
        } else if (next == 'o' || next == 'O') {
            convertOctal();
        } else if (next == 'a' || next == 'c' || next == 'f' || next == 'A' || next == 'C' || next == 'F') {
            hexadecimal();
        } else if (next == 'd' || next == 'D') {
            buffer.append(next);
            getChar();
            convertDecimal();
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal();
        } else if (next == 'b' || next == 'B') {
            buffer.append(next);
            getChar();
            if (isHexadecimalDigit()) {
                hexadecimal();
            } else {
                convertBinary();
            }
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            decimal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void convertBinary() {
        buffer.deleteCharAt(buffer.length() - 1);
        int binary = Integer.parseInt(buffer.toString(), 2);
        lexemeOutput.out(3, numbers.add(String.valueOf(binary)));
    }

    private static void octal() throws UnexpectedSymbolException {
        while (next == '0' || next == '1' || next == '2' || next == '3' ||
                next == '4' || next == '5' || next == '6' || next == '7') {
            buffer.append(next);
            getChar();
        }
        if (next == '8' || next == '9') {
            decimal();
        } else if (next == '.') {
            real();
        } else if (next == 'e' || next == 'E') {
            exponentialInt();
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal();
        } else if (next == 'd' || next == 'D') {
            buffer.append(next);
            getChar();
            convertDecimal();
        } else if (isHexadecimalDigit()) {
            hexadecimal();
        } else if (next == 'o' || next == 'O') {
            convertOctal();
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            buffer.append('d');
            convertDecimal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void convertOctal() {
        buffer.append(next);
        getChar();
        buffer.deleteCharAt(buffer.length() - 1);
        int octal = Integer.parseInt(buffer.toString(), 8);
        lexemeOutput.out(3, numbers.add(String.valueOf(octal)));
    }

    private static void decimal() throws UnexpectedSymbolException {
        while (isDigit()) {
            buffer.append(next);
            getChar();
        }
        if (next == '.') {
            real();
        } else if (next == 'e' || next == 'E') {
            exponentialInt();
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal();
        } else if (next == 'd' || next == 'D') {
            buffer.append(next);
            getChar();
            convertDecimal();
        } else if (next == 'a' || next == 'b' || next == 'c' || next == 'd' || next == 'e' || next == 'f' ||
                next == 'A' || next == 'B' || next == 'C' || next == 'D' || next == 'E' || next == 'F' ||
                next == '0' || next == '1' || next == '2' || next == '3' || next == '4' || next == '5' ||
                next == '6' || next == '7' || next == '8' || next == '9') {
            hexadecimal();
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            buffer.append('d');
            convertDecimal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void convertDecimal() {
        buffer.deleteCharAt(buffer.length() - 1);
        int decimal = Integer.parseInt(buffer.toString(), 10);
        lexemeOutput.out(3, numbers.add(String.valueOf(decimal)));
    }

    private static void hexadecimal() throws UnexpectedSymbolException {
        while (isHexadecimalDigit()) {
            buffer.append(next);
            getChar();
        }
        if (next == 'h' || next == 'H') {
            convertHexadecimal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void convertHexadecimal() {
        buffer.append(next);
        getChar();
        buffer.deleteCharAt(buffer.length() - 1);
        int hexadecimal = Integer.parseInt(buffer.toString(), 16);
        lexemeOutput.out(3, numbers.add(String.valueOf(hexadecimal)));
    }

    private static void real() throws UnexpectedSymbolException {
        buffer.append(next);
        getChar();
        while (isDigit()) {
            buffer.append(next);
            getChar();
        }
        if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            lexemeOutput.out(3, numbers.add(buffer.toString()));
        } else if (next == 'e' || next == 'E') {
            buffer.append(next);
            getChar();
            exponentialReal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void exponentialInt() throws UnexpectedSymbolException {
        buffer.append(next);
        getChar();
        if (next == '+' || next == '-') {
            buffer.append(next);
            getChar();
            while (isDigit()) {
                buffer.append(next);
                getChar();
            }
            if (isWhiteSpace() || delimiters.contains(next)) {
                lexemeOutput.out(3, numbers.add(buffer.toString()));
            } else {
                throw new NumberFormatException();
            }
        } else if (isDigit()) {
            while (isDigit()) {
                buffer.append(next);
                getChar();
            }
            if (isHexadecimalLetter()) {
                hexadecimal();
            } else if (next == 'h' || next == 'H') {
                convertHexadecimal();
            } else if (isWhiteSpace() || delimiters.contains(next)) {
                lexemeOutput.out(3, numbers.add(buffer.toString()));
            } else {
                throw new NumberFormatException();
            }
        } else if (isHexadecimalLetter()) {
            hexadecimal();
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal();
        } else {
            throw new NumberFormatException();
        }
    }

    private static void exponentialReal() throws UnexpectedSymbolException {
        if (next == '+' || next == '-') {
            buffer.append(next);
            getChar();
            while (isDigit()) {
                buffer.append(next);
                getChar();
            }
            if (isWhiteSpace() || delimiters.contains(next)) {
                lexemeOutput.out(3, numbers.add(buffer.toString()));
            } else {
                throw new NumberFormatException();
            }
        } else if (isDigit()) {
            while (isDigit()) {
                buffer.append(next);
                getChar();
            }
            if (isWhiteSpace() || delimiters.contains(next)) {
                lexemeOutput.out(3, numbers.add(buffer.toString()));
            } else {
                throw new NumberFormatException();
            }
        } else {
            throw new NumberFormatException();
        }
    }

    private static boolean isLetter() {
        return (next >= 65 && next <= 90) ||
                (next >= 97 && next <= 122);
    }

    private static boolean isDigit() {
        return next >= 48 && next <= 57;
    }

    private static boolean isHexadecimalDigit() {
        return (next >= 48 && next <= 57) ||
                (next >= 65 && next <= 70) ||
                (next >= 97 && next <= 102);
    }

    private static boolean isHexadecimalLetter() {
        return (next >= 65 && next <= 70) ||
                (next >= 97 && next <= 102);
    }

    private static boolean isWhiteSpace() {
        if(next == '\n'){
            line++;
            column = 0;
        }
        return next == ' ' || next == '\r' || next == '\n' || next == '\t';
    }
}
