package com.ghartmann.commands;

import java.util.HashMap;
import java.util.Map;

import com.ghartmann.service.AppContext;

public class CommandManager {

    private final Map<String, Command> commands = new HashMap();
    private final AppContext context;

    public CommandManager(AppContext context){
        this.context = context;
    }

    public void registerCommand(String name, Command command){
        commands.put(name, command);
    }

    public void executeCommand(String name, String[] args){
        Command command = commands.get(name);
        if(command == null){
            context.getConsoleService().printLine("\u001B[31mComando desconhecido: " + name + "\u001B[0m");
            return;
        }
        command.execute(args, context);
    }


}
