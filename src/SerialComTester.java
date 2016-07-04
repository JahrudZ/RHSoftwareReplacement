/**
 * Created by JahrudZ on 6/27/16.
 */
public class SerialComTester {

    public SerialComTester(){

    }

    public String selectPort(){
        System.out.println("Just opened");
        return "PORT";
    }

    public void openPort(String port){
        System.out.println("Opened");
    }

    public void writeToPort(String text){
        //System.out.println("written");
    }

    public String readFromPort(){
        return "" + Math.random();
    }

    private void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
