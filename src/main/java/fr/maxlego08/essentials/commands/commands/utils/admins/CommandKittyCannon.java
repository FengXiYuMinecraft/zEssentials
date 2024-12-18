package fr.maxlego08.essentials.commands.commands.utils.admins;

import com.google.common.collect.Lists;
import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.entity.Cat;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CommandKittyCannon extends VCommand {

    private final Random random = new Random();

    public CommandKittyCannon(EssentialsPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ESSENTIALS_KITTY_CANNON);
        this.setDescription(Message.DESCRIPTION_KITTY_CANNON);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Location location = player.getEyeLocation();
        var cats = Lists.newArrayList(Registry.CAT_VARIANT);
        location.getWorld().spawn(location, Cat.class, cat -> {
            cat.setCatType(cats.get(random.nextInt(cats.size())));
            cat.setTamed(true);
            cat.setBaby();
            cat.setVelocity(location.getDirection().multiply(2));

            plugin.getScheduler().runAtLocationLater(location, cat::remove, 1, TimeUnit.SECONDS);
        });

        return CommandResultType.SUCCESS;
    }
}
