package cc.slack.features.commands.impl;

import cc.slack.features.commands.api.CMD;
import cc.slack.features.commands.api.CMDInfo;
import cc.slack.features.config.configManager;
import cc.slack.utils.other.FileUtil;
import cc.slack.utils.other.PrintUtil;

@CMDInfo(
        name = "config",
        alias = "c",
        description = "Save and Load Configs."
)
public class configCMD extends CMD {

    @Override
    public void onCommand(String[] args, String command) {
        switch (args.length) {
            case 0:
                configsMessage();
                commandsMessage();
                break;
            case 1:
                switch (args[0]) {
                    case "save":
                    case "s":
                        configManager.saveConfig(configManager.currentConfig);
                        break;
                    case "load":
                    case "l":
                    case "delete":
                    case "d":
                        commandsMessage();
                        break;
                    case "list":
                        configsMessage();
                        break;
                    case "folder":
                    case "f":
                        FileUtil.showFolder("/SlackClient/configs");
                        break;
                    default:
                        commandsMessage();
                        break;
                }
                break;
            case 2:
                switch (args[0]) {
                    case "save":
                    case "s":
                        configManager.saveConfig(args[1]);
                        break;
                    case "load":
                    case "l":
                        configManager.loadConfig(args[1]);
                        break;
                    case "delete":
                    case "d":
                        configManager.delete(args[1]);
                        break;
                    case "list":
                        configsMessage();
                        break;
                    case "folder":
                    case "f":
                        FileUtil.showFolder("/SlackClient/configs");
                        break;
                    default:
                        commandsMessage();
                        break;
                }
                break;
        }
    }

    private void configsMessage() {
        PrintUtil.message("§fShowing Slack configs:");
        for (String str : configManager.getConfigList()) {
            PrintUtil.message("§e " + str);
        }
    }

    private void commandsMessage() {
        PrintUtil.message("§fConfig commands:");
        PrintUtil.msgNoPrefix("§c> §f.config save [config name]");
        PrintUtil.msgNoPrefix("§c> §f.config load [config name]");
        PrintUtil.msgNoPrefix("§c> §f.config delete [config name]");
        PrintUtil.msgNoPrefix("§c> §f.config list");
        PrintUtil.msgNoPrefix("§c> §f.config folder");
    }

}
