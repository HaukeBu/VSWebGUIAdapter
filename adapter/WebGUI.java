package adapter;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.cads.ev3.gui.ICaDSGUIUpdater;
import org.cads.ev3.rmi.ICaDSRMIConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cadSRMIInterface.IIDLCaDSEV3RMIMoveGrapper;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveHorizontal;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveVertical;
import io.cuckoo.websocket.nephila.WebSocket;
import io.cuckoo.websocket.nephila.WebSocketConfig;
import io.cuckoo.websocket.nephila.WebSocketException;
import io.cuckoo.websocket.nephila.WebSocketListener;
import io.cuckoo.websocket.nephila.impl.DefaultWebSocket;

public class WebGUI implements Runnable{

    private final IIDLCaDSEV3RMIMoveGrapper moveGrap;
    private final IIDLCaDSEV3RMIMoveHorizontal moveHorizontal;
    private final IIDLCaDSEV3RMIMoveVertical moveVertical;
    private final ICaDSRMIConsumer consumer;
    private final CaDSGUIUpdater guiUpdater;
    private final WebSocket ws;
    private final BlockingQueue<String> newMessages;
    private int transactionNumber;

    public WebGUI(IIDLCaDSEV3RMIMoveGrapper moveGrap, IIDLCaDSEV3RMIMoveHorizontal moveHorizontal, IIDLCaDSEV3RMIMoveVertical moveVertical, ICaDSRMIConsumer consumer, String ip, int port) throws WebSocketException {
        this.moveGrap = moveGrap;
        this.moveHorizontal = moveHorizontal;
        this.moveVertical = moveVertical;
        this.consumer = consumer;
        transactionNumber = 0;
        
        newMessages = new ArrayBlockingQueue<String>(1000);
        guiUpdater = new CaDSGUIUpdater();
        WebSocketListenerImpl wsl = new WebSocketListenerImpl(newMessages);
        ws = new DefaultWebSocket(wsl);
        wsl.setWs(ws);
        
        ws.connect("ws://" + ip + ":" + port);
        
        consumer.register(guiUpdater);

        sendAllServices();
    }

    public static void startGUI(IIDLCaDSEV3RMIMoveGrapper moveGrap, IIDLCaDSEV3RMIMoveHorizontal moveHorizontal, IIDLCaDSEV3RMIMoveVertical moveVertical, ICaDSRMIConsumer consumer, String ip, int port) throws WebSocketException {
        WebGUI gui = new WebGUI(moveGrap, moveHorizontal, moveVertical, consumer, ip, port);
        Thread thread = new Thread(gui);
        thread.setName(gui.getClass().getSimpleName() + "-Thread");
        thread.setDaemon(false);
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                processInputString(newMessages.take());
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private void processInputString(String webSocketInput) {
        JSONObject jsonObj = null;
        try {
            jsonObj = (JSONObject) new JSONParser().parse(webSocketInput);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (jsonObj != null) {
            String type = (String) jsonObj.get("type");
            String service = "";
            int value = 0;

            switch (type) {
                case "openIT":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveGrap.openIT(transactionNumber++);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                    
                case "closeIT":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveGrap.closeIT(transactionNumber++);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                    
                case "moveVerticalToPercent":
                    value = Integer.parseInt(jsonObj.get("percent").toString());
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveVertical.moveVerticalToPercent(transactionNumber++, value);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                    
                case "moveHorizontalToPercent":
                    value = Integer.parseInt(jsonObj.get("percent").toString());
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveHorizontal.moveMoveHorizontalToPercent(transactionNumber++, value);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                    
                case "stop":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveHorizontal.stop(transactionNumber++);
                        moveVertical.stop(transactionNumber++);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                    
                case "request":                  
                    sendAllServices();

                    break;
                    
                default :
                    System.out.println("default--- Fall");
            }
        }
    }

    private void sendAllServices() {
        JSONObject allServices = new JSONObject();
        JSONArray array = new JSONArray();
        
        allServices.put("type", "allServices");
        
        for(String context: guiUpdater.getContextSet()) {
            array.add(context);
        }
        
        allServices.put("services", array);
        
        try {
            ws.send(allServices.toJSONString() + "\n");
        } catch (WebSocketException e) {
            
        }
    }
}
