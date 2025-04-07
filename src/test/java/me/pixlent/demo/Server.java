package me.pixlent.demo;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.callback.IJavetDirectCallable;
import com.caoccao.javet.interop.callback.JavetCallbackContext;
import com.caoccao.javet.interop.callback.JavetCallbackType;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import me.pixlent.api.JSNative;
import me.pixlent.demo.commands.GamemodeCommand;
import me.pixlent.Spool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import net.minestom.server.utils.time.TimeUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
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

        // Node logic
        Spool.moduleBuilder()
                .exportClass("Component", Component.class)
                .exportClass("TextComponent", TextComponent.class)
                .exportClass("TextColor", TextColor.class)
                .build("@adventure");

        JavetCallbackContext typeFunctionCallback = new JavetCallbackContext("type", JavetCallbackType.DirectCallNoThisAndResult,
                (IJavetDirectCallable.NoThisAndResult<Exception>) (v8Values) -> {
                    if (v8Values.length == 0) {
                        throw new IllegalArgumentException("No arguments passed to function \"type\"");
                    }
                    if (v8Values.length == 1) {
                        return JSNative.type(v8Values[0].asString());
                    }
                    V8ValueObject classes = Spool.INSTANCE.nodeRuntime.createV8ValueObject();
                    for (V8Value arg : v8Values) {
                        String name = Arrays.stream(arg.asString().split("\\.")).toList().getLast();
                        classes.set(name, JSNative.type(arg.asString()));
                    }
                    return classes;
                });

        try {
            Spool.moduleBuilder().exportObject("type", Spool.INSTANCE.nodeRuntime.createV8ValueFunction(typeFunctionCallback))
                    .build("@spool");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }

        Spool.INSTANCE.start();

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

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());
        //commandManager.register(new ReloadCommand());

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
