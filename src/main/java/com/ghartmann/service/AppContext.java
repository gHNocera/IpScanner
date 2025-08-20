package com.ghartmann.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class AppContext {

    private final ConsoleService consoleService = new ConsoleService();
    private final Map<String, String> ipToMac = new ConcurrentHashMap<>();
    private final Map<String, String> macToVendor = new ConcurrentHashMap<>();
    private final Map<String, String> ipToDomain = new ConcurrentHashMap<>();
    private volatile String trackedIp = null;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private NetworkScanner networkScanner;
    private PacketCaptureService packetCaptureService;
    private KeyboardListener keyboardListener;

    public ConsoleService getConsoleService() {
        return consoleService;
    }
    public Map<String, String> getIpToMac() {
        return ipToMac;
    }
    public Map<String, String> getMacToVendor() {
        return macToVendor;
    }
    public Map<String, String> getIpToDomain() {
        return ipToDomain;
    }
    public String getTrackedIp() {
        return trackedIp;
    }
    public void setTrackedIp(String trackedIp) {
        this.trackedIp = trackedIp;
    }

    public boolean isRunning(){
        return running.get();
    }

    public void shutdown(){
        running.set(false);
        if (networkScanner != null) {
            networkScanner.shutdown();
        }
        if (packetCaptureService != null) {
            packetCaptureService.shutdown();
        }
        if (keyboardListener != null) {
            keyboardListener.stopListening();
        }
        consoleService.close();
    }
    
    public void setNetworkScanner(NetworkScanner scanner) {
        this.networkScanner = scanner;
    }
    
    public void setPacketCaptureService(PacketCaptureService service) {
        this.packetCaptureService = service;
    }
    
    public PacketCaptureService getPacketCaptureService() {
        return packetCaptureService;
    }
    
    public void setKeyboardListener(KeyboardListener listener) {
        this.keyboardListener = listener;
    }
    
    public KeyboardListener getKeyboardListener() {
        return keyboardListener;
    }







}
