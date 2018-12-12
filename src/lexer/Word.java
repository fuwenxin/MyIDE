package lexer;
/*
 * 继承自词法单元，
 *   用于存储关键字 和 标识符
 * */

public class Word extends Token{
    public final String lexemne;
    public Word(int lexe,String str){
        super(lexe);
        lexemne = str;
    }
}
