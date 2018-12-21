package Table;

import Lexer.Token;

public class InfoSym{
    public int lineNum;                          // 声明行号
    public int symbolKind;                       // 符号表中类型 如：常量,变量,过程,函数,参数值,临时变量
    public int symbolType;                       // 符号表中类型 如: INT, CHAR,FLOAT,STRING,VOID 等
    public String symValue;                      // 符号值
    public int relatedPos;

    public InfoSym(int line,int kind, int type,int pos,String value){
        lineNum = line;
        symbolType = type;
        symbolKind = kind;
        symValue = value;
        relatedPos = pos;
    }
}
