package fr.maxlego08.essentials.zutils;

import com.google.gson.Gson;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.maxlego08.essentials.api.Configuration;
import fr.maxlego08.essentials.api.ConfigurationFile;
import fr.maxlego08.essentials.api.commands.CommandManager;
import fr.maxlego08.essentials.api.economy.EconomyManager;
import fr.maxlego08.essentials.api.modules.ModuleManager;
import fr.maxlego08.essentials.api.placeholders.Placeholder;
import fr.maxlego08.essentials.api.storage.Persist;
import fr.maxlego08.essentials.api.storage.StorageManager;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ZPlugin extends JavaPlugin {

    protected final List<ConfigurationFile> configurationFiles = new ArrayList<>();
    protected CommandManager commandManager;
    protected StorageManager storageManager;
    protected ModuleManager moduleManager;
    protected Placeholder placeholder;
    protected Configuration configuration;
    protected EconomyManager economyManager;
    protected Gson gson;
    protected Persist persist;
    protected PlatformScheduler platformScheduler;

    protected void registerCommand(String command, VCommand vCommand, String... aliases) {
        this.commandManager.registerCommand(this, command, vCommand, Arrays.asList(aliases));
    }

    protected void registerConfiguration(ConfigurationFile configurationFile) {
        this.configurationFiles.add(configurationFile);
    }

    protected void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public boolean resourceExist(String resourcePath) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            return in != null;
        }
        return false;
    }

    public void saveResource(String resourcePath, String toPath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getFile());
            } else {
                File outFile = new File(getDataFolder(), toPath);
                int lastIndex = toPath.lastIndexOf(47);
                File outDir = new File(getDataFolder(), toPath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                        getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = Files.newOutputStream(outFile.toPath());
                        byte[] buf = new byte[1024];

                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException exception) {
                    getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, exception);
                }

            }
        } else throw new IllegalArgumentException("ResourcePath cannot be null or empty");
    }

    protected <T> T getProvider(Class<T> classz) {
        RegisteredServiceProvider<T> provider = getServer().getServicesManager().getRegistration(classz);
        if (provider == null) return null;
        return provider.getProvider();
    }

    public boolean isPaperVersion() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
