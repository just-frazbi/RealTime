package com.frazbi.realTime;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;

public class RealTime extends JavaPlugin {

    private BukkitRunnable timeTask;
    private boolean syncEnabled = true;
    private int updateInterval = 20; // Обновление каждую секунду (20 тиков)
    private ZoneId timeZone = ZoneId.systemDefault();

    @Override
    public void onEnable() {
        // Сохраняем конфиг по умолчанию
        saveDefaultConfig();

        // Загружаем настройки
        loadConfig();

        // Регистрируем команду
        getCommand("realtime").setExecutor(new RealTimeCommand(this));

        // Запускаем синхронизацию времени
        if (syncEnabled) {
            startTimeSync();
        }

        getLogger().info("RealTime плагин успешно загружен!");
        getLogger().info("Часовой пояс: " + timeZone.getId());
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

        // Загружаем часовой пояс
        String timeZoneStr = getConfig().getString("timezone", "auto");
        if (timeZoneStr.equalsIgnoreCase("auto")) {
            timeZone = ZoneId.systemDefault();
        } else {
            try {
                timeZone = ZoneId.of(timeZoneStr);
            } catch (Exception e) {
                getLogger().warning("Неверный часовой пояс: " + timeZoneStr + ". Используется системный.");
                timeZone = ZoneId.systemDefault();
            }
        }
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
        ZonedDateTime zonedNow = ZonedDateTime.now(timeZone);
        LocalTime realTime = zonedNow.toLocalTime();

        // Конвертируем реальное время в Minecraft время
        // В Minecraft: 0 тиков = 6:00, 6000 тиков = 12:00, 12000 тиков = 18:00, 18000 тиков = 00:00
        // Формула: (часы - 6) * 1000 + минуты * 16.67
        int hours = realTime.getHour();
        int minutes = realTime.getMinute();

        long minecraftTime = ((hours - 6) * 1000 + (long)(minutes * 16.67)) % 24000;
        if (minecraftTime < 0) {
            minecraftTime += 24000;
        }

        // Применяем время ко всем мирам
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

    public ZoneId getTimeZone() {
        return timeZone;
    }
}