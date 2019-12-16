package file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Table {
    private List<String> entries;
    private String path;

    public Table(String path, int number) {
        this.path = path;
        entries = new ArrayList<>(number);
    }

    public Table(String path) {
        this(path, 8);
    }

    public int look(StringBuilder s){
        return entries.indexOf(s.toString());
    }

    public int look(String s){
        return entries.indexOf(s);
    }

    public String get(int number){
        return entries.get(number);
    }

    public int add(String s){
        if(!entries.contains(s)){
            entries.add(s);
            return entries.size() - 1;
        }
        return entries.indexOf(s);
    }

    public void out(){
        int totalCount = 0;
        for(String s : entries){
            totalCount += s.length();
        }
        StringBuilder stringBuilder = new StringBuilder(totalCount + entries.size());
        for(String s : entries){
            stringBuilder.append(s);
            stringBuilder.append('\n');
        }
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load(){
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNext()){
            entries.add(scanner.nextLine());
        }
        scanner.close();
    }

    public boolean contains(char c){
        return entries.contains(String.valueOf(c));
    }
}
