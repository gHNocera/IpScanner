package com.ghartmann.service;

import com.ghartmann.commands.Command;

public class Banner implements Command{

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    @Override
    public void execute(String[] args, AppContext context) {
        ConsoleService console = context.getConsoleService();
        console.printLine(BLUE + "==================================" + RESET);
        console.printLine(GREEN + "       IP Scanner - Network Tool  " + RESET);
        console.printLine(BLUE + "==================================" + RESET);
        console.printLine(CYAN + "Comandos:" + RESET);
        console.printLine("  " + YELLOW + "help" + RESET + " -> Exibe este banner de ajuda");
        console.printLine("  " + YELLOW + "iplist" + RESET + " -> Lista IPs e MACs na rede");
        console.printLine("  " + YELLOW + "iptracker <IP>" + RESET + " -> Monitora pacotes de um IP específico");
        console.printLine("  " + YELLOW + "exit" + RESET + " -> Sai do programa");
        console.printLine(BLUE + "==================================" + RESET);
    }

}
