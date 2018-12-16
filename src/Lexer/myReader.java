package Lexer;
import java.io.*;

/*
 * 用于从文件中读取字符
 *
 * */

public class myReader {
    int cur_peak;
    int last_peek;
    int lineNum;
    int wordNum;
    int status;               // 用于判断是否到达文件末尾
    FileReader fileReader;
    PushbackReader pushbackReader;
    public myReader(String path)throws IOException{
        status = 0;
        lineNum = 1;
        wordNum = 0;
        fileReader = new FileReader(path);
        pushbackReader = new PushbackReader(fileReader,20);
    }

    public int Read()throws IOException{
        last_peek = cur_peak;
        cur_peak = pushbackReader.read();
        if(cur_peak == '\n') {
            lineNum = lineNum + 1;
            wordNum = 0;
        }
        else{
            wordNum ++;
        }
        if (status == 1){
            // TODO- ERROR(LERERROR) 错误处理
        }
        else if(cur_peak == -1) {
            status = 1;
            // TODO - ERROR(LERERROR) 错误处理
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
