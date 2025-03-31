package me.pixlent.demo;

import me.pixlent.SpoolApi;
import me.pixlent.demo.api.*;
import me.pixlent.demo.commands.GamemodeCommand;
import me.pixlent.demo.commands.ReloadCommand;
import me.pixlent.module.SpoolModule;
import me.pixlent.utils.FileUtils;
import me.pixlent.utils.ReflectionUtils;
import me.pixlent.utils.ValueUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import org.graalvm.polyglot.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DemoServer {
    private static final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();

        instance.setGenerator(generator -> {
            Vec start = new Vec(-25, 63, -25);
            Vec end = new Vec(25, 64, 25);
            GenerationUnit fork = generator.fork(start, end);

            fork.modifier().fill(start, end, Block.STONE);
            fork.modifier().setBlock(0, 63, 0, Block.BEDROCK);
        });

        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);

        SpoolApi.INSTANCE.subscribeToReload(event -> {
            if (event.state().equals(SpoolApi.ReloadSignal.LoadState.PRE_CLOSE)) EventsApi.getInstance().clear();
        });

        SpoolApi.INSTANCE.subscribeToIndex(event -> {
            if (!event.stage().equals(SpoolApi.IndexSignal.IndexStage.REGISTER)) return;

            SpoolModule apiModule = new SpoolModule();

            apiModule.export("system", new SystemApi());
            apiModule.export("math", new MathApi());
            apiModule.export("eventHandler", MinecraftServer.getGlobalEventHandler());
            apiModule.export("events", EventsApi.getInstance());
            apiModule.export("blocks", new BlockApi());
            apiModule.export("world", instance);

            event.register("@api", apiModule);

            SpoolModule adventureModule = new SpoolModule();
            ValueUtils.packageClasses("net.kyori.adventure").forEach(adventureModule::export);
            event.register("@adventure", adventureModule);

            SpoolModule minestomModule = new SpoolModule();
            ValueUtils.packageClasses("net.minestom.server").forEach(minestomModule::export);
            event.register("@minestom", minestomModule);
        });

        SpoolApi.INSTANCE.subscribeToModuleResolver(event -> {
            if (event.moduleName.startsWith("@minestom/")) {
                String modulePath = event.moduleName.substring(1);
                Value jsObject = ValueUtils.emptyObject();

                String[] parts = modulePath.split("/", 2);
                if (parts.length > 1) {
                    parts[1] = parts[1].replace("/", ".");
                }

                String packagePath = "net.minestom.server";

                List<String> entries = ReflectionUtils.findClassNamesInPackage(packagePath + "." + parts[1]);

                entries.forEach(c -> {
                    c = c.replaceFirst(packagePath + "." + parts[1] + ".", "");
                    if (!c.contains(".")) {
                        try {
                            jsObject.putMember(c, ValueUtils.type(packagePath + "." + parts[1] + "." + c));
                        } catch (Exception _) {}
                    }
                });

                event.module = jsObject;
            }
        });

        SpoolApi.INSTANCE.subscribeToModuleResolver(event -> {
            System.out.println(event.module);
            if (event.moduleName.startsWith("@adventure/")) {
                String modulePath = event.moduleName.substring(1);
                Value jsObject = ValueUtils.emptyObject();

                String[] parts = modulePath.split("/", 2);
                if (parts.length > 1) {
                    parts[1] = parts[1].replace("/", ".");
                }

                String packagePath = "net.kyori.adventure";

                List<String> entries = ReflectionUtils.findClassNamesInPackage(packagePath + "." + parts[1]);

                entries.forEach(c -> {
                    c = c.replaceFirst(packagePath + "." + parts[1] + ".", "");
                    if (!c.contains(".")) {
                        try {
                            jsObject.putMember(c, ValueUtils.type(packagePath + "." + parts[1] + "." + c));
                        } catch (Exception _) {}
                    }
                });

                event.module = jsObject;
            }
        });

        SpoolApi.INSTANCE.index(FileUtils.getBasePath().resolve("src/scripts"));

        Watcher watcher = new Watcher();
        watcher.watch(FileUtils.getBasePath().resolve("src/scripts"), TaskSchedule.seconds(1), SpoolApi.INSTANCE::reload);

        SpoolApi.INSTANCE.open();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0.5, 64, 0.5));
        });

        eventHandler.addListener(EntitySpawnEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            player.addEffect(new Potion(PotionEffect.NIGHT_VISION, 0, -1));
            player.setGameMode(GameMode.CREATIVE);
            player.setPermissionLevel(4);
        });

        eventHandler.addListener(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();

            if (!event.getRawMessage().startsWith(">")) return;

            AtomicReference<Value> value = new AtomicReference<>();
            Component returnValue = Component.text("No value was returned");

            try {
                value.set(SpoolApi.INSTANCE.execute(event.getRawMessage().replaceFirst(">", "")));
            } catch (Exception e) {
                returnValue = Component.text(e.getMessage()).color(TextColor.fromHexString("#fff066"));
                Arrays.stream(e.getStackTrace()).forEach(System.err::println);
            }

            if (value.get() != null) {
                if (value.get().isString()) {
                    returnValue = Component.text(value.get().asString());
                } else if (value.get().isNumber()) {
                    if (value.get().asDouble() % 1 == 0) {
                        if (value.get().fitsInInt()) {
                            returnValue = Component.text(value.get().asInt());
                        } else if (value.get().fitsInLong()) {
                            returnValue = Component.text(value.get().asLong());
                        } else returnValue = Component.text("Number value too big");
                    } else returnValue = Component.text(value.get().asDouble());
                } else if (value.get().isBoolean()) {
                    returnValue = Component.text(value.get().asBoolean());
                }
            }

            player.sendMessage(Component.text("Return value: ").color(TextColor.fromHexString("#fff066"))
                    .append(returnValue.color(TextColor.fromHexString("#ff2b2b"))));

            event.setCancelled(true);
        });

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());
        commandManager.register(new ReloadCommand());

        MojangAuth.init();

        minecraftServer.start("0.0.0.0", 25565);

        eventHandler.addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));

        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (LAST_TICK.get() == null || MinecraftServer.getConnectionManager().getOnlinePlayerCount() == 0)
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= (long) 1e6; // bytes to MB

            TickMonitor tickMonitor = LAST_TICK.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
}
