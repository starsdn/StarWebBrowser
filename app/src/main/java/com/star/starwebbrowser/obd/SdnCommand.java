package com.star.starwebbrowser.obd;

import com.sdn.obd.commands.ObdCommand;

public class SdnCommand extends ObdCommand {

    private String command;

    public SdnCommand(String command) {
        super(command);
        this.command=command;
    }

    @Override
    public String getFormattedResult() {
        return "自定义命令结果: " + this.getResult();
    }

    @Override
    public String getName() {
        return this.command;
    }
}