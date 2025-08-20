package com.ghartmann;

import com.ghartmann.commands.Command;
import com.ghartmann.service.AppContext;

public class Exit implements Command {

    @Override
    public void execute(String[] args, AppContext context) {
        context.getConsoleService().printLine("\u001B[32mEncerrando o programa...\u001B[0m");
        context.shutdown();
    }
}