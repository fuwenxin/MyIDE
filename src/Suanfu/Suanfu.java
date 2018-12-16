package Suanfu;

import javafx.util.Pair;
import Lexer.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class Suanfu {
    Lexer lexer;
    BuildTable bt;
    int stack_top;
    HashMap optTable;
    ArrayList<String> stack;
    public boolean is_OK;
    String cur_tok;
    int step;
    boolean is_continue;
    boolean is_start;
    public Suanfu(BuildTable buildTable,String path)throws Exception{
        bt = buildTable;
        initSuanfu(path);
    }

    void initSuanfu(String path)throws Exception{
        lexer = new Lexer(path);
        is_OK = false;
        step = 0;
        stack_top = -1;
        is_start = true;
        optTable = bt.table;
        stack = new ArrayList<>();
        is_continue = true;
        cur_tok = "";
    }

    public String reduce()throws Exception{
        if(is_start){
            is_start = false;
            return formatInfo("步骤","栈","优先关系","当前符号","动作");
        }
        step ++;
        String retStr = "";
        if (is_continue) {
            cur_tok = ((Word) lexer.getToken()).lexemne;
        }
        if(stack_top == -1){
            retStr = formatInfo(step + "",printStack(stack_top)," ",cur_tok,"移进");
            stack.add(++stack_top,cur_tok);
            is_continue = true;
        }
        else{
            String peek_tok = getVt(stack_top);
            int flag = JudgeFromTable(peek_tok,cur_tok);
            System.out.print("flag: " + flag + "\t");

            if (flag == -1){
                stack.add(++stack_top,cur_tok);                      // 移进，入栈
                retStr = formatInfo(step + "",printStack(stack_top),"<",cur_tok,"移进");
                is_continue = true;
            }
            else if(flag == 0){
                String str = cur_tok;
                retStr = formatInfo(step + "",printStack(stack_top),"=",cur_tok,"归约");
                for(int i = stack_top;stack_top >=0 && !stack.get(i).equals(peek_tok);i--){
                    str = stack.get(i) + str;
                    stack_top --;                                      // 弹栈
                }
                stack_top --;
                str = peek_tok + str;
                String rep = (String)bt.grammar1.get(str);
                if(rep != null){
                    stack.add(++stack_top,rep);
                    is_continue = true;
                }
                else{
                    is_OK = true;
                    return "Error";
                }
            }
            else if(flag == 1){
                String str = "";
                retStr = formatInfo(step + "",printStack(stack_top),">",cur_tok,"归约");
                for(int i = stack_top;i >=0;i--){
                    str = stack.get(i) + str;
                    stack_top --;                                      // 弹栈
                    if(bt.grammar1.containsKey(str)){
                        String rep = (String)bt.grammar1.get(str);
                        stack.add(++stack_top,rep);
                        System.out.println("归约 " + str + "->" + rep);
                        is_continue = false;
                        break;
                    }
                }
                if (stack_top == -1){
                    System.out.println("error");
                    is_OK = true;
                    return "Error";
                }
            }
            else if(flag == 2){
                is_OK = true;
                return "分析成功Success";
            }
            else{
                is_OK = true;
                return "Error";
            }
        }
        return retStr;
    }

    String formatInfo(String a,String b,String c,String d,String f){
        Formatter formatter = new Formatter();
        formatter.format("%10s\t%10s\t%10s\t%10s\t%10s\n",a,b,c,d,f);
        return formatter.toString();
    }

    String printStack(int top){
        String retStr = "";
        for(int i=0;i<=top;++i)
            retStr += (stack.get(i) + "");
        return retStr;
    }

    String getVt(int top){
        for(int i=top;i>=0;--i){
            if(bt.Vt.contains(stack.get(i))){
                return stack.get(i);
            }
        }
        return "#";
    }

    int JudgeFromTable(String a,String b){
        if(a.equals("#") && b.equals("#")){
            return 2;
        }
        else if(a.equals("#")){
            return -1;
        }
        else if(b.equals("#")){
            return 1;
        }
        else{
            Pair <String,String> pair = new Pair(a,b);
            if(optTable.containsKey(pair)){
                return (int)optTable.get(pair);
            }
            else
                return -2;
        }
    }
}
