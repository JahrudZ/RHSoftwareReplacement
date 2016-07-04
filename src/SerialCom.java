/**
 * Created by JahrudZ on 6/20/16.
 */
import jssc.*;

public class SerialCom {

    SerialPort serialPort;

    public SerialCom(){

    }

    public String selectPort(){
        System.out.print("\b\b\b\b\b");

        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i > -1; i++){
            int j = i % portNames.length;
            sleep(40);
            openPort(portNames[j]);
            sleep(40);
            writeToPort("ID?");
            sleep(40);
            String ID = readFromPort();
            sleep(40);
            writeToPort("IDN?");
            sleep(40);
            String IDN = readFromPort();
            IDN = IDN.replaceAll("\r", "");
            IDN = IDN.replaceAll("\n", "");
            if(ID.contains(IDN)){
                return portNames[j];
            }
        }
        return "NONE";
    }

    public void openPort(String port){
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0, false, true);

            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void writeToPort(String text){
        try {
            text += (char)13;
            serialPort.writeBytes(text.getBytes());
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public String readFromPort(){
        String input = null;
        try {
            input = serialPort.readString();
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
        return input;
    }

    private void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
