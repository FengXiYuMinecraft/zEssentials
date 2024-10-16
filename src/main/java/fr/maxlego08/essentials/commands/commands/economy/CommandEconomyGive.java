package fr.maxlego08.essentials.commands.commands.economy;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.economy.EconomyModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

public class CommandEconomyGive extends VCommand {


    public CommandEconomyGive(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(EconomyModule.class);
        this.setPermission(Permission.ESSENTIALS_ECO_GIVE);
        this.setDescription(Message.DESCRIPTION_ECO_GIVE);
        this.addSubCommand("give");
        this.addRequireArg("economy", (a, b) -> plugin.getEconomyManager().getEconomies().stream().map(Economy::getName).toList());
        this.addRequireOfflinePlayerNameArg();
        this.addRequireArg("amount", (a, b) -> Stream.of(10, 20, 30, 40, 50, 60, 70, 80, 90).map(String::valueOf).toList());
        this.addBooleanOptionalArg("silent");
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        String economyName = this.argAsString(0);
        String userName = this.argAsString(1);
        double amount = this.argAsDouble(2);
        boolean silent = this.argAsBoolean(3, false);

        EconomyManager economyManager = plugin.getEconomyManager();
        Optional<Economy> optional = economyManager.getEconomy(economyName);
        if (optional.isEmpty()) {
            message(sender, Message.COMMAND_ECONOMY_NOT_FOUND, "%name%", economyName);
            return CommandResultType.DEFAULT;
        }

        Economy economy = optional.get();
        fetchUniqueId(userName, uniqueId -> {

            economyManager.deposit(uniqueId, economy, new BigDecimal(amount));

            String economyFormat = economyManager.format(economy, amount);
            message(sender, Message.COMMAND_ECONOMY_GIVE_SENDER, "%player%", userName, "%economyFormat%", economyFormat);
            if (!silent) {
                message(uniqueId, Message.COMMAND_ECONOMY_GIVE_RECEIVER, "%economyFormat%", economyFormat);
            }
        });

        return CommandResultType.SUCCESS;
    }
}
