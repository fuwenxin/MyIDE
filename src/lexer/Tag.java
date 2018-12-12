package lexer;

public class Tag {
    public final static int
            NUM = 1998,      // 整数
            FLOATNUM = 1999, // 浮点数
            ID = 2000,       // 关键字/标识符
            PLUS = 2001,     // 加法运算符
            MULT = 2002,     // 乘法运算符
            RELA = 2003,     // 关系运算符
            SPIC = 2004,     // 专用符号
            STRI = 2005,     // 字符串
            SHAP = 2006      // # 词法分析器使用
                    ;
}