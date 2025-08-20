package com.ghartmann;

import java.util.Arrays;

import com.ghartmann.commands.CommandManager;
import com.ghartmann.service.AppContext;
import com.ghartmann.service.Banner;
import com.ghartmann.service.KeyboardListener;
import com.ghartmann.service.ListarIps;
import com.ghartmann.service.NetworkScanner;
import com.ghartmann.service.PacketCaptureService;

/**
 * Hello world!
 *
 */
public class App {

    static AppContext context = new AppContext();
    static Banner banner = new Banner();
    static NetworkScanner networkScanner = new NetworkScanner(context);
    static PacketCaptureService packetCaptureService = new PacketCaptureService(context);
    static KeyboardListener keyboardListener = new KeyboardListener(context);

    public static void main( String[] args ){
        context.setNetworkScanner(networkScanner);
        context.setPacketCaptureService(packetCaptureService);
        context.setKeyboardListener(keyboardListener);
        banner.execute(args, context);
        networkScanner.startScan();
        
        // Start keyboard listener for ESC key
        keyboardListener.startListening();
        
        iniciarLoopComandos();
    }

    private static final String RESET = "\u001B[0m";
    private static final String PURPLE = "\u001B[35m";
    
    private static void iniciarLoopComandos() {
        CommandManager commandManager = new CommandManager(context);
        // Registrando todos os comandos
        commandManager.registerCommand("help", new Banner());
        commandManager.registerCommand("iplist", new ListarIps());
        commandManager.registerCommand("iptracker", new IpTracker());
        commandManager.registerCommand("exit", new Exit());

        while (context.isRunning()) {
            context.getConsoleService().print(PURPLE + "\nComando> " + RESET);
            String line = context.getConsoleService().readLine();
            if (line == null || line.trim().isEmpty()) {
                // If we get an empty line, just show the prompt again
                continue;
            }

            String[] parts = line.trim().split("\\s+");
            String commandName = parts[0].toLowerCase();
            String[] commandArgs = Arrays.copyOfRange(parts, 1, parts.length);

            commandManager.executeCommand(commandName, commandArgs);
        }
    }
}
