package Table;
import Lexer.Token;
/*
 * 用于存储标识符以及关键字
 *       词法分析其中未用到
 * */

import javax.sound.sampled.Line;
import java.util.*;


public class Tab {
    private Hashtable table;
    protected Tab prev;
    private int pos;

    public Tab(Tab p,int loc){
        table = new Hashtable();  prev = p;          // 如果prev == null， 表示为全局变量
        pos = loc;
    }

    public void put(String name, InfoSym sym){
        table.put(name,sym);
    }

    public void change(String name ,InfoSym sym){
        table.remove(name);
        table.put(name,sym);
    }

    public int getPos(){
        return pos;
    }

    public InfoSym get(String name){
        for (Tab e = this; e != null; e = e.prev){  // 从本语句块开始，逐个寻找目标关键字
            InfoSym infoSym = (InfoSym) e.table.get(name);
            if (infoSym != null) return infoSym;
        }
        return null;
    }

    public int getSymLayer(String name){
        int layer = 0;
        for (Tab e = this; e != null; e = e.prev,layer ++){  // 从本语句块开始，逐个寻找目标关键字
            InfoSym infoSym = (InfoSym) e.table.get(name);
            if (infoSym != null) return layer;
        }
        return -1;
    }

}
