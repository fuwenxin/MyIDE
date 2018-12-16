package MyIDEUI;

import Suanfu.BuildTable;
import Suanfu.Suanfu;
import javafx.util.Pair;
import Lexer.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Editor {
    JFrame frame;
    JButton suanfuBtn;
    JButton lexelBtn;
    JButton grammBtn;
    JTextArea showFile;
    JScrollPane scrollPane;
    JPanel displayJpanel;
    JMenuBar jMenuBar;
    JMenu fileMenu;
    JMenu helpMenu;

    String path;
    String dir;
    String fil;

    JTextArea showResult;
    JScrollPane scrollPane1;
    BuildTable buildTable;

    public Editor(){
        frame = new JFrame();
        frame.setTitle("My little IDE ~~");
        frame.setSize(600,600);
        frame.setLocation(400,400);
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);

        Container container = frame.getContentPane();
        suanfuBtn = new JButton("算符优先");
        grammBtn = new JButton("选择文法");
        lexelBtn = new JButton("词法分析");
        BorderLayout layout = new BorderLayout();
        container.setLayout(layout);
        showFile = new JTextArea("Welcome to my little IDE ~~");
        showFile.setRows(10);
        showFile.setColumns(10);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(showFile);
        container.add(scrollPane,BorderLayout.CENTER);
        showFile.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S){
                    System.out.println("2333");
                    String saveBuffer = showFile.getText();
                    String pathname = dir + File.separator + fil;
                    File file = new File(pathname);
                    try {
                        PrintWriter pw  = new PrintWriter(file);
                        pw.write(saveBuffer);
                        pw.close();
                    }
                    catch (FileNotFoundException ee){
                        ee.printStackTrace();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        showResult = new JTextArea("");
        showResult.setEditable(false);
        showResult.setRows(7);
        showResult.setColumns(15);
        scrollPane1 = new JScrollPane();
        scrollPane1.setViewportView(showResult);
        container.add(scrollPane1,BorderLayout.SOUTH);
        /*
        * 创建以及菜单
        * */
        jMenuBar = new JMenuBar();
        fileMenu = new JMenu("文件");
        helpMenu = new JMenu("帮助");
        jMenuBar.add(fileMenu);
        jMenuBar.add(helpMenu);
        JMenuItem openMenuItem = new JMenuItem("打开");
        JMenuItem saveMenuItem = new JMenuItem("保存");
        JMenuItem exitMenuItem = new JMenuItem("退出");
        JMenuItem helpMenuItem = new JMenuItem("使用帮助");
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        helpMenu.add(helpMenuItem);
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog( frame, "简单的编译器\n" +
                        "Author : 16211104 付文欣\n" +
                        "当前功能:\n" +
                        "!!! 在编辑后必须保存(Ctrl+S)才能进行分析编辑后的文件，否则仍分析的是保存前的文件；!!!\n" +
                        "(1) 词法分析器，在`文件`->`打开`中选择要分析的文件，点击词法分析即可；如果在编辑框中有修改，必须将原文件进行保存才能分析新文件；\n" +
                        "\n" +
                        "(2) 算符优先分析器：\n" +
                        "    可已使用默认文法：\n" +
                        "        E -> E+T|T\n" +
                        "        T -> T*F|F\n" +
                        "        F -> (E)|i\n" +
                        "    也可输入文法[每行一个产生式，不须写产生式个数];\n" +
                        "    输入的表达式应以#开头，#结尾。", "帮助", JOptionPane.INFORMATION_MESSAGE );
            }
        });
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        container.add(jMenuBar,BorderLayout.NORTH);
        displayJpanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        displayJpanel.setLayout(gridBagLayout);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy= 3;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.insets = new Insets(8,8,8,8);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        displayJpanel.add(lexelBtn,gridBagConstraints);
        gridBagConstraints.gridy = 4;
        displayJpanel.add(suanfuBtn,gridBagConstraints);
        gridBagConstraints.gridy = 5;
        displayJpanel.add(grammBtn,gridBagConstraints);
        container.add(displayJpanel,BorderLayout.EAST);
        myDialog();
        frame.validate();

        buildTable = new BuildTable(null);
    }


    public void loadFile(){
        FileDialog fileDialog = new FileDialog(frame,"选择文件并打开",FileDialog.LOAD);
        fileDialog.setVisible(true);
        String pathName = fileDialog.getDirectory() + fileDialog.getFile();
        path = pathName;
        if (buildTable.is_default)
            frame.setTitle(pathName +  "(使用默认算符文法) - My little IDE");
        else
            frame.setTitle(pathName +  "(使用输入的算符文法) - My little IDE");
        File file = new File(pathName);

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            try {
                String content = null;
                showFile.setText(null);
                while ((content = br.readLine()) != null){
                    showFile.append(content + '\n');
                }
                dir = fileDialog.getDirectory();
                fil = fileDialog.getFile();
                fr.close();
                br.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public  void saveFile (){
        FileDialog savefile = new FileDialog(frame,"保存到：",FileDialog.SAVE);
        savefile.setVisible(true);
        String saveBuffer = showFile.getText();
        String pathname = savefile.getDirectory() + savefile.getFile();
        File file = new File(pathname);
        try {
            PrintWriter pw  = new PrintWriter(file);
            pw.write(saveBuffer);
            pw.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public void myDialog(){
        lexelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                   Lexer lexer = new Lexer(path);
                   String fileStr = dir + File.separator + "lexer.txt";
                   File file = new File(fileStr);
                   if(!file.exists()){
                       file.createNewFile();
                   }
                   PrintStream printStream = new PrintStream(new FileOutputStream(fileStr));
                   printStream.println("==============================================\n" +
                           "简单的词法分析器\n"+
                           "==============================================");
                   printStream.println("将分析"+ path +"中的内容");
                   printStream.println("运行结果如下：");
                   while(lexer.cur_status == 0){
                       printStream.println(lexer.getTokenStr());
                   }
                   FileReader fr = new FileReader(file);
                   BufferedReader bf = new BufferedReader(fr);
                   String content = null;
                   showResult.setText(null);
                   while ((content = bf.readLine()) != null){
                       showResult.append(content + '\n');
                   }
                   fr.close();
                   bf.close();

                   printStream.close();
               }
                catch (Exception ee){
                   ee.printStackTrace();
                }
            }
        });
        suanfuBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                showResult.setText(null);
                showResult.append("文法为：\n");
                for(Object obj:buildTable.grammar.keySet()){
                    showResult.append(buildTable.grammar.get(obj) + "->" + obj + "\n");
                }
                showResult.append("*********************开始符号Z**********************\n");
                showResult.append(buildTable.Z + "\n");
                showResult.append("************************Vt集***********************\n");
                for (Object obj:buildTable.Vt){
                    showResult.append(obj + "   ");
                }
                showResult.append("\n");
                showResult.append("************************Vn集***********************\n");
                for (Object obj:buildTable.Vn) {
                    showResult.append(obj + "   ");
                }
                showResult.append("\n");
                showResult.append("********************FirstVt集**********************\n");
                for (Object obj:buildTable.Vn){
                    showResult.append(obj + ": ");
                    for (Object key:buildTable.firstVt){
                        Pair<String,String> temp = (Pair<String,String>)key;
                        if(temp.getKey().equals(obj)){
                            showResult.append(temp.getValue() + "    ");
                        }
                    }
                    showResult.append("\n");
                }
                showResult.append("*********************LastVt集**********************\n");
                for (Object obj:buildTable.Vn){
                    showResult.append(obj + ": ");
                    for (Object key:buildTable.lastVt){
                        Pair<String,String> temp = (Pair<String,String>)key;
                        if(temp.getKey().equals(obj)){
                            showResult.append(temp.getValue() + "    ");
                        }
                    }
                    showResult.append("\n");
                }
                showResult.append("*********************算符优先表**********************\n");
                showResult.append(" \t");
                for (Object obj:buildTable.Vt){
                    showResult.append(obj + "\t");
                }
                showResult.append("\n");
                for (Object right:buildTable.Vt){
                    showResult.append(right + "\t");
                    for (Object left:buildTable.Vt){
                        Pair <String,String> pair = new Pair(left,right);
                        Integer result = (Integer)buildTable.table.get(pair);
                        if (result == null){
                            showResult.append(" \t");
                        }
                        else if (result.equals(1)){
                            showResult.append(">\t");
                        }
                        else if (result.equals(0)){
                            showResult.append("=\t");
                        }
                        else
                            showResult.append("<\t");
                    }
                    showResult.append("\n");
                }
                showResult.append("\n");
                showFile.append("*********************归约结果**********************\n");
                try {
                    Suanfu suanfu = new Suanfu(buildTable,dir + File.separator + fil);
                    while (!suanfu.is_OK){
                        showFile.append(suanfu.reduce() + "\n");
                    }
                }catch (Exception ee){
                    ee.printStackTrace();
                }
                showFile.append("*********************归约结束**********************\n");
            }
        });

        grammBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog(frame,"选择文件并打开",FileDialog.LOAD);
                fileDialog.setVisible(true);
                String pathName = fileDialog.getDirectory() + fileDialog.getFile();
                File file = new File(pathName);
                if(file == null){
                    // 文件不存在 error
                    return;
                }
                buildTable = new BuildTable(pathName);
                if (buildTable.is_default)
                    frame.setTitle(path +  "(使用默认算符文法) - My little IDE");
                else
                    frame.setTitle(path +  "(使用输入的算符文法) - My little IDE");
            }
        });
    }
}
