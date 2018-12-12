package Suanfu;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class BuildTable {
    Stack stack;
    public String Z;
    public HashSet firstVt;
    public HashSet lastVt;
    public HashSet Vt;
    public HashSet Vn;
    public HashSet V;
    public HashMap grammar;
    public HashMap grammar1;
    public HashMap table;
    public boolean is_default;
    Pretreat pretreat;
    String path;

    public BuildTable(String pathName){
        path = pathName;
        if (path == null)
            is_default = true;
        else
            is_default = false;
        initGrammar();
        createTable();
    }

    void initGrammar() {
        Vt = new HashSet<String>();
        Vn = new HashSet<String>();
        V = new HashSet<String>();
        grammar = new HashMap<String, String>();
        grammar1 = new HashMap<String, String>();
        stack = new Stack<Pair<String, String>>();
        lastVt = new HashSet<Pair<String, String>>();
        firstVt = new HashSet<Pair<String, String>>();
        table = new HashMap<Pair<String, String>, Integer>();

        if (is_default) {
            Vt.add("i");
            Vt.add("+");
            Vt.add("(");
            Vt.add("*");
            Vt.add(")");
            Vn.add("E");
            Vn.add("T");
            Vn.add("F");
            Z = "E";
            grammar.put("E+T", "E");
            grammar.put("T", "E");
            grammar.put("T*F", "T");
            grammar.put("F", "T");
            grammar.put("(E)", "F");
            grammar.put("i", "F");
            replaceVn();
        } else {
            pretreat = new Pretreat(path);
            Vn = pretreat.Vn;
            Vt = pretreat.Vt;
            V = pretreat.V;
            Z = pretreat.Z;
            grammar = pretreat.grammar;
            replaceVn();
        }
    }


    void replaceVn(){
        for(Object key:grammar.keySet()){
            String right = (String)key;
            String left = (String)grammar.get(key);
            String rright = "";
            String lleft = "";
            for(int i=0;i<right.length();++i){
                if(Vn.contains(right.charAt(i) + "")){
                    rright += "N";
                }
                else
                    rright += (right.charAt(i) + "");
            }
            for(int i=0;i<left.length();++i){
                if(Vn.contains(left.charAt(i) + "")){
                    lleft += "N";
                }
                else
                    lleft += (left.charAt(i) + "");
            }
            if(!rright.equals(lleft))
                grammar1.put(rright,lleft);
        }
    }

    public HashMap createTable(){
        getFirstVt();
        getLastVt();
        for(Object key:grammar.keySet()){
            String right = (String)key;
            int len = right.length();
            for(int i=0;i<len-1;i++){
                if(Vt.contains(right.charAt(i) + "") && Vt.contains(right.charAt(i+1) + "")){
                    Pair<String,String> pair = new Pair<>(right.charAt(i) + "",right.charAt(i+1) + "");
                    table.put(pair,0);
                }
                if((i<len-2) && Vt.contains(right.charAt(i) + "") && Vt.contains(right.charAt(i+2) + "") && !Vt.contains(right.charAt(i+1) + "")){
                    Pair<String,String> pair = new Pair<>(right.charAt(i) + "",right.charAt(i+2) + "");
                    table.put(pair,0);
                }
                if(Vt.contains(right.charAt(i) + "") && !Vt.contains(right.charAt(i+1) + "")){
                    for(Object object:firstVt){
                        Pair<String,String> pair = (Pair<String,String>)object;
                        if(pair.getKey().compareTo(right.charAt(i+1) + "") == 0){
                            Pair<String,String> pair1 = new Pair(right.charAt(i) + "",pair.getValue());
                            table.put(pair1,-1);
                        }
                    }
                }
                if(!Vt.contains(right.charAt(i) + "") && !Vt.contains(right.charAt(i) + "")){
                    for(Object iter:lastVt){
                        Pair<String,String> pair = (Pair<String,String>)iter;
                        if(pair.getKey().compareTo(right.charAt(i) + "") == 0){
                            Pair<String,String> pair1 = new Pair(pair.getValue(),right.charAt(i+1) + "");
                            table.put(pair1,1);
                        }
                    }
                }
            }
        }
        return table;
    }

    void insertF(String U,String b){
        Pair pair = new Pair(U,b);
        if(!firstVt.contains(pair)){
            firstVt.add(pair);
            stack.push(pair);
        }
    }

    void insertL(String U,String b){
        Pair pair = new Pair(U,b);
        if(!lastVt.contains(pair)){
            lastVt.add(pair);
            stack.push(pair);
        }
    }

    void getFirstVt(){
        for(Object key:grammar.keySet()){
            String right = (String)key;
            if(Vt.contains(right.charAt(0)+"")){
                insertF((String)grammar.get(key),right.charAt(0) + "");
            }
            else if(right.length() >= 2 && Vt.contains(right.charAt(1)+"")){
                insertF((String)grammar.get(key),right.charAt(1) + "");
            }
        }
        while (!stack.empty()){
            Pair <String,String> pair= (Pair <String,String>)stack.peek();
            stack.pop();
            for(Object key:grammar.keySet()){
                String right = (String)key;              // V....
                String left = (String)grammar.get(key);  // U
                String V = pair.getKey();
                String b = pair.getValue();
                if(V.compareTo(right.charAt(0) + "") == 0){
                    insertF(left,b);
                }
            }
        }
    }

    void getLastVt(){
        for(Object key:grammar.keySet()){
            String right = (String)key;
            int len = right.length();
            if(Vt.contains(right.charAt(len-1)+"")){
                insertL((String)grammar.get(key),right.charAt(len-1) + "");
            }
            else if(right.length() >= 2 && Vt.contains(right.charAt(len-2)+"")){
                insertL((String)grammar.get(key),right.charAt(len-2) + "");
            }
        }
        while (!stack.empty()){
            Pair <String,String> pair = (Pair <String,String>)stack.peek();
            stack.pop();
            for(Object key:grammar.keySet()){
                String right = (String)key;
                int len = right.length();
                String left = (String)grammar.get(key);
                String V = pair.getKey();
                String b = pair.getValue();
                if(V.compareTo(right.charAt(len-1) + "") == 0){
                    insertL(left,b);
                }
            }
        }
    }
}
