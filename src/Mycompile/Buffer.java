package Mycompile;
import Lexer.*;
import Table.Tag;

/**
 * 提供缓冲区，使得可以预读2个词法单元
 *
 * */


public class Buffer {
    int buffer_pointer;
    String file_path;
    Lexer lexer;
    Token cur_read_token;
    public Token []buffer;
    public Buffer(){  // 预读入三个词法单元
        file_path = ".";
        buffer_pointer = 0;
        try {
            lexer = new Lexer(file_path);
            buffer = new Token[3];
            buffer[0] = new Token(-1);
            buffer[1] = new Token(-1);
            buffer[2] = new Token(-1);
            cur_read_token = lexer.getToken();
            if(cur_read_token.tag == -1)         // cur_status == 1 表示已读结束
                return;
            buffer[0] = cur_read_token;
            cur_read_token = lexer.getToken();
            if(cur_read_token.tag == -1)
                return;
            buffer[1] = cur_read_token;
            cur_read_token = lexer.getToken();
            if(cur_read_token.tag == -1)
                return;
            buffer[2] = cur_read_token;
        }catch (Exception e){
            // error
        }
    }

    public int get_buffer_type(int n){
        return buffer[(buffer_pointer + n)%3].tag;
    }

    public int get_token_position(int n){
        return buffer[(buffer_pointer + n)%3].lineNum;
    }

    public String get_buffer_value(int n){
        Token tok = buffer[(buffer_pointer + n)%3];
        if(tok.tag == Tag.ID){
            return ((Word)tok).lexemne;
        }
        if(tok.tag == Tag.NUM){
            return ((Num)tok).Ivalue + "";
        }
        if(tok.tag == Tag.FLOATNUM){
            return ((FloatNum)tok).F_Num + "";
        }
        return null;
    }

    /**
    * 更新缓存区、读入下一个词法单元
    * */
    public void update_buffer(){
        if (cur_read_token.tag == -1){
            buffer[buffer_pointer++] = cur_read_token;
        }
        else{
            try {
                cur_read_token = lexer.getToken();
            }
            catch (Exception e){
                // error
            }
            buffer[buffer_pointer++] = cur_read_token;
        }
        buffer_pointer %= 3;
    }
}
