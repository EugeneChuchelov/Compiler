package file;

import java.io.*;
import java.util.*;

public class Table {
    private Map<String, Integer> entries;
    private String path;
    private int number = 0;

  public   static int timesGet = 0;
    public static int timesLooked = 0;

    public Table(String path, int number) {
        this.path = path;
        entries = new HashMap<>(number);
    }

    public Table(String path) {
        this(path, 8);
    }

    public int look(StringBuilder s){
        timesLooked++;
        return entries.getOrDefault(s.toString(), -1);
    }

    public int look(String s){
        timesGet++;
        return entries.getOrDefault(s, -1);
    }

    public String get(int number){
        timesGet++;
        String key = "";
        for(Map.Entry<String, Integer> entry : entries.entrySet()){
            if(entry.getValue() == number)
                key = entry.getKey();
        }
        return key;
    }

    public int add(String s){
        if(!entries.containsKey(s)){
            entries.put(s, number++);
            return entries.size() - 1;
        }
        return entries.get(s);
    }

    public void out(){
        int totalCount = 0;
        for(String s : entries.keySet()){
            totalCount += s.length();
        }
        StringBuilder stringBuilder = new StringBuilder(totalCount + entries.size());
        for(String s : entries.keySet()){
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
            entries.put(scanner.nextLine(), number++);
        }
        scanner.close();
    }

    public boolean contains(char c){
        return entries.containsKey(String.valueOf(c));
    }
}
