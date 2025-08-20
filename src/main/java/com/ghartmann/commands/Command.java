package com.ghartmann.commands;

import com.ghartmann.service.AppContext;

public interface Command {

    void execute(String[] args, AppContext context);


}
