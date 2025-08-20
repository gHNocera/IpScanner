package com.ghartmann.service;

import com.ghartmann.commands.Command;

public class ListarIps implements Command{

    @Override
    public void execute(String[] args, AppContext context) {
        if (context.getIpToMac().isEmpty()) {
            context.getConsoleService().printLine("\u001B[33mNenhum dispositivo encontrado ainda. A varredura está em andamento...\u001B[0m");
            return;
        }
        context.getConsoleService().printLine("\u001B[34m\n=== Dispositivos na Rede ===\u001B[0m");
        context.getIpToMac().forEach((ip, mac) -> {
            context.getConsoleService().printLine(String.format("\u001B[32m%-15s\u001B[0m | \u001B[33m%-17s\u001B[0m", ip, mac));
        });
    }

}
