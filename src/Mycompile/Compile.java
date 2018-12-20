package Mycompile;
import Lexer.*;
import Table.InfoSym;
import Table.Tab;
import Table.Tablist;
import Table.Tag;

public class Compile{
    Buffer token_buf;
    Tablist tab_list;
    public Compile(){
        token_buf = new Buffer();
    }

    // 递归向下子程序 ---------------------------------------------------------------------------------------------------
    // ＜程序＞    ::= ［＜常量说明部分＞］［＜变量说明部分＞］
    //					{＜有返回值函数定义部分＞|＜无返回值函数定义部分＞}
    //					＜主函数＞
    void program_entry(){
        //TODO -  write_headsge() 如果有时间写x86可以使用到
        System.out.println("<程序>");
        tab_list = new Tablist(new Tab(null,0));
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            const_description_proc();
        }
        if ((token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(";"))||
                (token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(","))){
            var_description_proc();
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
        System.out.println("<常量声明子程序>");
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
    void var_description_proc(){
        System.out.println("<变量声明子程序>");
        var_define_proc();
        if (token_buf.get_buffer_value(0).equals(";")){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR 错误处理
        }
        while (token_buf.get_buffer_value(2).equals(";") || token_buf.get_buffer_value(2).equals(",")){
            var_define_proc();
            if (token_buf.get_buffer_value(0).equals(";")){
                token_buf.update_buffer();
            }
            else{
                // TODO - ERROR 错误处理
            }
        }
    }

    // ＜变量定义＞  ::= ＜类型标识符＞＜标识符＞{,＜标识符＞}
    void var_define_proc(){
        System.out.println("<变量定义>");
        int symKind = SymbolType.VAR;
        int symType = -1;
        int symLine = -1;
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
        if (symLine != -1)
            tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,symValue));

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
            if (symLine != -1)
                tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,symValue));
        }
    }

    // ＜常量定义＞   ::=   int＜标识符＞＝[+|-]＜整数＞{,＜标识符＞＝[+|-]＜整数＞}
    //						| float＜标识符＞＝[+|-]＜实数＞{,＜标识符＞＝[+|-]＜实数＞}
    //						| char＜标识符＞＝＜字符＞{,＜标识符＞＝＜字符＞}
    void const_define_proc(){
        System.out.println("<常量定义>");
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
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
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
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
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
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
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
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
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
                tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
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
                    tab_list.getCurTable().put(symName,new InfoSym(curLine,symKind,symType,symValue));
            }
        }
        else {
            // TODO - ERROR
        }
    }

    // ＜有返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void function_call_proc(){
        System.out.println("<有返回值函数调用语句>");
        int funcTable = -1;
        if (token_buf.get_buffer_type(0) == Tag.ID){
            InfoSym infoSym = tab_list.getCurTable().get(token_buf.get_buffer_value(0));
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

        if (funcTable != -1){
            tab_list.shiftBack();
        }
        // TODO - 函数调用处理和返回值的处理
    }

    // ＜无返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void process_call_proc(){
        System.out.println("<无返回值函数调用语句>");
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
        System.out.println("<有返回值函数定义部分>");
        StringBuffer symName = new StringBuffer();
        InfoSym infoSym = new InfoSym(-1,SymbolType.FUNC,-1, "");
        statement_head_proc(symName,infoSym);
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        infoSym.symValue = tab_list.addTable(tab_list.getCurTable()) + "";  // func 和 proc 的value存储的是其对应符号表的下标
        parameter_table_proc();
        // TODO - 对于参数可能还会有什么处理 ？？？ 2333

        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
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
        compound_statement_proc();

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
        System.out.println("<无返回值函数定义部分>");
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
        InfoSym infoSym = new InfoSym(token_buf.get_token_position(0),SymbolType.PROC,-1, "");
        infoSym.symValue = tab_list.addTable(tab_list.getCurTable()) + "";
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        parameter_table_proc();
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
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

        compound_statement_proc();
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
    void compound_statement_proc(){
        System.out.println("<复合语句>");
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            const_description_proc();
        }
        if ((token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(";"))||
                (token_buf.get_buffer_type(1) == Tag.ID && token_buf.get_buffer_value(2).equals(","))){
            var_description_proc();
        }
        statement_list_proc();
    }

    // <语句列>  ::= ＜语句＞｛＜语句＞｝
    void statement_list_proc(){
        System.out.println("<语句列>");
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
        System.out.println("<语句>");
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
        System.out.println("<条件语句>");
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
        condition_proc();
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else{
            // TODO - ERROR 错误处理
        }
        statement_proc();
        tab_list.frontable();

        if (token_buf.get_buffer_type(0) == Tag.ELSESYM){
            token_buf.update_buffer();
            statement_proc();
        }
    }

    // ＜条件＞  ::=  ＜表达式＞＜关系运算符＞＜表达式＞｜＜表达式＞ //表达式为0条件为假，否则为真
    void condition_proc(){
        System.out.println("<条件>");
        expression_proc();
        if (token_buf.get_buffer_type(0) == Tag.RELA){
            token_buf.update_buffer();

            expression_proc();
        }
        // TODO - 处理条件判断及结果
        return;
    }

    // ＜循环语句＞   ::=  while ‘(’＜条件＞‘)’＜语句＞
    void loop_statemnet_proc(){
        System.out.println("<循环语句>");
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
        // TODO - 条件处理。。。
        condition_proc();
        if (token_buf.get_buffer_value(0).equals(")")){
            token_buf.update_buffer();
        }
        else {
            // TODO - 错误处理
        }
        statement_proc();
    }

    // ＜赋值语句＞   ::=  ＜标识符＞＝＜表达式＞
    void assign_statemnet_proc(){
        System.out.println("<赋值语句>");
        String symName = "";
        if (token_buf.get_buffer_type(0) == Tag.ID){
            symName = token_buf.get_buffer_value(0);
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
        // TODO - 赋值的相关处理
        expression_proc();
    }

    // ＜声明头部＞   ::=  int＜标识符＞ |float ＜标识符＞|char＜标识符＞
    void statement_head_proc(StringBuffer symName, InfoSym infoSym){
        System.out.println("<声明头部>");
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
    void parameter_table_proc(){
        System.out.println("<参数表>");
        int symKind = SymbolType.PARA;
        int symType = -1;
        int symLine = -1;
        String symName = "";
        String symValue = "0";
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
            return;    // <空>
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
            tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,symValue));
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
                tab_list.getCurTable().put(symName,new InfoSym(symLine,symKind,symType,symValue));
            }
        }
    }

    // ＜表达式＞    ::= ［＋｜－］＜项＞{＜加法运算符＞＜项＞}
    void expression_proc(){
        System.out.println("<表达式>");
        boolean isNegative = false;
        InfoSym infoSym = new InfoSym(token_buf.get_token_position(0),-1,-1,"0");
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
            // infoSym = new InfoSym(token_buf.get_token_position(0),-1,-1,"0");
            token_buf.update_buffer();
            item_proc(infoSym);
        }
    }

    // ＜项＞  ::= ＜因子＞{＜乘法运算符＞＜因子＞}
    void item_proc(InfoSym infoSym){
        System.out.println("<项>");
        factor_proc();
        // TODO 可能要对乘法有些处理
        while (token_buf.get_buffer_value(0).equals("*") || token_buf.get_buffer_value(0).equals("/")){
            token_buf.update_buffer();
            factor_proc();
        }
    }

    // ＜因子＞    ::= ＜标识符＞｜‘(’＜表达式＞‘)’｜＜整数＞｜＜有返回值函数调用语句＞|＜实数＞|＜字符＞
    void factor_proc(){
        System.out.println("<因子>");
        if (token_buf.get_buffer_type(0) == Tag.ID){
            String symName = token_buf.get_buffer_value(0);
            InfoSym infoSym = tab_list.getCurTable().get(symName);

            // TODO - 完成infoSym的使用2333
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();
            expression_proc();
            if (token_buf.get_buffer_value(0).equals(")")){
                token_buf.update_buffer();
            }
            else{
                // TODO - ERROR 错误处理
            }
        }
        else if (token_buf.get_buffer_type(0) == Tag.NUM){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATNUM){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHAR){
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
        expression_proc();
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
        System.out.println("<值参数表>");
        if (token_buf.get_buffer_value(0).equals(")")){ // <空>
            return;
        }
        expression_proc();
        while (token_buf.get_buffer_value(0).equals(",")){
            token_buf.update_buffer();
            expression_proc();
        }
    }

    // ＜读语句＞    ::=  scanf ‘(’＜标识符＞{,＜标识符＞}‘)’
    void scanf_proc(){
        System.out.println("<读语句>");
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
        System.out.println("<写语句>");
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
        if (token_buf.get_buffer_type(0) == Tag.STRI){
            // TODO - 输出字符串
            token_buf.update_buffer();
            if (token_buf.get_buffer_value(0).equals(",")){
                token_buf.update_buffer();

                expression_proc();
            }
        }
        else if (token_buf.get_buffer_value(0).equals("+")
        || token_buf.get_buffer_value(0).equals("-")
        || token_buf.get_buffer_value(0).equals("(")
        || token_buf.get_buffer_type(0) == Tag.ID
        || token_buf.get_buffer_type(0) == Tag.CHAR){
            expression_proc();
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
        System.out.println("<返回语句>");
        if (token_buf.get_buffer_type(0) == Tag.RETURNSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理 ERROR
        }
        if (token_buf.get_buffer_value(0).equals("(")){
            token_buf.update_buffer();

            expression_proc();

            if (token_buf.get_buffer_value(0).equals(")")){
                token_buf.update_buffer();
            }
            else {
                // TODO - 错误处理 ERROR
            }
        }
        else{
            // TODO - 函数返回处理
            return;
        }
    }

    // ＜主函数＞    ::= void main‘(’‘)’‘{’＜复合语句＞‘}’
    void main_function_proc(){
        System.out.println("<主函数>");
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
        }
        else {
            // TODO - 错误处理
        }
        tab_list.addTable(tab_list.getCurTable());
        compound_statement_proc();
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




    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String []args){   // Test ------------------------
        Compile compile = new Compile();
        compile.program_entry();
    }
}
