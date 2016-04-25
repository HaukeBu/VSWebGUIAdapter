package adapter;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import io.cuckoo.websocket.nephila.WebSocket;
import io.cuckoo.websocket.nephila.WebSocketException;
import io.cuckoo.websocket.nephila.WebSocketListener;
import logger.LogLevel;
import logger.Logger;

public class WebSocketListenerImpl implements WebSocketListener{
    private BlockingQueue<String> newMessages;
    private WebSocket ws;
    
    public WebSocketListenerImpl(BlockingQueue<String> newMessages) {
        this.newMessages = newMessages;
    }

    @Override
    public void onConnect() {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " connected to server ");
    }

    @Override
    public void onClose() {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " disconnected from server");
    }

    @Override
    public void onMessage(String message) {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server has sent some text: " + message);
        newMessages.add(message); 
    }

    @Override
    public void onMessage(byte[] message) {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server has sent some bytes: " + Arrays.toString(message));
    }

    @Override
    public void onMessageChunk(String messageChunk, boolean isFinalChunk) {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server has sent a text chunk: " + messageChunk + " # final chunk: " + isFinalChunk);
    }

    @Override
    public void onMessageChunk(byte[] messageChunk, boolean isFinalChunk) {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server has sent a binary chunk: " + Arrays.toString(messageChunk) + " # final chunk: " + isFinalChunk);
    }

    @Override
    public void onPing() {
        try {
            ws.pong();
        } catch (WebSocketException ignored) {}
    }

    @Override
    public void onPing(byte[] data) {
        try {
            ws.pong(data);
        } catch (WebSocketException ignored) {}
    }

    @Override
    public void onPong() {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server is still alive");
    }

    @Override
    public void onPong(byte[] data) {
        Logger.log(LogLevel.DEBUG, this.getClass().getSimpleName() + " server is still alive, data: " + Arrays.toString(data));
    }

    public void setWs(WebSocket ws) {
        this.ws = ws;
    }
}
