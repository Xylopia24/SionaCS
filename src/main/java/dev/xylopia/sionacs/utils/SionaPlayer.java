package dev.xylopia.sionacs.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SionaPlayer extends Player {
    public SionaPlayer(Level level, BlockPos blockPos) {
        super(level, blockPos, 0, new GameProfile(UUID.randomUUID(), "SionaPlayer"));
    }

    public SionaPlayer(Level level, BlockPos blockPos, UUID uuid) {
        super(level, blockPos, 0, new GameProfile(uuid, "SionaPlayer"));
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
