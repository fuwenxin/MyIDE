package lexer;
import javax.swing.*;
import java.io.*;

/*
 * 用于从文件中读取字符
 *
 * */

public class myReader {
    int cur_peak;
    int last_peek;
    int line;
    int status;               // 用于判断是否到达文件末尾
    FileReader fileReader;
    PushbackReader pushbackReader;
    myError_lex myError;
    public myReader(String path,myError_lex error)throws IOException{
        status = 0;
        line = 1;
        myError = error;
        fileReader = new FileReader(path);
        pushbackReader = new PushbackReader(fileReader,20);
    }

    public int Read()throws IOException{
        last_peek = cur_peak;
        cur_peak = pushbackReader.read();
        if(cur_peak == '\n')
            line = line + 1;
        if (status == 1){
            myError.print_error(line);
        }
        else if(cur_peak == -1) {
            status = 1;
            //throw new EOFException();
        }
        return cur_peak;
    }

    public int unRead()throws IOException{
        pushbackReader.unread(cur_peak);
        return last_peek;
    }
    public void close()throws IOException{
        fileReader.close();
        pushbackReader.close();
    }
}
