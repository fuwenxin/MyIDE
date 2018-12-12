package lexer;

/*
 * 词法单元对象:
 *   含有一个属性tag，用以存储该词法单元的类型；
 *       1. 整数
 *       2. 浮点数
 *       3. 关键字/标识符
 *       4. 分界符
 *       5. 常量字符串
 *       6. 运算符
 * */

public class Token {
    public final int tag;
    public Token(int t) {
        tag = t;
    }
}
