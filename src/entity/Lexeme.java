package entity;

public class Lexeme {

    private int table;

    private int number;

    public Lexeme(int table, int number) {
        this.table = table;
        this.number = number;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
