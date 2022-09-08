package net.pl3x.map.command.commands;

import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.pl3x.map.Pl3xMap;
import net.pl3x.map.command.CommandHandler;
import net.pl3x.map.command.Pl3xMapCommand;
import net.pl3x.map.command.Sender;
import net.pl3x.map.configuration.AdvancedConfig;
import net.pl3x.map.configuration.Config;
import net.pl3x.map.configuration.Lang;

public class ReloadCommand extends Pl3xMapCommand {
    public ReloadCommand(CommandHandler handler) {
        super(handler);
    }

    public void register() {
        getHandler().registerSubcommand(builder -> builder.literal("reload")
                .permission("pl3xmap.command.reload")
                .handler(this::execute));
    }

    public void execute(CommandContext<Sender> context) {
        Sender sender = context.getSender();

        Pl3xMap.api().disable();

        Config.reload();
        Lang.reload();
        AdvancedConfig.reload();

        Pl3xMap.api().enable();

        sender.send(Lang.COMMAND_RELOAD_SUCCESS,
                Placeholder.unparsed("version", Pl3xMap.api().getVersion()));
    }
}