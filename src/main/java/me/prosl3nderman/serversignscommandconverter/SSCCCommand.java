package me.prosl3nderman.serversignscommandconverter;

import de.czymm.serversigns.parsing.command.ServerSignCommand;
import de.czymm.serversigns.signs.ServerSign;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SSCCCommand implements CommandExecutor {

    private ServerSignsCommandConverter plugin;

    public SSCCCommand(ServerSignsCommandConverter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Must be sent by a player!");
            return true;
        }
        Player player = (Player) commandSender;
        if (!player.hasPermission("SSCC.convert") && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.DARK_GREEN + "--------------- " + ChatColor.GREEN + "ServerSignsCommand Help" + ChatColor.DARK_GREEN + " ---------------");
            player.sendMessage(ChatColor.DARK_GREEN + "/sscc convertAllSignCommands: " + ChatColor.GREEN + "Command to convert all sign commands from old commands to new commands. Configure old and new commands in the config.yml located in the plugin's" +
                    " folder, 'ServerSignsCommandConverter'.");
            player.sendMessage(ChatColor.DARK_GREEN + "/sscc reload: " + ChatColor.GREEN + "Command to reload the config.");
            player.sendMessage(ChatColor.DARK_GREEN + "/sscc debug: " + ChatColor.GREEN + "Command to toggle on debug messages (used to troubleshoot when a command isn't converting properly). ");
            player.sendMessage(ChatColor.DARK_RED + "WARNING: " + ChatColor.RED + "this" +
                    " command will flood your chat if you have more than 10 server signs. Please use only under the direction of ProSl3nderMan, in which case you will probably need to send them your server log when you try converting all signs.");
            return true;
        }

        if (args[0].equalsIgnoreCase("convertAllSignCommands")) {
            player.sendMessage(ChatColor.GOLD + "Converting all sign commands......");
            int changedSigns = 0;
            for (ServerSign SS : plugin.SSP.serverSignsManager.getSigns()) {
                int index = 0;
                for (ServerSignCommand SSC : SS.getCommands()) {
                    String newCommand = getNewCommand(SSC.getUnformattedCommand());

                    broadcastIfDebug(ChatColor.DARK_GREEN + "Unformatted Command: " + ChatColor.GREEN + SSC.getUnformattedCommand());

                    ServerSignCommand newSSC = new ServerSignCommand(SSC.getType(), newCommand);
                    newSSC.setAlwaysPersisted(SSC.isAlwaysPersisted());
                    newSSC.setDelay(SSC.getDelay());
                    newSSC.setGrantPermissions(SSC.getGrantPermissions());
                    newSSC.setInteractValue(SSC.getInteractValue());

                    if (!SSC.getUnformattedCommand().equalsIgnoreCase(newSSC.getUnformattedCommand())) {
                        SS.addCommand(newSSC);
                        SS.removeCommand(index);
                        changedSigns++;
                    }
                    index++;
                }
                plugin.SSP.serverSignsManager.save(SS);
            }
            player.sendMessage(ChatColor.GOLD + "Sign commands changed: " + ChatColor.WHITE + changedSigns);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GOLD + "Config.yml has been reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("debug")) {
            plugin.toggleDebug();
            player.sendMessage(ChatColor.GOLD + "Debug mode was set to " + (plugin.debug ? ChatColor.GREEN : ChatColor.RED) + plugin.debug);
            return true;
        }
        return true;
    }

    private String getNewCommand(String oldCommand) {
        HashMap<String,String> replaceCommands = getReplaceCommands();
        for (String oldCmd : replaceCommands.keySet()) {
            broadcastIfDebug(ChatColor.DARK_GREEN + "oldCmd: " + ChatColor.GREEN + oldCmd);
            if (oldCmd.equalsIgnoreCase(oldCommand))
                return replaceCommands.get(oldCmd);
            else if (oldCmd.contains("<value") && oldCmd.split(" ").length == oldCommand.split(" ").length) {
                String newOldCmd = oldCmd;
                HashMap<Integer, String> values = new HashMap<>();
                int arg = 0;
                for (String word : oldCmd.split(" ")) {
                    broadcastIfDebug(ChatColor.DARK_GREEN + "word1: " + ChatColor.GREEN + word);
                    if (word.contains("<value")) {
                        broadcastIfDebug(ChatColor.DARK_GREEN + "value word found: " + ChatColor.GREEN + word);
                        broadcastIfDebug(ChatColor.DARK_GREEN + "value word found: " + ChatColor.GREEN + getValueIntFromString(word));
                        broadcastIfDebug(ChatColor.DARK_GREEN + "value word found: " + ChatColor.GREEN + oldCommand.split(" ")[arg]);
                        broadcastIfDebug(ChatColor.DARK_GREEN + "value word found arg: " + ChatColor.GREEN + arg);
                        values.put(getValueIntFromString(word), oldCommand.split(" ")[arg]);
                        newOldCmd = newOldCmd.replace(word, oldCommand.split(" ")[arg]);
                    }
                    arg++;
                }
                broadcastIfDebug(ChatColor.DARK_GREEN + "newOldCmd: " + ChatColor.GREEN + newOldCmd);
                if (newOldCmd.equalsIgnoreCase(oldCommand)) {
                    String newCommand = replaceCommands.get(oldCmd);
                    arg = 0;
                    for (String word : replaceCommands.get(oldCmd).split(" ")) {
                        broadcastIfDebug(ChatColor.DARK_GREEN + "word2: " + ChatColor.GREEN + word);
                        if (word.contains("<value"))
                            newCommand = newCommand.replace(word, values.get(getValueIntFromString(word)));
                        arg++;
                    }
                    broadcastIfDebug(ChatColor.DARK_GREEN + "newOldCmd: " + ChatColor.GREEN + newOldCmd);
                    return newCommand;
                }
            }
        }
        return oldCommand;
    }

    private Integer getValueIntFromString(String value) {
        return Integer.parseInt(value.replaceAll("<value", "").replaceAll(">", ""));
    }

    private HashMap<String, String> getReplaceCommands() {
        HashMap<String, String> replaceCommands = new HashMap<>(); //String = original command (command being replaced with) ; String = new command (command the replaced is being replaced with).
        for (String commands : plugin.getConfig().getStringList("convertCommands")) {
            String originalCommand = commands.split("\\|")[0];
            String newCommand = commands.split("\\|")[1];

            replaceCommands.put(originalCommand, newCommand);
        }
        return replaceCommands;
    }

    private void broadcastIfDebug(String message) {
        if (plugin.debug)
            Bukkit.broadcastMessage(message);
    }
}
