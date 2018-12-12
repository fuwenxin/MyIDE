package Suanfu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Pretreat {
    String path;
    String Z;
    HashSet Vt;
    HashSet Vn;
    HashSet V;
    HashMap grammar;
    public Pretreat(String filePath){
        path = filePath;
        initPre();
        createGrammar();
    }

    void initPre(){
        grammar = new <String,String>HashMap();
        Vt = new <String>HashSet();
        Vn = new <String>HashSet();
        V = new <String>HashSet();
    }

    void createGrammar(){
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader);

            String str = "";
            boolean is_haveZ = false;
            while ((str = br.readLine()) != null){
                String []strings = str.split("->");
                if(strings.length != 2){
                    //  错误
                    return;
                }
                if(!is_haveZ){
                    Z = strings[0];
                    is_haveZ = true;
                }
                Vn.add(strings[0]);
                V.add(strings[0]);
                for(int i=0;i<strings[1].length();++i){
                    V.add(strings[1].charAt(i) + "");
                }
                String []right = strings[1].split("[|]");
                for(int i=0;i<right.length;++i){
                    grammar.put(right[i],strings[0]);
                }
            }
            for(Object obj:V){
                if(!Vn.contains(obj)){
                    Vt.add(obj);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
