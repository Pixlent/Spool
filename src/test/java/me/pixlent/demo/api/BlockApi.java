package me.pixlent.demo.api;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockApi {
    Random random = new Random();
    Block[] blocks;

    public BlockApi() {
        List<Block> blockList = new ArrayList<>(Block.values());

        blocks = blockList.toArray(new Block[0]);
    }

    public Block of(String name) {
        return Block.fromNamespaceId("minecraft:" + name);
    }

    public Block handler(Block block) {
        return block.withHandler(new BlockHandler() {
            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("spool");
            }

            @Override
            public boolean onInteract(@NotNull Interaction interaction) {
                interaction.getInstance().setBlock(interaction.getBlockPosition(), random());

                return BlockHandler.super.onInteract(interaction);
            }
        });
    }

    public Block random() {
        return blocks[random.nextInt(blocks.length)];
    }
}
