package com.ghartmann.service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyboardListener {
    private final AppContext context;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final AtomicBoolean active = new AtomicBoolean(false);
    private Thread listenerThread;
    
    public KeyboardListener(AppContext context) {
        this.context = context;
    }
    
    public void startListening() {
        if (listening.get()) {
            return;
        }
        
        listening.set(true);
        
        listenerThread = new Thread(() -> {
            try {
                while (listening.get() && context.isRunning()) {
                    if (System.in.available() > 0) {
                        int key = System.in.read();
                        
                        // Check for ESC key (27)
                        if (key == 27 && active.get()) {
                            // If packet capture is running, stop it
                            if (context.getPacketCaptureService().isCapturing()) {
                                context.getPacketCaptureService().stopCapture();
                                context.getConsoleService().printLine("\u001B[32mCaptura interrompida pelo usuário (ESC)\u001B[0m");
                                // Deactivate the listener
                                active.set(false);
                                // Show banner again
                                new Banner().execute(new String[0], context);
                            }
                        }
                    }
                    
                    // Small delay to prevent high CPU usage
                    Thread.sleep(100);
                } 
            } catch (IOException | InterruptedException e) {
                // Ignore exceptions when shutting down
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    public void stopListening() {
        listening.set(false);
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
    }
    
    public void setActive(boolean active) {
        this.active.set(active);
    }
}