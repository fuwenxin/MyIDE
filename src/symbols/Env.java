package symbols;
import lexer.Token;
/*
 * 用于存储标识符以及关键字
 *       词法分析其中未用到
 * */

import java.util.*;

public class Env {
    private Hashtable table;
    protected Env prev;

    public Env(Env p){
        table = new Hashtable();  prev = p;
    }

    public void put(String str, Token tok){
        table.put(str,tok);
    }

    public Token get(String str){
        for (Env e = this; e != null; e = e.prev){  // 从本语句块开始，逐个寻找目标关键字
            Token tok = (Token)e.table.get(str);
            if (tok != null) return tok;
        }
        return null;
    }
}
