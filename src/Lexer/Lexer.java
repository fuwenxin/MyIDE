package Lexer;
import Table.Tag;

import java.io.*;
import java.util.*;



/**
 * Author : F_uu
 *
 * 词法分析器 ：
 * 基础功能 : 获取整数和基本的只含有字母的关键字的词法单元
 * 扩展功能 ：　消除注释、判别运算符、浮点数
 *
 */


public class Lexer {
    private int peek;
    myReader reader;
    String filePath;

    public int cur_status;
    private Hashtable words = new Hashtable();

    public Lexer(String path) throws Exception{

        reader = new myReader(path);
        filePath = path;
        initKeyWords();
        peek = ' ';
    }

    void  initKeyWords() {                              // 将关键字事先加入
        reserve(new Word(Tag.IFSYM, "if"));
        reserve(new Word(Tag.INTSYM, "int"));
        reserve(new Word(Tag.CASESYM, "case"));
        reserve(new Word(Tag.CHARSYM, "char"));
        reserve(new Word(Tag.SWITCHSYM, "switch"));
        reserve(new Word(Tag.MAINSYM, "main"));
        reserve(new Word(Tag.ELSESYM, "else"));
        reserve(new Word(Tag.FLOATSYM, "float"));
        reserve(new Word(Tag.VOIDSYM, "void"));
        reserve(new Word(Tag.CONSTSYM, "const"));
        reserve(new Word(Tag.WHILESYM, "while"));
        reserve(new Word(Tag.SCANFSYM, "scanf"));
        reserve(new Word(Tag.RETURNSYM, "return"));
        reserve(new Word(Tag.PRINTFSYM, "printf"));
        reserve(new Word(Tag.DEFAULTSYM, "default"));
    }

    void reserve(Word word){
        // 将该词存入Hash表中， 以 词素 —— Token 的键值对的形式 , 用以存储标识符和关键字
        words.put(word.lexemne,word);             // string ---- int
    }



    String formatInfo(String a,String b,String c){
        Formatter formatter = new Formatter();
        formatter.format("%10s\t%15s\t%15s\n",a,b,c);
        return formatter.toString();
    }

    String toBin(int b){
        if (b == 0)
            return "0";
        StringBuffer stringBuffer = new StringBuffer();
        while (b > 0){
            stringBuffer.append((b%2) + "");
            b/=2;
        }
        stringBuffer.reverse();
        return stringBuffer.toString();
    }

    public String getTokenStr()throws Exception{
        Token token = Scan();
        cur_status = reader.status;
        if(cur_status == 1)
            reader.close();
        return JudgePrint(token);
    }

    public Token getToken()throws Exception{
        Token token = Scan();
        token.lineNum = reader.lineNum;
        token.wordNum = reader.wordNum;
        cur_status = reader.status;
        if(cur_status == 1)
            reader.close();
        return token;
    }

    String JudgePrint(Token token){
        if (token == null)
            return "";
        String ret = "";
        switch(token.tag){
            case Tag.NUM:
                Num n = (Num)token;
                ret += formatInfo(n.Ivalue + "","整数",toBin(n.Ivalue) + "(二进制)");
                break;
            case Tag.FLOATNUM:
                FloatNum f = (FloatNum)token;
                ret += formatInfo(f.F_Num + "","浮点数",(f.F_Num) + "(十进制)");
                break;
            case Tag.ID:
                Word word = (Word)token;
                ret += formatInfo(word.lexemne,"标识符",word.lexemne);
                break;
            case Tag.PLUS:
                Word word1 = (Word)token;
                ret += formatInfo(word1.lexemne,"加法运算符",word1.lexemne);
                break;
            case Tag.MULT:
                Word word2 = (Word)token;
                ret += formatInfo(word2.lexemne,"乘法运算符",word2.lexemne);
                break;
            case Tag.RELA:
                Word word3 =(Word)token;
                ret += formatInfo(word3.lexemne,"关系运算符",word3.lexemne);
                break;
            case Tag.SPIC:
                Word word4 = (Word)token;
                ret += formatInfo(word4.lexemne,"分界符",word4.lexemne);
                break;
            case Tag.STRI:
                Word word5 = (Word)token;
                ret += formatInfo(word5.lexemne,"字符串",word5.lexemne);
                break;
            case Tag.CHAR:
                Word word6 = (Word)token;
                ret += formatInfo(word6.lexemne,"字符串",word6.lexemne);
                break;
        }
        if (token.tag >= 2009){
            Word word = (Word)token;
            ret += formatInfo(word.lexemne,"关键字",word.lexemne);
        }
        return ret;
    }

    public Token Scan()throws Exception{
        Scan_Blank_Comment();                         // 略过空格、换行符、注释

        if (Character.isDigit((char)peek)){           // 判断是否是数字，是的话返回相应的词法单元
            int num = 0 ;
            boolean isInteger = true;
            double float_num = 0.0 , num_width = 0.1;
            do {
                if (isInteger){
                    num = num * 10 + Character.digit(peek,10);
                    peek = reader.Read();

                    if (peek == '.'){
                        isInteger = false;
                        float_num = num;
                        peek = reader.Read();
                        continue;
                    }
                }
                else {
                    float_num += num_width * Character.digit(peek, 10);
                    num_width *= 0.1;
                    peek = reader.Read();
                }

            }while (Character.isDigit((char)peek));

            if (isInteger)
                return new Num(num);
            else
                return new FloatNum(float_num);
        }

        if (Character.isLetter((char)peek) || peek == '_'){          // 判断是否是关键字\标识符，是的话返回相应的词法单元
            StringBuffer strBuffer = new StringBuffer();

            do{
                strBuffer.append((char)peek);
                peek = reader.Read();
            }while (Character.isLetterOrDigit((char)peek) || peek == '_');

            String str = strBuffer.toString();
            Word word = (Word) words.get(str);
            if(word != null) {
                return word;
            }
            word = new Word(Tag.ID,str);
            return word;
        }

        if (peek == '<' || peek == '>' || peek == '!' || peek == '='){             // 处理 <、 > 、 <= 、 >= 、==、!=号
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append((char)peek);
            int last_peek = peek;
            peek = reader.Read();

            if (peek == '='){
                strBuffer.append((char)peek);
                peek = reader.Read();
            }
            else if(last_peek == '='){
                String str = strBuffer.toString();
                Word word = new Word(Tag.SPIC,str);        // 返回专用符号的词法单元
                return word;
            }
            String str = strBuffer.toString();
            Word word = new Word(Tag.RELA,str);           // 返回关系运算符的词法单元
            return word;
        }

        if (peek == '+' || peek == '-'){
            int last_peek = peek;
            peek = reader.Read();
            return new Word(Tag.PLUS, ((char)last_peek) + "");
        }

        if (peek == '*' || peek == '/'){
            int last_peek = peek;
            peek = reader.Read();
            return new Word(Tag.MULT, ((char)last_peek) + "");
        }

        if (peek == '\'') {
            char ch = (char)reader.Read();
            peek = reader.Read();
            if (peek == '\''){
                peek = reader.Read();
                return new Word(Tag.CHAR, ch + "");
            }
            else {
                // error
            }
        }

        if (peek == '#'){
            int last_peek = peek;
            peek = reader.Read();
            return new Word(Tag.SHAP, ((char)last_peek) + "");
        }

        if (peek == '"') {
            StringBuffer stringBuffer = new StringBuffer();
            while((peek = reader.Read()) != '"'){
                if(peek == '/'){
                    peek = reader.Read();
                    if (peek == 't'
                            || peek == 'b'
                            || peek == 'n'
                            || peek == 'f'
                            || peek == 'r'){
                        switch (peek){
                            case 'b':
                                peek = '\b';
                                break;
                            case 't':
                                peek = '\t';
                                break;
                            case 'f':
                                peek = '\f';
                                break;
                            case 'n':
                                peek = '\n';
                                break;
                            case 'r':
                                peek = '\r';
                                break;
                        }
                    }
                    else{
                        peek = reader.unRead();
                    }
                }
                stringBuffer.append((char)peek);
            }
            peek = reader.Read();
            return new Word(Tag.STRI,stringBuffer.toString());
        }
        if (peek == '(' || peek == ')' || peek == '{' || peek =='}' || peek == ',' || peek == ';')    {
            int last_peek = peek;
            peek = reader.Read();
            return new Word(Tag.SPIC,(char)last_peek + "");
        }
        // error
        peek = reader.Read();
        return new Token(-1);
    }
    private void Scan_Blank_Comment()throws IOException{
        for(;;peek = reader.Read()){                // 略过空格、换行符、注释
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n'||peek != '\r') continue;
            else if (peek == '/') {
                if(Scan_Comment()){
                    continue;
                }
                else
                    break;
            }
            else
                break;
        }
    }

    private boolean Scan_Comment()throws IOException{       // 判断完注释后指向peek注释的下一位置
        if (peek == '/'){
            peek = reader.Read();
            if(peek == '/'){                  // 判断 '//'
                do{
                    peek = reader.Read();
                }while (peek != '\n' || peek != '\r');
            }
            else if (peek == '*'){            // 判断  '/*'
                int status = 0;
                do{
                    peek = reader.Read();
                    if (peek == '*'){
                        status = 1;
                    }
                    else if(peek == '/' && status == 1){
                        peek = reader.Read();
                        break;
                    }
                    else
                        status = 0;
                }while (true);
            }
            else{
                peek = reader.unRead();
                return false;
            }
        }
        return true;
    }
}