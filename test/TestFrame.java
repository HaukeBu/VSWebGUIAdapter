package test;

import org.cads.ev3.gui.ICaDSGUIUpdater;
import org.cads.ev3.rmi.ICaDSRMIConsumer;
import org.junit.Test;

import adapter.WebGUI;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveGrapper;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveHorizontal;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveVertical;
import io.cuckoo.websocket.nephila.WebSocketException;
import logger.LogLevel;
import logger.Logger;

public class TestFrame implements IIDLCaDSEV3RMIMoveGrapper, IIDLCaDSEV3RMIMoveHorizontal, IIDLCaDSEV3RMIMoveVertical, ICaDSRMIConsumer {

    @Test
    public static void main(String[] args) {
        Logger.init(LogLevel.DEBUG);
        
        TestFrame bla = new TestFrame();
         try {
            WebGUI.startGUI(bla, bla, bla, bla, true);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(ICaDSGUIUpdater observer) {
        System.out.println("New Observer");
        observer.addService("Service1");
        observer.addService("Service2");
    }

    @Override
    public void update(String comboBoxText) {
        // called if Combobox is updated
        System.out.println("Combo Box updated " + comboBoxText);
    }

    @Override
    public void moveVerticalToPercent(int tid, int percent) throws Exception {
        System.out.println("Call to move vertical -  TID: " + tid + " degree " + percent);

    }

    @Override
    public void moveMoveHorizontalToPercent(int tid, int percent) throws Exception {
        System.out.println("Call to move horizontal -  TID: " + tid + " degree " + percent);

    }

    @Override
    public void stop(int i) throws Exception {
        System.out.println("Stop movement....");
    }

    @Override
    public void closeIT(int i) throws Exception {
        System.out.println("Close....");

    }

    @Override
    public void openIT(int i) throws Exception {
        System.out.println("Open....");
    }
}
