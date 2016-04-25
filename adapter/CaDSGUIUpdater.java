package adapter;

import java.util.HashSet;
import java.util.Set;

import org.cads.ev3.gui.ICaDSGUIUpdater;

public class CaDSGUIUpdater implements ICaDSGUIUpdater{
    Set<String> contextSet;
      
    public CaDSGUIUpdater() {
        contextSet = new HashSet<String>();
    }
    
    @Override
    public void addService(String newContext) {
        if(newContext != null){
            contextSet.add(newContext);
        } else {
            throw new NullPointerException("Parameter is null!");
        }
    }

    @Override
    public void setChoosenService(String arg0, int unused1, int unused2) {
        //throw new UnsupportedOperationException("setChoosenService is not supported.");
    }

    @Override
    public void teardown() {
        throw new UnsupportedOperationException("teardown is not supported.");      
    }

    @Override
    public void updateComboBox() {
        throw new UnsupportedOperationException("updateComboBox is not supported.");
    }

    @Override
    public void updateHorizontalSlider() {
        throw new UnsupportedOperationException("updateHorizontalSlider is not supported."); 
    }

    @Override
    public void updateVerticalSlider() {
        throw new UnsupportedOperationException("updateVerticalSlider is not supported.");   
    }
    
    public Set<String> getContextSet(){
        return contextSet;
    }
}
