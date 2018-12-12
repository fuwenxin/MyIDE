package lexer;

/*
 * 继承自词法单元，
 *  用于存储浮点数
 * */

public class FloatNum extends Token{
    public final double F_Num;
    public FloatNum(double f_num){
        super(Tag.FLOATNUM);
        F_Num = f_num;
    }

}
