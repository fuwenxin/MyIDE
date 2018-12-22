package Mycompile;
import Lexer.*;
import Table.InfoSym;
import Table.Tab;
import Table.Tablist;
import Table.Tag;

import javax.sound.sampled.Line;
import java.util.Formatter;
import java.util.Vector;

class Pcode{
    String p;
    String x;
    String y;
    Pcode(String pp,String xx,String yy){
        p = pp;
        x = xx;
        y = yy;
    }
}

public class Compile{
    Buffer token_buf;
    Tablist tab_list;
    int Pc;
    Vector pcode;
    public Compile(){
        token_buf = new Buffer();
        pcode = new Vector<Pcode>();
    }

    // 递归向下子程序 ---------------------------------------------------------------------------------------------------
    // ＜程序＞    ::= ［＜常量说明部分＞］［＜变量说明部分＞］
    //					{＜有返回值函数定义部分＞|＜无返回值函数定义部分＞}
    //					＜主函数＞
    void program_entry(){
        //TODO -  write_headsge() 如果有时间写x86可以使用到
        Pc = 0;
        ////System.out.println("<程序>");
        tab_list = new Tablist(new Tab(null,0));
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            const_description_proc();
        }
        if ((token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(";"))||
                (token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(","))){
            var_description_proc(0);
        }
        while (token_buf.get_buffer_type(1) == Tag.ID){
            if (token_buf.get_buffer_type(0) == Tag.INTSYM
                    || token_buf.get_buffer_type(0) == Tag.FLOATSYM
                    || token_buf.get_buffer_type(0) == Tag.CHARSYM){
                function_define_proc();
            }
            else if (token_buf.get_buffer_type(0) == Tag.VOIDSYM){
                process_define_proc();
            }
            else{
                // TODO - 错误处理 语法错误
            }
        }
        if ((token_buf.get_buffer_type(0) == Tag.VOIDSYM)
                && token_buf.get_buffer_type(1) == Tag.MAINSYM){

        }

        if(token_buf.get_buffer_type(0) != -1){
            // TODO - 错误处理 语法错误
        }
        main_function_proc();
    }

    // <常量声明子程序>::=  const＜常量定义＞;{ const＜常量定义＞;}
    void const_description_proc(){
        //System.out.println("<常量声明子程序>");
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - EORROR 语法错误处理
        }
        const_define_proc();
        if (token_buf.get_buffer_value(0).equals(";")){
            token_buf.update_buffer();
        }
        else {
            // TODO - EORROR 语法错误处理
        }
        while (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            token_buf.update_buffer();
            const_define_proc();
            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else {
                // TODO - EORROR 语法错误处理
            }
        }
    }

    // <变量声明子程序>::= ＜变量定义＞;{＜变量定义＞;}
    void var_description_proc(int numPara){
        //System.out.println("<变量声明子程序>");
        int varNum = 0;
        varNum += var_define_proc(numPara);
        if (token_buf.get_buffer_value(0).equals(";")){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        while (token_buf.get_buffer_value(2).equals(";") || token_buf.get_buffer_value(2).equals(",")){
            varNum += var_define_proc(numPara);
            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else{
                // TODO - ERROR 错误处理
            }
        }
        if (varNum != 0){
            _emit("INT","0",varNum + "");
        }
    }

    // ＜变量定义＞  ::= ＜类型标识符＞＜标识符＞{,＜标识符＞}
    int var_define_proc(int numPara){
        //System.out.println("<变量定义>");
        int symKind = SymbolType.VAR;
        int symType = -1;
        int symLine = -1;
        int varNum = 0;
        String symName = "";
        String symValue = "0";          // 变量的默认值为　０　
        if (token_buf.get_buffer_type(0) == Tag.INTSYM){
            symType = Tag.NUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
            symType = Tag.FLOATNUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
            symType = Tag.CHAR;
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            symLine = token_buf.get_token_position(0);
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        if (symLine != -1) {
            varNum ++;
            tab_list.getCurTable().put(symName, new InfoSym(symLine, symKind, symType,4 + numPara + varNum, symValue));
        }
        while (token_buf.get_buffer_value(0).equals(",")){
            symLine = -1;
            symName = "";
            token_buf.update_buffer();
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                symLine = token_buf.get_token_position(0);
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (symLine != -1){
                varNum ++;
                tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,4 + numPara + varNum,symValue));
            }
        }
        return varNum;
    }

    // ＜常量定义＞   ::=   int＜标识符＞＝[+|-]＜整数＞{,＜标识符＞＝[+|-]＜整数＞}
    //						| float＜标识符＞＝[+|-]＜实数＞{,＜标识符＞＝[+|-]＜实数＞}
    //						| char＜标识符＞＝＜字符＞{,＜标识符＞＝＜字符＞}
    void const_define_proc(){
        //System.out.println("<常量定义>");
        int symType ;
        int symKind = SymbolType.CONSTVAR;
        int curLine = -1;
        String symValue = "";
        String symName = "";
        boolean isNegative = false;
        if (token_buf.get_buffer_type(0) == Tag.INTSYM){
            token_buf.update_buffer();
            symType = Tag.NUM;
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_value(0).equals("=")){
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_value(0).equals("+")){
                token_buf.update_buffer();
            }
            else if (token_buf.get_buffer_value(0).equals("-")){
                isNegative = true;
                token_buf.update_buffer();
            }
            if (token_buf.get_buffer_type(0) == Tag.NUM){
                symValue = token_buf.get_buffer_value(0);
                curLine = token_buf.get_token_position(0);
                if (isNegative){
                    symValue = "-" + symValue;
                }
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if( curLine != -1)
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            while (token_buf.get_buffer_value(0).equals(",")){
                curLine = -1;
                symValue = "";
                symName = "";
                isNegative = false;
                token_buf.update_buffer();
                symType = Tag.NUM;
                if (token_buf.get_buffer_type(0) == Tag.ID){
                    symName = token_buf.get_buffer_value(0);
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_value(0).equals("=")){
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_value(0).equals("+")){
                    token_buf.update_buffer();
                }
                else if (token_buf.get_buffer_value(0).equals("-")){
                    isNegative = true;
                    token_buf.update_buffer();
                }
                if (token_buf.get_buffer_type(0) == Tag.NUM){
                    symValue = token_buf.get_buffer_value(0);
                    curLine = token_buf.get_token_position(0);
                    if (isNegative){
                        symValue = "-" + symValue;
                    }
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if( curLine != -1)
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
            token_buf.update_buffer();
            symType = Tag.FLOATNUM;
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_value(0).equals("=")){
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_value(0).equals("+")){
                token_buf.update_buffer();
            }
            else if (token_buf.get_buffer_value(0).equals("-")){
                isNegative = true;
                token_buf.update_buffer();
            }
            if (token_buf.get_buffer_value(0).equals("+")){
                token_buf.update_buffer();
            }
            if (token_buf.get_buffer_type(0) == Tag.FLOATNUM){
                symValue = token_buf.get_buffer_value(0);
                curLine = token_buf.get_token_position(0);
                if (isNegative){
                    symValue = "-" + symValue;
                }
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if( curLine != -1)
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            while (token_buf.get_buffer_value(0).equals(",")){
                curLine = -1;
                symValue = "";
                symName = "";
                token_buf.update_buffer();
                symType = Tag.FLOATNUM;
                if (token_buf.get_buffer_type(0) == Tag.ID){
                    symName = token_buf.get_buffer_value(0);
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_value(0).equals("=")){
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_value(0).equals("+")){
                    token_buf.update_buffer();
                }
                else if (token_buf.get_buffer_value(0).equals("-")){
                    isNegative = true;
                    token_buf.update_buffer();
                }
                if (token_buf.get_buffer_type(0) == Tag.FLOATNUM){
                    symValue = token_buf.get_buffer_value(0);
                    curLine = token_buf.get_token_position(0);
                    if (isNegative){
                        symValue = "-" + symValue;
                    }
                    token_buf.update_buffer();
                }
                else {

                }
                if( curLine != -1)
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
            token_buf.update_buffer();
            symType = Tag.CHAR;
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_value(0).equals("=")){
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_type(0) == Tag.CHAR){
                symValue = token_buf.get_buffer_value(0);
                curLine = token_buf.get_token_position(0);
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if( curLine != -1)
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            while (token_buf.get_buffer_value(0).equals(",")){
                curLine = -1;
                symValue = "";
                symName = "";
                token_buf.update_buffer();
                symType = Tag.CHAR;
                if (token_buf.get_buffer_type(0) == Tag.ID){
                    symName = token_buf.get_buffer_value(0);
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_value(0).equals("=")){
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if (token_buf.get_buffer_type(0) == Tag.CHAR){
                    symValue = token_buf.get_buffer_value(0);
                    curLine = token_buf.get_token_position(0);
                    token_buf.update_buffer();
                }
                else {
                    // TODO - ERROR 错误处理
                }
                if( curLine != -1)
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,-1,symValue));
            }
        }
        else {
            // TODO - ERROR
        }
    }

    // ＜有返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void function_call_proc(){
        //System.out.println("<有返回值函数调用语句>");
        int funcTable = -1;
        InfoSym infoSym = null;
        String symName = "";
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            infoSym = tab_list.getCurTable().get(symName);
            funcTable = Integer.parseInt(infoSym.symValue);
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (funcTable != -1) {
            tab_list.shiftTab(funcTable);
            value_parameter_table_proc();
        }
        else{
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理 ERROR
        }
        if (infoSym != null){
            int layer = tab_list.getCurTable().getSymLayer(symName);
            _emit("CAL",layer + "",infoSym.relatedPos + "");
        }
        if (funcTable != -1){
            tab_list.shiftBack();
        }
        // TODO - 函数调用处理和返回值的处理
    }

    // ＜无返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void process_call_proc(){
        //System.out.println("<无返回值函数调用语句>");
        if (token_buf.get_buffer_type(0) == Tag.ID){

            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        value_parameter_table_proc();
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理 ERROR
        }

        // TODO - 过程调用处理

    }

    // ＜有返回值函数定义部分＞  ::=  ＜声明头部＞‘(’＜参数表＞‘)’‘{’＜复合语句＞‘}’
    void function_define_proc(){
        //System.out.println("<有返回值函数定义部分>");
        StringBuffer symName = new StringBuffer();
        InfoSym infoSym = new InfoSym(-1,SymbolType.FUNC,-1,Pc +1, "");
        statement_head_proc(symName,infoSym);
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        infoSym.symValue = tab_list.addTable(tab_list.getCurTable()) + "";  // func 和 proc 的value存储的是其对应符号表的下标
        int numPara = parameter_table_proc();
        // TODO - 对于参数可能还会有什么处理 ？？？ 2333

        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
            _emit("MKS","0",numPara + "");
        }
        else{
            // TODO - ERROR 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("{")){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        compound_statement_proc(numPara);

        tab_list.frontable();

        tab_list.getCurTable().put(symName.toString(),infoSym);           // 将函数插入符号表中

        if (token_buf.get_buffer_value(0).equals("}")){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
    }

    // ＜无返回值函数定义部分＞  ::= void＜标识符＞‘(’＜参数表＞‘)’‘{’＜复合语句＞‘}’
    void process_define_proc(){
        //System.out.println("<无返回值函数定义部分>");
        if (token_buf.get_buffer_type(0) == Tag.VOIDSYM){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        String symName = "";
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        InfoSym infoSym = new InfoSym(token_buf.get_token_position(0),SymbolType.PROC,-1,Pc + 1, "");
        infoSym.symValue = tab_list.addTable(tab_list.getCurTable()) + "";
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        int numPara = parameter_table_proc();
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
            _emit("MKS","0",numPara+"");
        }
        else {
            // TODO - ERROR 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("{")){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }

        compound_statement_proc(numPara);
        tab_list.frontable();
        tab_list.getCurTable().put(symName,infoSym);
        if (token_buf.get_buffer_value(0).equals("}")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
    }

    // <复合语句>  ::=  ［＜常量说明部分＞］［＜变量说明部分＞］＜语句列＞
    void compound_statement_proc(int numPara){
        //System.out.println("<复合语句>");
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            const_description_proc();
        }
        if ((token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(";"))||
                (token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(","))){
            var_description_proc(numPara);
        }
        statement_list_proc();
    }

    // <语句列>  ::= ＜语句＞｛＜语句＞｝
    void statement_list_proc(){
        //System.out.println("<语句列>");
        statement_proc();
        while (token_buf.get_buffer_type(0) == Tag.IFSYM
        || token_buf.get_buffer_type(0) == Tag.WHILESYM
        || token_buf.get_buffer_value(0).equals("{")
        || token_buf.get_buffer_type(0) == Tag.ID && token_buf.get_buffer_value(1).equals("(")
        || token_buf.get_buffer_type(0) == Tag.ID && token_buf.get_buffer_value(1).equals("=")
        || token_buf.get_buffer_type(0) == Tag.SCANFSYM
        || token_buf.get_buffer_type(0) == Tag.PRINTFSYM
        || token_buf.get_buffer_type(0) == Tag.SWITCHSYM
        || token_buf.get_buffer_type(0) == Tag.RETURNSYM)
            statement_proc();
        // TODO - 此处应该会生成 P-code
    }


    //＜语句＞    ::= ＜条件语句＞｜＜循环语句＞| ‘{’＜语句列＞‘}’｜＜有返回值函数调用语句＞;|＜无返回值函数调用语句＞;
    //			｜＜赋值语句＞;｜＜读语句＞;｜＜写语句＞;｜＜空＞
    //			 |＜情况语句＞｜＜返回语句>
    void statement_proc(){
        //System.out.println("<语句>");
        if (token_buf.get_buffer_type(0) == Tag.CASESYM
        || token_buf.get_buffer_value(0).equals("}")
        || token_buf.get_buffer_type(0) == Tag.DEFAULTSYM){
            // TODO - 不知道要干个啥 ORZ
        }
        else if (token_buf.get_buffer_type(0) == Tag.IFSYM){
            tab_list.addTable(tab_list.getCurTable());
            if_statement_proc();
            tab_list.frontable();
        }
        else if (token_buf.get_buffer_type(0) == Tag.WHILESYM){
            tab_list.addTable(tab_list.getCurTable());
            loop_statemnet_proc();
            tab_list.frontable();
        }
        else if (token_buf.get_buffer_value(0).equals("{")){
            token_buf.update_buffer();
            tab_list.addTable(tab_list.getCurTable());
            statement_list_proc();
            tab_list.frontable();
            if (token_buf.get_buffer_value(0).equals("}")){
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.ID && token_buf.get_buffer_value(1).equals("(")){
            InfoSym infoSym;
            if ((infoSym = tab_list.getCurTable().get(token_buf.get_buffer_value(0))) == null){
                // TODO - ERROR 未知函数调用
            }
            else if (infoSym.symbolKind == SymbolType.FUNC){
                function_call_proc();
                // TODO - 函数调用时还会做些什么处理。。。
            }
            else if (infoSym.symbolKind == SymbolType.PROC){
                process_call_proc();
                // TODO - 过程调用时还会做些什么处理。。。
            }
            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else {
                // TODO - 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.ID && token_buf.get_buffer_value(1).equals("=")){
            assign_statemnet_proc();
            // TODO - 赋值相关的P-code~~~

            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else {
                // TODO - 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.SCANFSYM){
            scanf_proc();
            // TODO - scanf 输入相关

            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else{
                // TODO - 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.PRINTFSYM){
            printf_proc();
            // TODO - printf 输出相关

            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else{
                // TODO - 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.SWITCHSYM){
            switch_proc();
            // TODO - == 可能不会用到了2333
        }
        else if (token_buf.get_buffer_type(0) == Tag.RETURNSYM){
            return_proc();
            // TODO - 函数返回前的奇妙操作 ~~
            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
        }
        else {
            // TODO - 诶嘿嘿 错误处理
        }
    }

    // <条件语句> ::=  if ‘(’＜条件＞‘)’＜语句＞[else＜语句＞]
    void if_statement_proc(){
        //System.out.println("<条件语句>");
        if (token_buf.get_buffer_type(0) == Tag.IFSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        tab_list.addTable(tab_list.getCurTable());
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        condition_proc(infoSym);
        int loc_back1 = pcode.size();
        _emit("JPC","0","-1");
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        statement_proc();
        ((Pcode)pcode.get(loc_back1)).y = ((Pc+1) + "");
        tab_list.frontable();
        //JPC x,y
        //有条件跳转 t=t-1 if S(t-1) == 0 ip = y
        if (token_buf.get_buffer_type(0) == Tag.ELSESYM){
            token_buf.update_buffer();
            statement_proc();
        }
    }

    // ＜条件＞  ::=  ＜表达式＞＜关系运算符＞＜表达式＞｜＜表达式＞ //表达式为0条件为假，否则为真
    void condition_proc(InfoSym infoSym){
        //System.out.println("<条件>");
        expression_proc(infoSym);
        if (token_buf.get_buffer_type(0) == Tag.RELA){
            String opt = token_buf.get_buffer_value(0);
            token_buf.update_buffer();
            expression_proc(infoSym);
            if (infoSym.symbolType == Tag.FLOATNUM){
                _oprf(opt);
            }
            else{
                _opr(opt);
            }
        }
        // TODO - 处理条件判断及结果
        return;
    }

    // ＜循环语句＞   ::=  while ‘(’＜条件＞‘)’＜语句＞
    void loop_statemnet_proc(){
        //System.out.println("<循环语句>");
        int startPos = Pc + 1;
        if (token_buf.get_buffer_type(0) == Tag.WHILESYM){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        condition_proc(infoSym);
        int backPos = pcode.size();
        _emit("JPC","0","-1");
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理
        }
        statement_proc();
        _emit("JMP","0",startPos + "");
        ((Pcode)pcode.get(backPos)).y = ((Pc + 1) + "");
    }

    // ＜赋值语句＞   ::=  ＜标识符＞＝＜表达式＞
    void assign_statemnet_proc(){
        //System.out.println("<赋值语句>");
        String symName = "";
        InfoSym infoSym = null;
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            infoSym = tab_list.getCurTable().get(symName);
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("=")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理 ERROR
        }
        InfoSym infoSym1 = new InfoSym(-1,-1,-1,-1,"0");
        expression_proc(infoSym1);
        if (infoSym != null){
            int layer = tab_list.getCurTable().getSymLayer(symName);
            if (infoSym1.symbolType == Tag.FLOATNUM
                    && (infoSym.symbolType == Tag.CHAR || infoSym.symbolType == Tag.NUM))
                _emit("FIT","0","0");
            _emit("STO",layer + "",infoSym.relatedPos + "");
        }
    }

    // ＜声明头部＞   ::=  int＜标识符＞ |float ＜标识符＞|char＜标识符＞
    void statement_head_proc(StringBuffer symName, InfoSym infoSym){
        //System.out.println("<声明头部>");
        infoSym.lineNum = token_buf.get_token_position(0);
        if (token_buf.get_buffer_type(0) == Tag.INTSYM){
            infoSym.symbolType = Tag.NUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
            infoSym.symbolType = Tag.FLOATNUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
            infoSym.symbolType = Tag.CHAR;
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName.append(token_buf.get_buffer_value(0));
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
    }


    // ＜参数表＞    ::=  ＜类型标识符＞＜标识符＞{,＜类型标识符＞＜标识符＞} | ＜空＞
    int parameter_table_proc(){
        //System.out.println("<参数表>");
        int symKind = SymbolType.PARA;
        int symType = -1;
        int symLine = -1;
        String symName = "";
        String symValue = "0";
        int numPara = 0;
        if (token_buf.get_buffer_type(0) == Tag.INTSYM){
            symType = Tag.NUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
            symType = Tag.FLOATNUM;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
            symType = Tag.CHAR;
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_value(0).equals(")")){
            return 0;    // <空>
        }
        else {
            // TODO - ERROR　错误处理
        }
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            symLine = token_buf.get_token_position(0);
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        if (symLine != -1){
            numPara ++ ;
            tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,4 + numPara,symValue));
        }
        while (token_buf.get_buffer_value(0).equals(",")){
            token_buf.update_buffer();
            symType = -1;
            symLine = -1;
            symName = "";
            if (token_buf.get_buffer_type(0) == Tag.INTSYM){
                symType = Tag.NUM;
                token_buf.update_buffer();
            }
            else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
                symType = Tag.FLOATNUM;
                token_buf.update_buffer();
            }
            else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
                symType = Tag.CHAR;
                token_buf.update_buffer();
            }
            else {
                // TODO - ERROR 错误处理
            }
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                symLine = token_buf.get_token_position(0);
                token_buf.update_buffer();
            }
            else{
                // TODO - ERROR 错误处理
            }
            if (symLine != -1){
                numPara ++;
                tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,numPara + 4,symValue));
            }
        }
        return numPara;
    }

    // ＜表达式＞    ::= ［＋｜－］＜项＞{＜加法运算符＞＜项＞}
    void expression_proc(InfoSym infoSym){
        //System.out.println("<表达式>");
        boolean isNegative = false;
        if (token_buf.get_buffer_value(0).equals("+")){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_value(0).equals("-")){
            isNegative = true;
            token_buf.update_buffer();
        }
        item_proc(infoSym);
        // TODO - 类型转换~~

        while (token_buf.get_buffer_value(0).equals("+") || token_buf.get_buffer_value(0).equals("-")){
            String opt = token_buf.get_buffer_value(0);
            token_buf.update_buffer();
            item_proc(infoSym);
            if (infoSym.symbolType == Tag.FLOATNUM){
                _oprf(opt);
            }
            else {
                _opr(opt);
            }
        }
    }

    // ＜项＞  ::= ＜因子＞{＜乘法运算符＞＜因子＞}
    void item_proc(InfoSym infoSym){
        //System.out.println("<项>");
        factor_proc(infoSym);
        // TODO 可能要对乘法有些处理
        while (token_buf.get_buffer_value(0).equals("*") || token_buf.get_buffer_value(0).equals("/")){
            String opt = token_buf.get_buffer_value(0);
            token_buf.update_buffer();
            factor_proc(infoSym);
            if (infoSym.symbolType == Tag.FLOATNUM){
                _oprf(opt);
            }
            else {
                _opr(opt);
            }
        }
    }

    // ＜因子＞    ::= ＜标识符＞｜‘(’＜表达式＞‘)’｜＜整数＞｜＜有返回值函数调用语句＞|＜实数＞|＜字符＞
    void factor_proc(InfoSym sym){
        //System.out.println("<因子>");
        if (token_buf.get_buffer_type(0) == Tag.ID && !token_buf.get_buffer_value(1).equals("(")){
            String symName = token_buf.get_buffer_value(0);
            InfoSym infoSym = tab_list.getCurTable().get(symName);
            if (infoSym != null) {
                if (infoSym.symbolKind == SymbolType.VAR || infoSym.symbolKind == SymbolType.PARA)
                    sym.symbolType = _lod_type(sym.symbolType, infoSym.symbolType ,
                        tab_list.getCurTable().getSymLayer(symName) + "",infoSym.relatedPos + "");
                else if (infoSym.symbolKind == SymbolType.CONSTVAR){
                    sym.symbolType = _lit_type(sym.symbolType, infoSym.symbolType,infoSym.symValue);
                }
            }
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
            expression_proc(sym);
            if (token_buf.get_buffer_value(0).equals(")")){
                token_buf.update_buffer();
            }
            else{
                // TODO - ERROR 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.NUM){
            sym.symbolType = _lit_type(sym.symbolType, Tag.NUM ,token_buf.get_buffer_value(0));
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATNUM){
            sym.symbolType = _lit_type(sym.symbolType, Tag.FLOATNUM ,token_buf.get_buffer_value(0));
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHAR){
            sym.symbolType = _lit_type(sym.symbolType, Tag.CHAR ,token_buf.get_buffer_value(0));
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.ID && token_buf.get_buffer_value(1).equals("(")){
            function_call_proc();
        }
    }

    // ＜情况语句＞  ::=  switch ‘(’＜表达式＞‘)’ ‘{’＜情况表＞＜缺省＞ ‘}’
    void switch_statement_proc(){
        if (token_buf.get_buffer_type(0) == Tag.SWITCHSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        expression_proc(infoSym);
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else{
            // TODO -　错误处理
        }
        if (token_buf.get_buffer_value(0).equals("{")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        switch_table_proc();
        default_proc();
        if (token_buf.get_buffer_value(0).equals("}")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
    }

    //＜情况表＞   ::=  ＜情况子语句＞{＜情况子语句＞}
    void switch_table_proc(){
        switch_proc();

        while (token_buf.get_buffer_type(0) == Tag.CASESYM){
            switch_proc();
        }
    }

    // ＜情况子语句＞  ::=  case＜常量＞：＜语句＞
    void switch_proc(){
        if (token_buf.get_buffer_type(0) == Tag.CASESYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理　ERROR
        }
        if (token_buf.get_buffer_type(0) == Tag.NUM
                || token_buf.get_buffer_type(0) == Tag.CHAR){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.ID){
            InfoSym infoSym = tab_list.getCurTable().get(token_buf.get_buffer_value(0));
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理　ERROR
        }
        if (token_buf.get_buffer_value(0).equals(":")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        statement_proc();
    }

    // ＜缺省＞   ::=  default : ＜语句＞|＜空＞
    void default_proc(){
        if (token_buf.get_buffer_type(0) == Tag.DEFAULTSYM){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_value(0).equals("}")){
            return;
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals(":")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        statement_proc();

    }

    // ＜值参数表＞ ::= ＜表达式＞{,＜表达式＞}｜＜空＞
    void value_parameter_table_proc(){
        //System.out.println("<值参数表>");
        if (token_buf.get_buffer_value(0).equals(")")){ // <空>
            return;
        }
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        expression_proc(infoSym);
        while (token_buf.get_buffer_value(0).equals(",")){
            token_buf.update_buffer();
            infoSym = new InfoSym(-1,-1,-1,-1,"0");
            expression_proc(infoSym);
        }
    }

    // ＜读语句＞    ::=  scanf ‘(’＜标识符＞{,＜标识符＞}‘)’
    void scanf_proc(){
        //System.out.println("<读语句>");
        if (token_buf.get_buffer_type(0) == Tag.SCANFSYM){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        String symName = "";
        InfoSym infoSym;
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
            infoSym = tab_list.getCurTable().get(symName);
            // TODO - 读入操作  特定指令读入 2333 更新 infoSym 并加入且更新符号表、运行栈
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理 ERROR
        }
        while (token_buf.get_buffer_value(0).equals(",")){
            token_buf.update_buffer();
            if (token_buf.get_buffer_type(0) == Tag.ID){
                symName = token_buf.get_buffer_value(0);
                infoSym = tab_list.getCurTable().get(symName);
                // TODO - 读入操作  特定指令读入 2333 更新 infoSym 并加入且更新符号表、运行栈
                token_buf.update_buffer();
            }
            else {
                // TODO - 错误处理 ERROR
            }
        }
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
    }

    // ＜写语句＞    ::= printf ‘(’ ＜字符串＞,＜表达式＞ ‘)’| printf ‘(’＜字符串＞ ‘)’|printf ‘(’＜表达式＞‘)’
    void printf_proc(){
        //System.out.println("<写语句>");
        if (token_buf.get_buffer_type(0) == Tag.PRINTFSYM){
            token_buf.update_buffer();
        }
        else {
            // TODO -  错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        if (token_buf.get_buffer_type(0) == Tag.STRI){
            // TODO - 输出字符串
            token_buf.update_buffer();
            if (token_buf.get_buffer_value(0).equals(",")){
                token_buf.update_buffer();
                expression_proc(infoSym);
            }
        }
        else if (token_buf.get_buffer_value(0).equals("+")
        || token_buf.get_buffer_value(0).equals("-")
        || token_buf.get_buffer_value(0).equals("(")
        || token_buf.get_buffer_type(0) == Tag.ID
        || token_buf.get_buffer_type(0) == Tag.CHAR){
            expression_proc(infoSym);
        }

        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
    }

    // ＜返回语句＞   ::=  return[‘(’＜表达式＞‘)’]
    void return_proc(){
        //System.out.println("<返回语句>");
        InfoSym infoSym = new InfoSym(-1,-1,-1,-1,"0");
        if (token_buf.get_buffer_type(0) == Tag.RETURNSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();

            expression_proc(infoSym);

            if (token_buf.get_buffer_value(0).equals(")")){
                token_buf.update_buffer();
            }
            else {
                // TODO - 错误处理 ERROR
            }
        }
        if (infoSym.symbolType == Tag.FLOATNUM)
            _oprf("RE");
        else
            _opr("RE");
    }

    // ＜主函数＞    ::= void main‘(’‘)’‘{’＜复合语句＞‘}’
    void main_function_proc(){
        //System.out.println("<主函数>");
        if (token_buf.get_buffer_type(0) == Tag.VOIDSYM || token_buf.get_buffer_type(0) == Tag.INTSYM){
            token_buf.update_buffer();
        }
        else{
            //TODO - 错误处理 语法错误
        }
        if (token_buf.get_buffer_type(0) == Tag.MAINSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 语法错误
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理
        }
        if (token_buf.get_buffer_value(0).equals("{")){
            token_buf.update_buffer();
            _emit("MKS","0","0");
        }
        else {
            // TODO - 错误处理
        }
        tab_list.addTable(tab_list.getCurTable());
        compound_statement_proc(0);
        if (token_buf.get_buffer_value(0).equals("}")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
        tab_list.frontable();
        // TODO - 解释程序等等 。。。


    }
    //----------------------------------------------------------------------------------------------------------


    void _opr(String opt){
        String y = "-1";
        switch (opt){
            case "RE":
                y = 0 + "";
                break;
            case "+":
                y = 2 + "";
                break;
            case "-":
                y = 3 + "";
                break;
            case "*":
                y = 4 + "";
                break;
            case "/":
                y = 5 + "";
                break;
            case "==":
                y = 8 + "";
                break;
            case "!=":
                y = 9 + "";
                break;
            case "<":
                y = 10 + "";
                break;
            case ">":
                y = 11 + "";
                break;
            case ">=":
                y = 12 + "";
                break;
            case "<=":
                y = 13 + "";
                break;
        }
        _emit("OPR","0",y);
    }

    int _lod_type(int leftType,int rightType,String layer,String pos){
        if (leftType == -1){
            _emit("LOD",layer,pos);
        }
        else if ((leftType == Tag.CHAR || leftType == Tag.NUM) && (rightType == Tag.FLOATNUM)){
            _emit("FTI","0","1");
            _emit("LOD",layer,pos);
            leftType = rightType;
        }
        else if (leftType == Tag.FLOATNUM && (rightType == Tag.CHAR || rightType == Tag.NUM)){
            _emit("LOD",layer,pos);
            _emit("FTI","0","1");
        }
        else {
            _emit("LOD",layer,pos);
        }
        return leftType;
    }

    int _lit_type(int leftType,int rightType,String value){
        if (leftType == -1){
            _emit("LIT","0",value);
            leftType = rightType;
        }
        else if ((leftType == Tag.CHAR || leftType == Tag.NUM) && (rightType == Tag.FLOATNUM)){
            _emit("FTI","0","1");
            _emit("LIT","0",value);
            leftType = rightType;
        }
        else if (leftType == Tag.FLOATNUM && (rightType == Tag.CHAR || rightType == Tag.NUM)){
            _emit("LIT","0",value);
            _emit("FTI","0","1");
        }
        else {
            _emit("LIT","0",value);
        }
        return leftType;
    }

    void _oprf(String opt){
        String y = "-1";
        switch (opt){
            case "RE":
                y = 0 + "";
                break;
            case "+":
                y = 2 + "";
                break;
            case "-":
                y = 3 + "";
                break;
            case "*":
                y = 4 + "";
                break;
            case "/":
                y = 5 + "";
                break;
            case "==":
                y = 8 + "";
                break;
            case "!=":
                y = 9 + "";
                break;
            case "<":
                y = 10 + "";
                break;
            case ">":
                y = 11 + "";
                break;
            case ">=":
                y = 12 + "";
                break;
            case "<=":
                y = 13 + "";
                break;
        }
        _emit("OPRF","0",y);
    }

    void _emit(String str,String x,String y){
        Pc ++;
        pcode.add(new Pcode(str,x,y));
    }

    void _printOut(){
        int num = 0;
        for (Object o:pcode){
            Pcode p = (Pcode)o;
            System.out.printf("%3d%6s%10s%10s\n",++num,p.p,p.x,p.y);
        }
    }

    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String []args){   // Test ------------------------
        Compile compile = new Compile();
        compile.program_entry();
        compile._printOut();
    }
}
