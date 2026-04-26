package com.frazbi.realTime;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.util.logging.Level;

public class RealTime extends JavaPlugin {
    
    private BukkitRunnable timeTask;
    private boolean syncEnabled = true;
    private int updateInterval = 20;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();

        loadConfig();

        getCommand("realtime").setExecutor(new RealTimeCommand(this));

        if (syncEnabled) {
            startTimeSync();
        }
        
        getLogger().info("RealTime плагин успешно загружен!");
    }
    
    @Override
    public void onDisable() {
        if (timeTask != null) {
            timeTask.cancel();
        }
        getLogger().info("RealTime плагин выгружен!");
    }
    
    public void loadConfig() {
        reloadConfig();
        syncEnabled = getConfig().getBoolean("enabled", true);
        updateInterval = getConfig().getInt("update-interval", 20);
    }
    
    public void startTimeSync() {
        if (timeTask != null) {
            timeTask.cancel();
        }
        
        timeTask = new BukkitRunnable() {
            @Override
            public void run() {
                syncTime();
            }
        };
        
        timeTask.runTaskTimer(this, 0L, updateInterval);
        getLogger().log(Level.INFO, "Синхронизация времени запущена!");
    }
    
    public void stopTimeSync() {
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
            getLogger().log(Level.INFO, "Синхронизация времени остановлена!");
        }
    }
    
    private void syncTime() {
        LocalTime realTime = LocalTime.now();

        int hours = realTime.getHour();
        int minutes = realTime.getMinute();
        
        long minecraftTime = ((hours - 6) * 1000 + (long)(minutes * 16.67)) % 24000;
        if (minecraftTime < 0) {
            minecraftTime += 24000;
        }

        for (World world : Bukkit.getWorlds()) {
            if (getConfig().getStringList("enabled-worlds").isEmpty() || 
                getConfig().getStringList("enabled-worlds").contains(world.getName())) {
                world.setTime(minecraftTime);
            }
        }
    }
    
    public boolean isSyncEnabled() {
        return syncEnabled;
    }
    
    public void setSyncEnabled(boolean enabled) {
        this.syncEnabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();
        
        if (enabled) {
            startTimeSync();
        } else {
            stopTimeSync();
        }
    }
}
