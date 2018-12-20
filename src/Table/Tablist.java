package Table;

import java.util.ArrayList;
import java.util.Stack;

public class Tablist {
    private ArrayList tables;
    // TODO - 存储结构可以改为栈式存储

    private int sumTables;
    private int curTable;
    private Stack lastTable;

    public Tablist(Tab tab){
        tables = new ArrayList<Tab>();
        lastTable = new Stack();
        tables.add(tab);
        sumTables = 1;
        curTable = 0;
    }

    public int addTable(Tab tab){
        curTable = sumTables;
        Tab ret = new Tab(tab,curTable);
        tables.add(ret);
        sumTables ++;
        return curTable;
    }

    public Tab getCurTable(){
        return (Tab)tables.get(curTable);
    }

    public void shiftTab(int loc){
        lastTable.push(curTable);
        curTable = loc;
    }

    public void shiftBack(){
        curTable = (int)lastTable.pop();
    }

    public void frontable(){            // 当前表指针指向上一层的表
        curTable = getCurTable().prev.getPos();
    }

}
