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

    private static Lexeme buffer;

    private static List<Identifier> identifiers;

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
                result = "Syntax error";
            }
        } catch (AlreadyDefinedException e){
            result = "Defined variable was defined again";
        } catch (NotDefinedException e){
            result = "Not defined variable was used";
        } catch (TypesMismatchException e){
            result = "Types mismatch";
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
            if (!(description() || operator())) {
                return false;
            }
            if (!isNext(";")) {
                return false;
            }
            getNext();
        } while (!isNext("}"));
        return true;
    }

    private static boolean description() throws AlreadyDefinedException {
        List<Identifier> described = new ArrayList<>();
        if (!notDescribedIdentifier())
            return false;
        buffer = next;
        getNext();
        if (isNext(":=")){
            Lexeme l = next;
            next = buffer;
            buffer = l;
            return false;
        }
        addIdentifier(described, buffer);
        buffer = null;
        while (isNext(",")) {
            getNext();
            if (!notDescribedIdentifier())
                return false;
            addIdentifier(described, next);
            getNext();
        }
        if (!isNext(":"))
            return false;
        getNext();
        if (!type())
            return false;
        for(Identifier identifier : described){
            identifier.setType(next.getNumber());
        }
        getNext();
        if(!isNext(";"))
            return false;
        identifiers.addAll(described);
        getNext();
        return true;
    }

    private static void addIdentifier(List<Identifier> described, Lexeme lexeme) throws AlreadyDefinedException {
        Identifier identifier = new Identifier(lexeme.getNumber());
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
        return isNext("integer") || isNext("real") || isNext("boolean");
    }

    private static boolean operator() throws NotDefinedException, TypesMismatchException {
        return complex() || assign() || condition() || fixedCycle() ||
                conditionalCycle() || input() || output();
    }

    private static boolean complex() throws NotDefinedException, TypesMismatchException {
        if (!isNext("begin"))
            return false;
        getNext();
        if (!operator())
            return false;
        while (isNext(";")) {
            getNext();
            if (!operator())
                return false;
        }
        if (!isNext("end"))
            return false;
        getNext();
        return true;
    }

    private static boolean assign() throws NotDefinedException, TypesMismatchException {
        if (!identifier())
            return false;
        String type = words.get(identifiers.get(next.getNumber()).getType());
        if(buffer != null){
            next = buffer;
            buffer = null;
        } else {
            getNext();
        }
        if (!isNext(":="))
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
        if (!isNext("("))
            return false;
        getNext();
        if (!expression())
            return false;
        if(!expressionStack.pop().equals(BOOLEAN))
            throw new TypesMismatchException();
        if (!isNext(")"))
            return false;
        getNext();
        if (!operator())
            return false;
        if (isNext("else")) {
            getNext();
            return operator();
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
        if (isNext("step")) {
            getNext();
            if (!expression()) {
                return false;
            }
        }
        if (!operator())
            return false;
        if(!isNext("next"))
            return false;
        getNext();
        return true;
    }

    private static boolean conditionalCycle() throws NotDefinedException, TypesMismatchException {
        if (!isNext("while"))
            return false;
        getNext();
        if (!isNext("("))
            return false;
        getNext();
        if (!expression())
            return false;
        if(!expressionStack.pop().equals(BOOLEAN))
            throw new TypesMismatchException();
        if (!isNext(")"))
            return false;
        getNext();
        return operator();
    }

    private static boolean input() throws NotDefinedException {
        if (!isNext("readln"))
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
        return true;
    }

    private static boolean output() throws NotDefinedException, TypesMismatchException {
        if (!isNext("writeln"))
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
            if(operation.equals("!")){
                checkUnary(operand2);
            } else {
                String operand1 = expressionStack.pop();
                checkOperation(operand2, operation, operand1);
            }
        }
    }

    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    private static final String REAL = "real";

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
        } else if(operation.equals("||") || operation.equals("&&")) {
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
        if(isNext("!")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean relationshipOperation() {
        if(isNext("!=") || isNext("==") || isNext("<") ||
                isNext("<=") || isNext(">") || isNext(">=")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean additionOperation() {
        if(isNext("+") || isNext("-") || isNext("||")){
            pushOperation();
            return true;
        } else {
            return false;
        }
    }

    private static boolean multiplicationOperation() {
        if(isNext("*") || isNext("/") || isNext("&&")){
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
        expressionStack.push(words.get(identifiers.get(next.getNumber()).getType()));
    }

    private static void pushLogical(){
        expressionStack.push(BOOLEAN);
    }

    private static void getNext() {
            next = lexemeInput.getLexeme();
    }
}
