package analysis;

import exception.UnexpectedSymbolException;
import file.FileInput;
import file.FileOut;
import file.Table;

public class LexicalAnalysis {
    public static void run(FileInput fileInput, FileOut fileOut){
        Table words = new Table("tables/1.txt", 16);
        words.load();
        Table delimiters = new Table("tables/2.txt", 19);
        delimiters.load();
        Table numbers = new Table("tables/3.txt");
        Table identifiers = new Table("tables/4.txt");
        StringBuilder buffer = new StringBuilder();

        char next = fileInput.getChar();
        try {
            while (next != 65535) {
                buffer.append(next);
                if (isWhiteSpace(next)) {
                    next = fileInput.getChar();
                } else if (isLetter(next)) {
                    next = fileInput.getChar();
                    while (isLetter(next) || isDigit(next)) {
                        buffer.append(next);
                        next = fileInput.getChar();
                    }
                    int number = words.look(buffer);
                    if (number != -1) {
                        fileOut.out(1, number);
                    } else {
                        fileOut.out(4, identifiers.add(buffer.toString()));
                    }
                } else if (isDigit(next) || next == '.') {
                    checkNumber(next, buffer, fileInput, fileOut, numbers, delimiters);
                    next = fileInput.getChar();
                } else if (next == '|') {
                    checkDoubleSymbol('|', next, buffer, fileInput, fileOut, delimiters);
                } else if (next == '=') {
                    checkDoubleSymbol('=', next, buffer, fileInput, fileOut, delimiters);
                } else if (next == '&') {
                    checkDoubleSymbol('&', next, buffer, fileInput, fileOut, delimiters);
                } else if (next == '!' || next == '<' || next == '>' || next == ':') {
                    next = fileInput.getChar();
                    if (next == '=') {
                        buffer.append(next);
                        fileOut.out(2, delimiters.look(buffer));
                        next = fileInput.getChar();
                    } else {
                        fileOut.out(2, delimiters.look(buffer));
                    }
                } else if (delimiters.look(buffer) != -1) {
                    fileOut.out(2, delimiters.look(buffer));
                    next = fileInput.getChar();
                }
                buffer.delete(0, buffer.length());
            }
        } catch (UnexpectedSymbolException e) {
            e.printStackTrace();
        }
        identifiers.out();
        numbers.out();
        fileInput.close();
        fileOut.close();
    }

    private static void checkDoubleSymbol(char symbol, char next, StringBuilder buffer,
                                          FileInput fileInput, FileOut fileOut,
                                          Table delimiters) throws UnexpectedSymbolException {
        buffer.append(next);
        next = fileInput.getChar();
        if (next != symbol) {
            throw new UnexpectedSymbolException("Unknown delimiter");
        }
        buffer.append(next);
        fileOut.out(2, delimiters.look(buffer));
        buffer.delete(0, buffer.length());
    }

    private static void checkNumber(char next, StringBuilder buffer,
                                    FileInput fileInput, FileOut fileOut,
                                    Table numbers, Table delimiters) throws UnexpectedSymbolException {
        if (next == '0' || next == '1') {
            binary(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '2' || next == '3' || next == '4' || next == '5' || next == '6' || next == '7') {
            octal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '8' || next == '9') {
            decimal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '.') {
            buffer.insert(0, 0);
            real(next, buffer, fileInput, fileOut, numbers, delimiters);
        }
    }

    private static void binary(char next, StringBuilder buffer,
                               FileInput fileInput, FileOut fileOut,
                               Table numbers, Table delimiters) throws UnexpectedSymbolException {
        next = fileInput.getChar();
        while (next == '0' || next == '1') {
            buffer.append(next);
            next = fileInput.getChar();
        }
        if (next == '2' || next == '3' || next == '4' || next == '5' || next == '6' || next == '7') {
            octal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '8' || next == '9') {
            decimal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '.') {
            real(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'e' || next == 'E') {
            exponentialInt(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'o' || next == 'O') {
            convertOctal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'a' || next == 'c' || next == 'f' || next == 'A' || next == 'C' || next == 'F') {
            hexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'd' || next == 'D') {
            buffer.append(next);
            next = fileInput.getChar();
            convertDecimal(buffer, fileOut, numbers);
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'b' || next == 'B') {
            buffer.append(next);
            next = fileInput.getChar();
            if (isHexadecimalDigit(next)) {
                hexadecimal(next, buffer, fileInput, fileOut, numbers);
            } else {
                convertBinary(next, buffer, fileInput, fileOut, numbers);
            }
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            decimal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void convertBinary(char next, StringBuilder buffer,
                                      FileInput fileInput, FileOut fileOut,
                                      Table numbers) {
        buffer.deleteCharAt(buffer.length() - 1);
        int binary = Integer.parseInt(buffer.toString(), 2);
        fileOut.out(3, numbers.add(String.valueOf(binary)));
    }

    private static void octal(char next, StringBuilder buffer,
                              FileInput fileInput, FileOut fileOut,
                              Table numbers, Table delimiters) throws UnexpectedSymbolException {
        while (next == '0' || next == '1' || next == '2' || next == '3' ||
                next == '4' || next == '5' || next == '6' || next == '7') {
            next = fileInput.getChar();
        }
        if (next == '8' || next == '9') {
            decimal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == '.') {
            real(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'e' || next == 'E') {
            exponentialInt(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (isHexadecimalDigit(next)) {
            hexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'o' || next == 'O') {
            convertOctal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            buffer.append('d');
            convertDecimal(buffer, fileOut, numbers);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void convertOctal(char next, StringBuilder buffer,
                                     FileInput fileInput, FileOut fileOut,
                                     Table numbers) {
        buffer.append(next);
        next = fileInput.getChar();
        buffer.deleteCharAt(buffer.length() - 1);
        int octal = Integer.parseInt(buffer.toString(), 8);
        fileOut.out(3, numbers.add(String.valueOf(octal)));
    }

    private static void decimal(char next, StringBuilder buffer,
                                FileInput fileInput, FileOut fileOut,
                                Table numbers, Table delimiters) throws UnexpectedSymbolException {
        while (isDigit(next)) {
            buffer.append(next);
            next = fileInput.getChar();
        }
        if (next == '.') {
            real(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'e' || next == 'E') {
            exponentialInt(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'd' || next == 'D') {
            buffer.append(next);
            next = fileInput.getChar();
            convertDecimal(buffer, fileOut, numbers);
        } else if (next == 'a' || next == 'b' || next == 'c' || next == 'd' || next == 'e' || next == 'f' ||
                next == 'A' || next == 'B' || next == 'C' || next == 'D' || next == 'E' || next == 'F' ||
                next == '0' || next == '1' || next == '2' || next == '3' || next == '4' || next == '5' ||
                next == '6' || next == '7' || next == '8' || next == '9') {
            hexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            buffer.append('d');
            convertDecimal(buffer, fileOut, numbers);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void convertDecimal(StringBuilder buffer,
                                       FileOut fileOut,
                                       Table numbers) {
        buffer.deleteCharAt(buffer.length() - 1);
        int decimal = Integer.parseInt(buffer.toString(), 10);
        fileOut.out(3, numbers.add(String.valueOf(decimal)));
    }

    private static void hexadecimal(char next, StringBuilder buffer,
                                    FileInput fileInput, FileOut fileOut,
                                    Table numbers) throws UnexpectedSymbolException {
        while (isHexadecimalDigit(next)) {
            buffer.append(next);
            next = fileInput.getChar();
        }
        if (next == 'h' || next == 'H') {
            convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void convertHexadecimal(char next, StringBuilder buffer,
                                           FileInput fileInput, FileOut fileOut,
                                           Table numbers) {
        buffer.append(next);
        next = fileInput.getChar();
        buffer.deleteCharAt(buffer.length() - 1);
        int hexadecimal = Integer.parseInt(buffer.toString(), 16);
        fileOut.out(3, numbers.add(String.valueOf(hexadecimal)));
    }

    private static void real(char next, StringBuilder buffer,
                             FileInput fileInput, FileOut fileOut,
                             Table numbers, Table delimiters) throws UnexpectedSymbolException {

        buffer.append(next);
        next = fileInput.getChar();
        while (isDigit(next)) {
            buffer.append(next);
            next = fileInput.getChar();
        }
        if (next == ' ' || next == '\r' || next == '\n' || delimiters.contains(next)) {
            fileOut.out(3, numbers.add(buffer.toString()));
        } else if (next == 'e' || next == 'E') {
            buffer.append(next);
            next = fileInput.getChar();
            exponentialReal(next, buffer, fileInput, fileOut, numbers, delimiters);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void exponentialInt(char next, StringBuilder buffer,
                                       FileInput fileInput, FileOut fileOut,
                                       Table numbers, Table delimiters) throws UnexpectedSymbolException {
        buffer.append(next);
        next = fileInput.getChar();
        if (next == '+' || next == '-') {
            buffer.append(next);
            next = fileInput.getChar();
            while (isDigit(next)) {
                buffer.append(next);
                next = fileInput.getChar();
            }
            if (isWhiteSpace(next) || delimiters.contains(next)) {
                fileOut.out(3, numbers.add(buffer.toString()));
            } else {
                throw new UnexpectedSymbolException("Unexpected symbol in number");
            }
        } else if (isDigit(next)) {
            while (isDigit(next)) {
                buffer.append(next);
                next = fileInput.getChar();
            }
            if (isHexadecimalLetter(next)) {
                hexadecimal(next, buffer, fileInput, fileOut, numbers);
            } else if (next == 'h' || next == 'H') {
                convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
            } else if (isWhiteSpace(next) || delimiters.contains(next)) {
                fileOut.out(3, numbers.add(buffer.toString()));
            } else {
                throw new UnexpectedSymbolException("Unexpected symbol in number");
            }
        } else if (isHexadecimalLetter(next)) {
            hexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else if (next == 'h' || next == 'H') {
            convertHexadecimal(next, buffer, fileInput, fileOut, numbers);
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static void exponentialReal(char next, StringBuilder buffer,
                                        FileInput fileInput, FileOut fileOut,
                                        Table numbers, Table delimiters) throws UnexpectedSymbolException {
        if (next == '+' || next == '-') {
            buffer.append(next);
            next = fileInput.getChar();
            while (isDigit(next)) {
                buffer.append(next);
                next = fileInput.getChar();
            }
            if (isWhiteSpace(next) || delimiters.contains(next)) {
                fileOut.out(3, numbers.add(buffer.toString()));
            } else {
                throw new UnexpectedSymbolException("Unexpected symbol in number");
            }
        } else if (isDigit(next)) {
            while (isDigit(next)) {
                buffer.append(next);
                next = fileInput.getChar();
            }
            if (isWhiteSpace(next) || delimiters.contains(next)) {
                fileOut.out(3, numbers.add(buffer.toString()));
            } else {
                throw new UnexpectedSymbolException("Unexpected symbol in number");
            }
        } else {
            throw new UnexpectedSymbolException("Unexpected symbol in number");
        }
    }

    private static boolean isLetter(char nextChar) {
        return (nextChar >= 65 && nextChar <= 90) ||
                (nextChar >= 97 && nextChar <= 122);
    }

    private static boolean isDigit(char nextChar) {
        return nextChar >= 48 && nextChar <= 57;
    }

    private static boolean isHexadecimalDigit(char nextChar) {
        return (nextChar >= 48 && nextChar <= 57) ||
                (nextChar >= 65 && nextChar <= 70) ||
                (nextChar >= 97 && nextChar <= 102);
    }

    private static boolean isHexadecimalLetter(char nextChar) {
        return (nextChar >= 65 && nextChar <= 70) ||
                (nextChar >= 97 && nextChar <= 102);
    }

    private static boolean isWhiteSpace(char nextChar) {
        return nextChar == ' ' || nextChar == '\r' || nextChar == '\n' || nextChar == '\t';
    }
}
