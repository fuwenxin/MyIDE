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

    public Tab(Tab p){
        table = new Hashtable();  prev = p;          // 如果prev == null， 表示为全局变量
    }

    public void back(){
        // TODO -- 解决单向符号表可能gg的问题 (如何在运行找表)
    }

    public void put(String name, InfoSym sym){
        table.put(name,sym);
    }

    public InfoSym get(String name){
        for (Tab e = this; e != null; e = e.prev){  // 从本语句块开始，逐个寻找目标关键字
            InfoSym infoSym = (InfoSym) e.table.get(name);
            if (infoSym != null) return infoSym;
        }
        return null;
    }
}
