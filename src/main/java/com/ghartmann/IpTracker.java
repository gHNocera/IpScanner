package com.ghartmann;

import com.ghartmann.commands.Command;
import com.ghartmann.service.AppContext;
import com.ghartmann.service.KeyboardListener;

public class IpTracker implements Command {

    @Override
    public void execute(String[] args, AppContext context) {
        if (args.length < 1) {
            context.getConsoleService().printLine("\u001B[31mErro: Você precisa especificar um IP para rastrear.\u001B[0m");
            context.getConsoleService().printLine("Uso: iptracker <endereço-ip>");
            return;
        }
        
        String ip = args[0];
        context.setTrackedIp(ip);
        
        // Stop any existing capture
        if (context.getPacketCaptureService().isCapturing()) {
            context.getPacketCaptureService().stopCapture();
        }
        
        // Enable keyboard listener for ESC key before starting capture
        KeyboardListener keyboardListener = context.getKeyboardListener();
        if (keyboardListener != null) {
            keyboardListener.setActive(true);
            context.getConsoleService().printLine("\u001B[33mPressione ESC para parar a captura\u001B[0m");
        }
        
        // Start new capture
        context.getPacketCaptureService().startCapture();
    }
}