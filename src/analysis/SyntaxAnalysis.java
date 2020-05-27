package analysis;

import entity.Identifier;
import entity.Lexeme;
import exception.AlreadyDefinedException;
import exception.NotDefinedException;
import exception.SemanticsException;
import exception.TypesMismatchException;
import file.LexemeInput;
import file.Table;

import java.util.*;

public class SyntaxAnalysis {

    private static Lexeme next;

    private static LexemeInput lexemeInput;

    private static Table words;

    private static Table delimiters;

    private static Table numbers;

    private static List<Identifier> identifiers;

    private static int line = 1;

    private static int column = 0;

    public static String run(LexemeInput lexemeInput) {
        SyntaxAnalysis.lexemeInput = lexemeInput;

        words = new Table("tables/1.txt", 16);
        words.load();
        delimiters = new Table("tables/2.txt", 19);
        delimiters.load();
        numbers = new Table("tables/3.txt", 16);
        numbers.load();

        identifiers = new ArrayList<>();

        String result;

        try{
            if(program()){
                result = "Ok";
            } else {
                result = "Syntax error at " + line + ":" + column;
            }
        } catch (AlreadyDefinedException e){
            result = "Defined variable was defined again at " + line + ":" + column;
        } catch (NotDefinedException e){
            result = "Not defined variable was used at " + line + ":" + column;
        } catch (TypesMismatchException e){
            result = "Types mismatch at " + line + ":" + column;
        } catch (Exception e){
            result = "General error";
        }

        return result;
    }

    private static boolean isNext(String lexeme) {
        char firstSymbol = lexeme.charAt(0);
        int table;
        int number;
        if (firstSymbol >= 97 && firstSymbol <= 122) {
            table = 1;
            number = words.look(lexeme);
            if (number == -1){
               table = 2;
               number = delimiters.look(lexeme);
            }
        } else {
            table = 2;
            number = delimiters.look(lexeme);
        }
        return table == next.getTable() && number == next.getNumber();
    }

    private static boolean program() throws SemanticsException {
        getNext();
        if(!isNext("{"))
            return false;
        getNext();
        do {
            if (!(description() || complex())) {
                return false;
            }
            if (!isNext(";")) {
                return false;
            }
            line++;
            column = -1;
            getNext();
        } while (!isNext("}"));
        return true;
    }

    private static boolean description() throws AlreadyDefinedException {
        if(!type())
            return false;
        String type = delimiters.get(next.getNumber());
        getNext();

        List<Identifier> described = new ArrayList<>();
        addIdentifier(described, next, type);

        if (!notDescribedIdentifier())
            return false;
        getNext();

        while (isNext(",")) {
            getNext();
            if (!notDescribedIdentifier())
                return false;
            addIdentifier(described, next, type);
            getNext();
        }
        identifiers.addAll(described);
        return true;
    }

    private static void addIdentifier(List<Identifier> described, Lexeme lexeme, String type) throws AlreadyDefinedException {
        Identifier identifier = new Identifier(lexeme.getNumber());
        identifier.setType(type);
        if(identifiers.contains(identifier))
            throw new AlreadyDefinedException();
        if(described.contains(identifier))
            throw new AlreadyDefinedException();
        described.add(identifier);

    }

    private static boolean notDescribedIdentifier() {
        return next.getTable() == 4;
    }

    private static boolean type(){
        return isNext(INTEGER) || isNext(REAL) || isNext(BOOLEAN);
    }

    private static boolean operator() throws NotDefinedException, TypesMismatchException {
        return  assign() || condition() || fixedCycle() ||
                conditionalCycle() || input() || output() || complex();
    }

    private static boolean complex() throws NotDefinedException, TypesMismatchException {
        if (!operator())
            return false;
        while (isNext(":") || isNext("\\n")) {
            getNext();
            if (!operator())
                return false;
        }
        return true;
    }

    private static boolean assign() throws NotDefinedException, TypesMismatchException {
        if (!identifier())
            return false;
        String type = identifiers.get(next.getNumber()).getType();
        getNext();
        if (!isNext("ass"))
            return false;
        getNext();
        if(!expression())
            return false;
        checkAssignType(type, expressionStack.pop());
        return true;
    }

    private static void checkAssignType(String left, String right) throws TypesMismatchException {
        if(left.equals(BOOLEAN) && !right.equals(BOOLEAN))
            throw new TypesMismatchException();
        if(left.equals(INTEGER) && !right.equals(INTEGER))
            throw new TypesMismatchException();
        if(left.equals(REAL) && right.equals(BOOLEAN))
            throw new TypesMismatchException();
    }

    private static boolean condition() throws NotDefinedException, TypesMismatchException {
        if (!isNext("if"))
            return false;
        getNext();
        if (!expression())
            return false;
        if(!expressionStack.pop().equals(BOOLEAN))
            throw new TypesMismatchException();
        if (!complex())
            return false;
        if (isNext("else")) {
            getNext();
            return complex();
        }
        return true;
    }

    private static boolean fixedCycle() throws NotDefinedException, TypesMismatchException {
        if (!isNext("for"))
            return false;
        getNext();
        if (!assign())
            return false;
        if (!isNext("to"))
            return false;
        getNext();
        if (!expression())
            return false;
        if(!expressionStack.pop().equals(BOOLEAN))
            throw new TypesMismatchException();
        if(!isNext("do"))
            return false;
        getNext();
        return complex();
    }

    private static boolean conditionalCycle() throws NotDefinedException, TypesMismatchException {
        if (!isNext("while"))
            return false;
        getNext();
        if (!expression())
            return false;
        if(!expressionStack.pop().equals(BOOLEAN))
            throw new TypesMismatchException();
        if (!isNext("do"))
            return false;
        getNext();
        return complex();
    }

    private static boolean input() throws NotDefinedException {
        if (!isNext("read"))
            return false;
        getNext();
        if (!isNext("("))
            return false;
        getNext();
        if (!identifier())
            return false;
        getNext();
        while (isNext(",")) {
            getNext();
            if (!identifier())
                return false;
            getNext();
        }
        if(!isNext(")"))
            return false;
        getNext();
        return true;
    }

    private static boolean output() throws NotDefinedException, TypesMismatchException {
        if (!isNext("write"))
            return false;
        getNext();
        if (!isNext("("))
            return false;
        getNext();
        if (!expression())
            return false;
        while (isNext(",")) {
            getNext();
            if (!expression())
                return false;
            getNext();
        }
        if(!isNext(")"))
            return false;
        getNext();
        return true;
    }

    private static boolean identifier() throws NotDefinedException {
        if(next.getTable() != 4)
            return false;
        if(!identifiers.contains(new Identifier(next.getNumber())))
            throw new NotDefinedException();
        pushIdentifier();
        return true;
    }

    private static Stack<String> expressionStack = new Stack<>();

    private static boolean expression() throws NotDefinedException, TypesMismatchException {
        expressionStack.clear();
        if (!operand())
            return false;
        while (relationshipOperation()) {
            getNext();
            if (!operand())
                return false;
        }
        checkTypes();
        return true;
    }

    private static void checkTypes() throws TypesMismatchException {
        while(expressionStack.size() > 1){
            String operand2 = expressionStack.pop();
            String operation = expressionStack.pop();
            if(operation.equals("not")){
                checkUnary(operand2);
            } else {
                String operand1 = expressionStack.pop();
                checkOperation(operand2, operation, operand1);
            }
        }
    }

    private static final String BOOLEAN = "$";
    private static final String INTEGER = "%";
    private static final String REAL = "!";

    private static void checkOperation(String operand2, String operation, String operand1) throws TypesMismatchException {
        if(operation.equals("+") || operation.equals("-") || operation.equals("*") || operation.equals("/")){
            if(operand1.equals(BOOLEAN) || operand2.equals(BOOLEAN)){
                throw new TypesMismatchException();
            }
            if(operand1.equals(REAL) || operand2.equals(REAL)){
                expressionStack.push(REAL);
            } else {
                expressionStack.push(INTEGER);
            }
        } else if(operation.equals("or") || operation.equals("and")) {
            if(!operand1.equals(BOOLEAN) || !operand2.equals(BOOLEAN)){
                throw new TypesMismatchException();
            }
            expressionStack.push(BOOLEAN);
        } else {
            if(operand1.equals(BOOLEAN) || operand2.equals(BOOLEAN)){
                throw new TypesMismatchException();
            }
            expressionStack.push(BOOLEAN);
        }
    }

    private static void checkUnary(String operand) throws TypesMismatchException {
        if(operand.equals(BOOLEAN)){
            expressionStack.push(BOOLEAN);
        } else {
            throw new TypesMismatchException();
        }
    }

    private static boolean operand() throws NotDefinedException, TypesMismatchException {
        if (!term())
            return false;
        while (additionOperation()) {
            getNext();
            if (!term())
                return false;
        }
        return true;
    }

    private static boolean term() throws NotDefinedException, TypesMismatchException {
        if (!multiplier())
            return false;
        getNext();
        while (multiplicationOperation()) {
            getNext();
            if (!multiplier())
                return false;
            getNext();
        }
        return true;
    }

    private static boolean multiplier() throws NotDefinedException, TypesMismatchException {
        if (unary()) {
            getNext();
            return multiplier();
        } else if (isNext("(")) {
            getNext();
            if (!expression())
                return false;
            return isNext(")");
        } else if(identifier() || number() || logical()){
            return true;
        } else {
            return false;
        }
    }

    private static boolean number() {
        if(next.getTable() == 3){
            pushNumber();
            return true;
        } else {
            return false;
        }
    }

    private static boolean logical() {
        if(isNext("true") || isNext("false")){
            pushLogical();
            return true;
        } else {
            return false;
        }
    }

    private static boolean unary() {
        if(isNext("not")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean relationshipOperation() {
        if(isNext("<>") || isNext("=") || isNext("<") ||
                isNext("<=") || isNext(">") || isNext(">=")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean additionOperation() {
        if(isNext("+") || isNext("-") || isNext("or")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean multiplicationOperation() {
        if(isNext("*") || isNext("/") || isNext("and")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static void pushOperation(){
        expressionStack.push(delimiters.get(next.getNumber()));
    }

    private static void pushNumber(){
        String number = numbers.get(next.getNumber());
        try{
            Integer.parseInt(number);
            expressionStack.push(INTEGER);
        } catch (NumberFormatException e){
            expressionStack.push(REAL);
        }
    }

    private static void pushIdentifier(){
        expressionStack.push(identifiers.get(next.getNumber()).getType());
    }

    private static void pushLogical(){
        expressionStack.push(BOOLEAN);
    }

    private static void getNext() {
            column++;
            next = lexemeInput.getLexeme();
    }
}
