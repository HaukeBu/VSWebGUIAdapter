package adapter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.cads.ev3.rmi.ICaDSRMIConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import cadSRMIInterface.IIDLCaDSEV3RMIMoveGrapper;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveHorizontal;
import cadSRMIInterface.IIDLCaDSEV3RMIMoveVertical;
import io.cuckoo.websocket.nephila.WebSocket;
import io.cuckoo.websocket.nephila.WebSocketException;
import io.cuckoo.websocket.nephila.impl.DefaultWebSocket;
import logger.LogLevel;
import logger.Logger;

public class WebGUI implements Runnable{

    private final IIDLCaDSEV3RMIMoveGrapper moveGrap;
    private final IIDLCaDSEV3RMIMoveHorizontal moveHorizontal;
    private final IIDLCaDSEV3RMIMoveVertical moveVertical;
    private final ICaDSRMIConsumer consumer;
    private final CaDSGUIUpdater guiUpdater;
    private final WebSocket ws;
    private final BlockingQueue<String> newMessages;
    private int transactionNumber;
    private String identifier;

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
        
        identifier = "noIdentifier";
        
        consumer.register(guiUpdater);

        registerAdapter();
        sendAllServices();
    }

    public static void startGUI(IIDLCaDSEV3RMIMoveGrapper moveGrap, IIDLCaDSEV3RMIMoveHorizontal moveHorizontal, IIDLCaDSEV3RMIMoveVertical moveVertical, ICaDSRMIConsumer consumer, String ip, int port, boolean activateLogger) throws WebSocketException {
        WebGUI gui = new WebGUI(moveGrap, moveHorizontal, moveVertical, consumer, ip, port);
        Thread thread = new Thread(gui);
        thread.setName(gui.getClass().getSimpleName() + "-Thread");
        thread.start();
        
        Logger.init(LogLevel.DEBUG);
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.log(LogLevel.DEBUG, gui.getClass().getSimpleName() + "join() was interrupted.");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                processInputString(newMessages.take());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "take() was interrupted.");
            }
        }
    }

    private void processInputString(String webSocketInput) {
        JSONObject jsonObj = null;
        try {
            jsonObj = (JSONObject) new JSONParser().parse(webSocketInput);
        } catch (ParseException e) {
            e.printStackTrace();
            Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "parse() failed. webSocketInput: " + webSocketInput);
        }

        if (jsonObj != null) {
            String type = (String) jsonObj.get("type");
            String service = "";
            int value = 0;

            System.out.println(this.getClass().getSimpleName() + "-  InputMessage: " + webSocketInput);
            
            switch (type) {
                case "openIT":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveGrap.openIT(transactionNumber++);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "CaDS failed. webSocketInput: " + webSocketInput);
                    }
                    break;
                    
                case "closeIT":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveGrap.closeIT(transactionNumber++);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "CaDS failed. webSocketInput: " + webSocketInput);
                    }
                    break;
                    
                case "moveVerticalToPercent":
                    value = Integer.parseInt(jsonObj.get("percent").toString());
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveVertical.moveVerticalToPercent(transactionNumber++, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "CaDS failed. webSocketInput: " + webSocketInput);
                    }
                    break;
                    
                case "moveHorizontalToPercent":
                    value = Integer.parseInt(jsonObj.get("percent").toString());
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveHorizontal.moveMoveHorizontalToPercent(transactionNumber++, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "CaDS failed. webSocketInput: " + webSocketInput);
                    }
                    break;
                    
                case "stop":
                    service = (String) jsonObj.get("service");
                    try {
                        consumer.update(service);
                        moveHorizontal.stop(transactionNumber++);
                        moveVertical.stop(transactionNumber++);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "CaDS failed. webSocketInput: " + webSocketInput);
                    }
                    break;
                    
                case "request":                  
                    sendAllServices();

                    break;
               
                case "init":
                    System.out.println("init");
                    identifier = jsonObj.get("identifier").toString();
                    Logger.log(LogLevel.SYSTEM, "WebGUI: 6-digit identifier: " + identifier);
                    
                default :
                    Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "processInputString - default:  string: " + webSocketInput);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void sendAllServices() {
        JSONObject allServices = new JSONObject();
        JSONArray array = new JSONArray();
        
        allServices.put("type", "allServices");
        allServices.put("identifier", identifier);
        
        for(String context: guiUpdater.getContextSet()) {
            array.add(context);
        }
        
        allServices.put("services", array);
        
        try {
            ws.send(allServices.toJSONString() + "\n");
        } catch (WebSocketException e) {
            e.printStackTrace();
            Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "sendAllServices - failed ");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerAdapter() {
        JSONObject allServices = new JSONObject();
        JSONArray array = new JSONArray();
        
        allServices.put("type", "init");
        
        try {
            ws.send(allServices.toJSONString() + "\n");
        } catch (WebSocketException e) {
            e.printStackTrace();
            Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + "sendAllServices - failed ");
        }
    }
}
