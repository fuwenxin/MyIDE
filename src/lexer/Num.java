package lexer;

/*
 * 继承自词法单元，
 *   用于存储整数
 * */

public class Num extends Token{
    public final int Ivalue;
    public Num(int I){
        super(Tag.NUM);
        Ivalue = I;
    }
}
