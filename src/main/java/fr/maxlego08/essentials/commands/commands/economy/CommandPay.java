package fr.maxlego08.essentials.commands.commands.economy;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.economy.Economy;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.maxlego08.essentials.api.economy.NumberMultiplicationFormat;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.Option;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.modules.economy.EconomyModule;
import fr.maxlego08.essentials.user.ZUser;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

public class CommandPay extends VCommand {

    public CommandPay(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(EconomyModule.class);
        this.setPermission(Permission.ESSENTIALS_PAY);
        this.setDescription(Message.DESCRIPTION_PAY);
        this.onlyPlayers();
        this.addRequirePlayerNameArg();
        this.addRequireArg("amount", (a, b) -> Stream.of(10, 20, 30, 40, 50, 60, 70, 80, 90).map(String::valueOf).toList());
        this.addOptionalArg("economy", (a, b) -> plugin.getEconomyManager().getEconomies().stream().map(Economy::getName).toList());
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        EconomyManager economyManager = plugin.getEconomyManager();
        String userName = this.argAsString(0);
        String amountAsString = this.argAsString(1);
        String economyName = this.argAsString(2, economyManager.getDefaultEconomy().getName());

        if (amountAsString.contains("-")) {
            message(sender, Message.COMMAND_PAY_NEGATIVE);
            return CommandResultType.DEFAULT;
        }

        final String sanitizedString = amountAsString.replaceAll("[^0-9.]", "");
        if (sanitizedString.isEmpty()) return CommandResultType.SYNTAX_ERROR;

        BigDecimal amount = new BigDecimal(amountAsString.replaceAll("[^0-9.]", ""));
        String format = amountAsString.replace(sanitizedString, "");
        Optional<NumberMultiplicationFormat> optional = economyManager.getMultiplication(format);
        if (optional.isPresent()) {
            NumberMultiplicationFormat numberMultiplicationFormat = optional.get();
            amount = amount.multiply(numberMultiplicationFormat.multiplication());
        }

        Optional<Economy> optionalEconomy = economyManager.getEconomy(economyName);
        if (optionalEconomy.isEmpty()) {
            message(sender, Message.COMMAND_ECONOMY_NOT_FOUND, "%name%", economyName);
            return CommandResultType.DEFAULT;
        }

        Economy economy = optionalEconomy.get();
        if (!economy.isPaymentEnabled()) {
            message(sender, Message.COMMAND_PAY_DISABLE, "%name%", economy.getDisplayName());
            return CommandResultType.DEFAULT;
        }

        if (amount.compareTo(economy.getMinPayValue()) < 0) {
            message(sender, Message.COMMAND_PAY_MIN, "%amount%", economyManager.format(economy, economy.getMinPayValue()));
            return CommandResultType.DEFAULT;
        }

        if (amount.compareTo(economy.getMaxPayValue()) > 0) {
            message(sender, Message.COMMAND_PAY_MAX, "%amount%", economyManager.format(economy, economy.getMaxPayValue()));
            return CommandResultType.DEFAULT;
        }

        if (userName.equalsIgnoreCase(player.getName())) {
            message(sender, Message.COMMAND_PAY_SELF);
            return CommandResultType.DEFAULT;
        }

        if (user.getBalance(economy).compareTo(amount) < 0) {
            message(sender, Message.COMMAND_PAY_NOT_ENOUGH);
            return CommandResultType.DEFAULT;
        }

        BigDecimal finalAmount = amount;
        this.fetchUniqueId(userName, uniqueId -> {

            checkOption(uniqueId, Option.PAY_DISABLE, isDisable -> {

                if (isDisable) {
                    message(sender, Message.COMMAND_PAY_DISABLED);
                    return;
                }

                if (economy.isConfirmInventoryEnabled() && finalAmount.compareTo(economy.getMinConfirmInventory()) > 0) {
                    User fakeUser = new ZUser(plugin, uniqueId);
                    fakeUser.setName(userName);
                    this.user.setTargetPay(fakeUser, economy, finalAmount);
                    plugin.getScheduler().runAtLocation(player.getLocation(), wrappedTask -> plugin.getInventoryManager().openInventory(player, plugin, "confirm_pay_inventory"));
                    return;
                }

                economyManager.pay(this.player.getUniqueId(), this.player.getName(), uniqueId, userName, economy, finalAmount);
            });
        });

        return CommandResultType.SUCCESS;
    }
}
