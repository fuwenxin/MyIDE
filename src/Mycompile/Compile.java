package Mycompile;
import Lexer.*;
import Table.Tab;
import Table.Tag;

import java.util.Hashtable;

public class Compile{
    Buffer token_buf;
    Tab symbolTablel;
    public Compile(){
        token_buf = new Buffer();
    }



    // 递归向下子程序 ---------------------------------------------------------------------------------------------------
    // 子程序 <程序>＜程序＞    ::= ［＜常量说明部分＞］［＜变量说明部分＞］
    //					{＜有返回值函数定义部分＞|＜无返回值函数定义部分＞}
    //					＜主函数＞
    void program_entry(){
        //TODO -  write_headsge() 如果有时间写x86可以使用到
        symbolTablel = new Tab(null);
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            const_description_proc();
        }
        if ((token_buf.get_buffer_type(2) == Tag.ID && token_buf.get_buffer_vlaue(2).equals(";"))||
                (token_buf.get_buffer_type(2) == Tag.ID && token_buf.get_buffer_vlaue(2).equals(","))){
            var_description_proc();
        }
        while (token_buf.get_buffer_type(1) == Tag.ID){
            if (token_buf.get_buffer_type(0) == Tag.NUM
                    || token_buf.get_buffer_type(0) == Tag.FLOATNUM
                    || token_buf.get_buffer_type(0) == Tag.CHAR){
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
    }

    // <常量声明子程序>::=  const＜常量定义＞;{ const＜常量定义＞;}
    void const_description_proc(){
        //TODO - 处理常量声明子程序
        if (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            token_buf.update_buffer();
        }
        else{
            // TODO - EORROR 语法错误处理
        }
        const_define_proc();
        if (token_buf.get_buffer_vlaue(0).equals(";")){
            token_buf.update_buffer();
        }
        else {
            // TODO - EORROR 语法错误处理
        }
        while (token_buf.get_buffer_type(0) == Tag.CONSTSYM){
            token_buf.update_buffer();
            const_define_proc();
            if (token_buf.get_buffer_vlaue(0).equals(";")){
                token_buf.update_buffer();
            }
            else {
                // TODO - EORROR 语法错误处理
            }
        }
    }

    // <变量声明子程序>::= ＜变量定义＞;{＜变量定义＞;}
    void var_description_proc(){
        //TODO - 处理变量声明子程序
    }

    // ＜变量定义＞  ::= ＜类型标识符＞＜标识符＞{,＜标识符＞}
    void var_define_proc(){
        // TODO - 变量定义
    }

    // ＜常量定义＞   ::=   int＜标识符＞＝＜整数＞{,＜标识符＞＝＜整数＞}
    //						| float＜标识符＞＝＜实数＞{,＜标识符＞＝＜实数＞}
    //						| char＜标识符＞＝＜字符＞{,＜标识符＞＝＜字符＞}
    void const_define_proc(){
        // TODO - 常量定义

        if (token_buf.get_buffer_type(0) == Tag.INTSYM){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.FLOATSYM){
            token_buf.update_buffer();
        }
        else if (token_buf.get_buffer_type(0) == Tag.CHARSYM){
            token_buf.update_buffer();
        }
        else {
            // TODO - ERROR
        }
    }

    // ＜常量＞   ::=  ＜整数＞| ＜实数＞|＜字符＞
    void const_proc(){
        // TODO - 常量
    }

    //＜整数＞    ::= ［＋｜－］＜非零数字＞｛＜数字＞｝｜０
    void integer_proc(){
        // TODO - 整数
    }

    // ＜实数＞    ::= ［＋｜－］<整数>.[＜小数部分＞]
    void real_num_proc(){

    }

    // <函数声明>::= ＜标识符＞‘(’＜值参数表＞‘)’
    void function_define_proc(){
        //TODO - 处理函数声明
    }

    // <过程声明>::= ＜标识符＞‘(’＜值参数表＞‘)’
    void process_define_proc(){
        //TODO - 处理过程声明
    }

    // <复合语句>  ::=  ［＜常量说明部分＞］［＜变量说明部分＞］＜语句列＞
    void compound_statement_proc(){
        // TODO - 处理复合语句
    }

    // <语句列>  ::= ＜语句＞｛＜语句＞｝
    void statement_list_proc(){
        // TODO - 处理语句列
    }

    // <语句>
    void statement_proc(){
        // TODO - 处理语句
    }


    // <条件语句> ::=  if ‘(’＜条件＞‘)’＜语句＞
    void if_statement_proc(){
        // TODO - 处理条件语句
    }

    // ＜条件＞  ::=  ＜表达式＞＜关系运算符＞＜表达式＞｜＜表达式＞ //表达式为0条件为假，否则为真
    void if_proc(){
        // TODO - 处理条件
    }

    // ＜循环语句＞   ::=  while ‘(’＜条件＞‘)’＜语句＞
    void cycle_statemnet_proc(){
        // TODO - 处理循环语句
    }

    // ＜赋值语句＞   ::=  ＜标识符＞＝＜表达式＞
    void assign_statemnet_proc(){
        // TODO - 赋值语句
    }

    // ＜有返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void function_call_proc(){
        // TODO - 有返回值的函数调用语句
    }

    // ＜无返回值函数调用语句＞ ::= ＜标识符＞‘(’＜值参数表＞‘)’
    void process_call_proc(){
        // TODO - 无返回值的函数调用语句
    }

    // ＜声明头部＞   ::=  int＜标识符＞ |float ＜标识符＞|char＜标识符＞
    void statement_head_proc(){
        // TODO - 处理声明头部
    }

    // ＜参数＞    ::= ＜参数表＞
    void parameter_proc(){
        // TODO - 处理参数
    }

    // ＜参数表＞    ::=  ＜类型标识符＞＜标识符＞{,＜类型标识符＞＜标识符＞} | ＜空＞
    void parameter_table_proc(){
        //TODO - 处理参数表
    }

    // ＜表达式＞    ::= ［＋｜－］＜项＞{＜加法运算符＞＜项＞}
    void expression_proc(){
        //TODO - 处理表达式
    }

    // ＜项＞  ::= ＜因子＞{＜乘法运算符＞＜因子＞}
    void item_proc(){
        // TODO - 处理项
    }

    // ＜因子＞    ::= ＜标识符＞｜‘(’＜表达式＞‘)’｜＜整数＞｜＜有返回值函数调用语句＞|＜实数＞|＜字符＞
    void factor_proc(){
        // TODO - 处理因子
    }

    // ＜情况语句＞  ::=  switch ‘(’＜表达式＞‘)’ ‘{’＜情况表＞＜缺省＞ ‘}’
    void switch_statement_proc(){
        // TODO - 处理情况语句
    }

    //＜情况表＞   ::=  ＜情况子语句＞{＜情况子语句＞}
    void switch_table_proc(){
        // TODO - 处理情况表
    }

    // ＜情况子语句＞  ::=  case＜常量＞：＜语句＞
    void switch_proc(){
        // TODO - 处理情况子语句
    }

    // ＜缺省＞   ::=  default : ＜语句＞|＜空＞
    void default_proc(){
        // TODO - 处理default缺省语句
    }

    // ＜值参数表＞ ::= ＜表达式＞{,＜表达式＞}｜＜空＞
    void value_parameter_table_proc(){
        // TODO - 处理值参数表
    }

    // ＜读语句＞    ::=  scanf ‘(’＜标识符＞{,＜标识符＞}‘)’
    void scanf_proc(){
        // TODO - 处理读语句
    }

    // ＜写语句＞    ::= printf ‘(’ ＜字符串＞,＜表达式＞ ‘)’| printf ‘(’＜字符串＞ ‘)’|printf ‘(’＜表达式＞‘)’
    void printf_proc(){
        // TODO - 处理写语句
    }

    // ＜返回语句＞   ::=  return[‘(’＜表达式＞‘)’]
    void return_proc(){
        // TODO - 处理返回语句
    }

    // ＜主函数＞    ::= void main‘(’‘)’‘{’＜复合语句＞‘}’
    void main_function_proc(){
        // TODO - 主函数处理过程
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
        if (token_buf.get_buffer_vlaue(0).equals("(")){
            token_buf.update_buffer();
        }
        else{
            // TODO - 错误处理
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public static void main(String []args){   // Test ------------------------
        try {
            Lexer l = new Lexer("D:\\code\\Java\\MyIDE\\src\\test_lexer.txt");
            while (l.cur_status != 1){
                System.out.println(l.getToken().tag);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}