package fr.maxlego08.essentials.commands.commands.hologram.text;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.hologram.Hologram;
import fr.maxlego08.essentials.api.hologram.HologramLine;
import fr.maxlego08.essentials.api.hologram.HologramManager;
import fr.maxlego08.essentials.api.hologram.HologramType;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.commands.commands.hologram.VCommandHologram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class CommandHologramSetLine extends VCommandHologram {

    public CommandHologramSetLine(EssentialsPlugin plugin) {
        super(plugin, HologramType.TEXT);
        this.setPermission(Permission.ESSENTIALS_HOLOGRAM_SET_LINE);
        this.setDescription(Message.DESCRIPTION_HOLOGRAM_SET_LINE);
        this.addSubCommand("setline");
        this.addRequireArgHologram("line", (sender, hologram) -> lineToList(hologram));
        this.addRequireArg("text", (sender, args) -> {
            HologramManager manager = this.plugin.getHologramManager();
            if (args.length >= 2) {
                String hologramName = args[1];
                Optional<Hologram> optional = manager.getHologram(hologramName);
                if (optional.isPresent()) {
                    Hologram hologram = optional.get();
                    try {
                        int line = Integer.parseInt(args[2]);
                        var optionalLine = hologram.getHologramLine(line);
                        if (optionalLine.isPresent()) {
                            HologramLine hologramLine = optionalLine.get();
                            return Collections.singletonList(hologramLine.getText());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            return new ArrayList<>();
        });
        this.setExtendedArgs(true);
    }

    @Override
    protected void perform(EssentialsPlugin plugin, Hologram hologram, HologramManager manager) {

        int line = this.argAsInteger(1);
        String text = this.getArgs(3);

        Optional<HologramLine> optional = hologram.getHologramLine(line);
        if (optional.isEmpty()) {
            message(sender, Message.HOLOGRAM_LINE_DOESNT_EXIST, "%name%", hologram.getName(), "%line%", line);
            return;
        }

        HologramLine hologramLine = optional.get();
        hologramLine.setText(text);
        hologram.updateForAllPlayers();

        manager.saveHologram(hologram);

        message(sender, Message.HOLOGRAM_SET_LINE, "%name%", hologram.getName(), "%line%", line);
    }
}
