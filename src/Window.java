import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class Window extends Component{

    private JFrame frame;
    private String version = "v1.0";
    SerialCom com;
    //SerialComTester com;

    JTextPane dataPane;

    long beginTime;
    long beginRecordingTime;

    Window window;
    String saveFile;
    boolean isRecording = false;
    FileWriter writer;

    final JFileChooser fc = new JFileChooser();

    private String[] syntax = {"DP?", "FP?", "RH?", "RHw?", "PPMv?", "PPMw?", "AH?", "SH?", "VP?", "P?", "Tx?", "Tm?", "Th?", "Om?", "Ox?"};
    private String[] function = {"Dew Point (C)", "Frost Point (C)", "Relative Humidity (%)", "Relative Humidity WMO (%)", "Volume Ratio PPMv", "Weight Ratio PPMw", "Absolute Humidity (g/m3)", "Specific Humidity (g/kg)", "Vapor Pressure (Pa)", "Head Pressure (Pa)", "External Temperature (C)", "Mirror Temperature (C)", "Head Temperature (C)", "Mirror PRT Resistance (Ohms)", "External PRT Resistance (Ohms)"};
    private String[] data = new String[15];
    private JFreeChart[] charts = new JFreeChart[15];
    private XYSeries[] series = new XYSeries[15];
    private JPanel[][] chartPanelList = {new JPanel[15], new JPanel[15], new JPanel[15], new JPanel[15]};
    private JPanel[] miniContainerPanel = new JPanel[4];
    private JPanel[] containerPanelList = new JPanel[4];

    public static void main(String[] args) {
        Window window = new Window();
        window.window = window;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t = new Thread(new Runnable() {
            public void run() {
                window.com = new SerialCom();
                window.com.selectPort();
                window.beginTime = System.currentTimeMillis();
                window.update();
            }
        });
        t.start();
    }

    public Window() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("RH Software Replacement " + version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JLabel BaseLabel = new JLabel("by Jared Zhao");
        BaseLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
        BaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(BaseLabel, BorderLayout.SOUTH);

        JPanel DataRecPanel = new JPanel();
        DataRecPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
        frame.getContentPane().add(DataRecPanel, BorderLayout.WEST);
        GridBagLayout gbl_DataRecPanel = new GridBagLayout();
        gbl_DataRecPanel.columnWidths = new int[]{87, 0};
        gbl_DataRecPanel.rowHeights = new int[]{262, 0, 0, 0};
        gbl_DataRecPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_DataRecPanel.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
        DataRecPanel.setLayout(gbl_DataRecPanel);

        dataPane = new JTextPane();
        dataPane.setText("Data Output");
        GridBagConstraints gbc_dataPane = new GridBagConstraints();
        gbc_dataPane.insets = new Insets(0, 0, 5, 0);
        gbc_dataPane.fill = GridBagConstraints.BOTH;
        gbc_dataPane.gridx = 0;
        gbc_dataPane.gridy = 0;
        DataRecPanel.add(dataPane, gbc_dataPane);

        JButton recordButton = new JButton("Record");
        GridBagConstraints gbc_recordButton = new GridBagConstraints();
        gbc_recordButton.anchor = GridBagConstraints.SOUTH;
        gbc_recordButton.insets = new Insets(0, 0, 5, 0);
        gbc_recordButton.gridx = 0;
        gbc_recordButton.gridy = 1;
        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isRecording) {
                    try {
                        writer.close();
                        isRecording = false;
                        beginRecordingTime = 0;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    window.saveFile = window.saveFile(".csv");
                    beginRecordingTime = System.currentTimeMillis();
                    isRecording = true;
                }
            }
        });
        DataRecPanel.add(recordButton, gbc_recordButton);

        JPanel GraphPanel = new JPanel();
        GraphPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
        frame.getContentPane().add(GraphPanel, BorderLayout.CENTER);
        GraphPanel.setLayout(new GridLayout(2, 2, 10, 10));



            for(int i = 0; i < series.length; i++){
                series[i] = new XYSeries(function[i]);
                XYSeriesCollection dataset = new XYSeriesCollection(series[i]);
                charts[i] = createChart(function[i], "Time (s)", function[i], dataset);
                for(int j = 0; j < chartPanelList.length; j++) {
                    chartPanelList[j][i] = new ChartPanel(charts[i]);
                }
            }

            initContainerPanels();

            GraphPanel.add(containerPanelList[0]);
            GraphPanel.add(containerPanelList[1]);
            GraphPanel.add(containerPanelList[2]);
            GraphPanel.add(containerPanelList[3]);

        JPanel ConsSetPanel = new JPanel();
        ConsSetPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
        frame.getContentPane().add(ConsSetPanel, BorderLayout.EAST);
        GridBagLayout gbl_ConsSetPanel = new GridBagLayout();
        gbl_ConsSetPanel.columnWidths = new int[]{0, 0};
        gbl_ConsSetPanel.rowHeights = new int[]{0, 0};
        gbl_ConsSetPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_ConsSetPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        ConsSetPanel.setLayout(gbl_ConsSetPanel);

        JTextPane consolePane = new JTextPane();
        consolePane.setText("  System Console: Outputs runtime info/error  ");
        GridBagConstraints gbc_consolePane = new GridBagConstraints();
        gbc_consolePane.fill = GridBagConstraints.BOTH;
        gbc_consolePane.gridx = 0;
        gbc_consolePane.gridy = 0;
        ConsSetPanel.add(consolePane, gbc_consolePane);
    }

    private void createContainerPanel(int whichGraph, int whichPanel){
        containerPanelList[whichGraph] = new JPanel();
        GridBagLayout gbl_THEGRAPHPANEL = new GridBagLayout();
        gbl_THEGRAPHPANEL.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_THEGRAPHPANEL.rowHeights = new int[]{0, 0, 0};
        gbl_THEGRAPHPANEL.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_THEGRAPHPANEL.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        containerPanelList[whichGraph].setLayout(gbl_THEGRAPHPANEL);

        JComboBox comboBox = new JComboBox(function);
        comboBox.setSelectedIndex(whichGraph);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)miniContainerPanel[whichGraph].getLayout();
                cl.show(miniContainerPanel[whichGraph], "Card " + comboBox.getSelectedIndex());
            }
        });
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.gridwidth = 3;
        gbc_comboBox.insets = new Insets(0, 0, 5, 5);
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 0;
        gbc_comboBox.gridy = 0;
        containerPanelList[whichGraph].add(comboBox, gbc_comboBox);

        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridwidth = 4;
        gbc_panel.insets = new Insets(0, 0, 0, 5);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 1;
        containerPanelList[whichGraph].add(miniContainerPanel[whichGraph], gbc_panel);
    }

    private void initContainerPanels(){
        for(int i = 0; i < miniContainerPanel.length; i++){
            miniContainerPanel[i] = new JPanel(new CardLayout());
            for(int j = 0; j < charts.length; j++){
                miniContainerPanel[i].add(chartPanelList[i][j], "Card " + j);
            }
            createContainerPanel(i, i);
        }
    }

    private JFreeChart createChart(String title, String XAxis, String YAxis, final XYDataset dataset){
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                XAxis,
                YAxis,
                dataset);
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60.0);
        axis = plot.getRangeAxis();
        axis.setAutoRange(true);
        return chart;
    }

    private void update(){

        for(int i = 0; i < syntax.length; i++){
            sleep(10);
            com.writeToPort(syntax[i]);
            sleep(60);
            data[i] = com.readFromPort();
            data[i] = data[i].replaceAll("\r", "");
            data[i] = data[i].replaceAll("\n", "");
        }

        long finishTime = System.currentTimeMillis();
        double displayTime = (double)(finishTime - beginTime) / 1000.0;
        double recordingTime = (double)(finishTime - beginRecordingTime) / 1000.0;
        if(recordingTime > 99999999){
            recordingTime = 0;
        }

        String dataOutput = " Time Elapsed (s): \t\t" + displayTime + "\r\n\n Recording Time (s): \t\t" + recordingTime + "\r\n\n ";
        for(int i = 0; i < syntax.length; i++) {
            dataOutput += function[i] + ":";
            if(i == 3 || i == 6 || i == 7 || i == 10 || i == 11 || i == 12 || i == 13 || i ==14){
                dataOutput += "\t" + data[i] + "\r\n\n ";
            } else {
                dataOutput += "\t\t" + data[i] + "\r\n\n ";
            }
        }

        if(isRecording){
            String recString = "" + displayTime + "," + recordingTime + ",";
            for(int i = 0; i < data.length; i++){
                recString += data[i];
                recString += ",";
            }
            recString += "\n";
            writeFile(recString);
        }

        //dataOutput += "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
        dataPane.setText(dataOutput);

        for(int i = 0; i < series.length; i++) {
            this.series[i].add(displayTime, Double.parseDouble(data[i]));
        }

        update();
    }

    private void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String saveFile(String format){
        fc.showSaveDialog(null);
        File target_file = fc.getSelectedFile();
        String currentName = target_file.toString();
        String fileName = currentName;
        if(currentName.contains(".")) {
            fileName = currentName.substring(0, currentName.indexOf("."));
        }
        fileName += format;
        String openingText = "System Time (s),Recording Time (s),";
        for(int i = 0; i < function.length; i++){
            openingText += function[i];
            openingText += ",";
        }
        openingText += "\n";
        try {
            writer = new FileWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeFile(openingText);
        return fileName;
    }

    public void writeFile(String text){
        try {
            writer.append(text);
            //writer.append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

