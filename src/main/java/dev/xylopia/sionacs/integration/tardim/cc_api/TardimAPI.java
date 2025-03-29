package dev.xylopia.sionacs.integration.tardim.cc_api;

// Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Mojang/Minecraft imports
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

// Tardim imports - corrected paths for public version
import com.swdteam.common.command.tardim.CommandTravel;
import com.swdteam.common.init.TRDSounds;
import com.swdteam.common.init.TardimRegistry;
import com.swdteam.common.item.ItemTardim;
import com.swdteam.tardim.TardimData;
import com.swdteam.tardim.TardimData.Location;
import com.swdteam.tardim.TardimManager;
import com.swdteam.main.Tardim;

// ComputerCraft imports
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.ObjectLuaTable;

// SionaCS imports
import dev.xylopia.sionacs.integration.tardim.peripheral.TardimPeripheralBlockEntity;
import dev.xylopia.sionacs.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API class that exposes TARDIM functionality to ComputerCraft.
 * This contains all the methods that can be called from Lua code.
 */
public class TardimAPI {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID + ":TardimAPI");
    private final TardimPeripheralBlockEntity blockEntity;

    public TardimAPI(TardimPeripheralBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Gets the TARDIM data, throwing an exception if not available.
     * @return The TARDIM data
     * @throws LuaException If the peripheral is not in a valid TARDIM
     */
    private TardimData getTardimData() throws LuaException {
        TardimData data = blockEntity.getTardimData();
        
        // Check if we have TARDIM data at all
        if (data == null) {
            throw new LuaException("Peripheral is not inside a valid TARDIM");
        }
        
        if (data.getCurrentLocation() == null || data.getOwnerName() == null) {
            throw new LuaException("TARDIM data is incomplete or invalid");
        }
        
        return data;
    }

    /**
     * Gets the server level this peripheral is in.
     * @return The server level
     * @throws LuaException If the level is not available or is client-side
     */
    private ServerLevel getServerLevel() throws LuaException {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            throw new LuaException("Cannot access server level");
        }
        return (ServerLevel) level;
    }

    // === INFORMATION METHODS ===

    /**
     * Return how much fuel is left in the TARDIM
     *
     * @return Fuel left (Out of 100)
     */
    @LuaFunction(mainThread = true)
    public final double getFuel() throws LuaException {
        return getTardimData().getFuel();
    }

    /**
     * Get how much fuel it would take to travel to the destination
     * @return Amount of fuel needed (Out of 100)
     */
    @LuaFunction(mainThread = true)
    public final double calculateFuelForJourney() throws LuaException {
        TardimData data = getTardimData();

        if (data.getTravelLocation() == null) return 0;

        Location curr = data.getCurrentLocation();
        Location dest = data.getTravelLocation();

        double fuel = 0.0;

        if (!curr.getLevel().equals(dest.getLevel())) {
            fuel = 10.0;
        }

        Vec3 posA = new Vec3(curr.getPos().getX(), curr.getPos().getY(), curr.getPos().getZ());
        Vec3 posB = new Vec3(dest.getPos().getX(), dest.getPos().getY(), dest.getPos().getZ());
        fuel += posA.distanceTo(posB) / 100.0;
        if (fuel > 100.0) fuel = 100.0;

        return fuel;
    }

    /**
     * Check whether the TARDIM is locked
     * @return true if locked, false if not
     */
    @LuaFunction(mainThread = true)
    public final boolean isLocked() throws LuaException {
        return getTardimData().isLocked();
    }

    /**
     * Check whether the TARDIM is in flight
     * @return true if in flight, false if not
     */
    @LuaFunction(mainThread = true)
    public final boolean isInFlight() throws LuaException { 
        return getTardimData().isInFlight(); 
    }

    /**
     * Gets timestamp of when we entered flight
     * @return Timestamp if in flight, -1 if not
     */
    @LuaFunction(mainThread = true)
    public final long getTimeEnteredFlight() throws LuaException {
        TardimData data = getTardimData();
        if (!data.isInFlight()) {
            return -1;
        }
        return data.getTimeEnteredFlight();
    }

    /**
     * Get username of the TARDIM's owner
     * @return String of the owner's username
     */
    @LuaFunction(mainThread = true)
    public final String getOwnerName() throws LuaException {
        return getTardimData().getOwnerName();
    }

    /**
     * Get the current location of the TARDIM
     * @return Table of the current location with dimension, position, and facing
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getCurrentLocation() throws LuaException {
        Location loc = getTardimData().getCurrentLocation();
        return new ObjectLuaTable(Map.of(
            "dimension", loc.getLevel().location().toString(),
            "pos", new ObjectLuaTable(Map.of(
                "x", loc.getPos().getX(),
                "y", loc.getPos().getY(),
                "z", loc.getPos().getZ()
            )),
            "facing", loc.getFacing().toString()
        ));
    }

    /**
     * Get the destination location of the TARDIM
     * @return Table of the destination or null if no destination
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getTravelLocation() throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }
        Location loc = data.getTravelLocation();
        return new ObjectLuaTable(Map.of(
            "dimension", loc.getLevel().location().toString(),
            "pos", new ObjectLuaTable(Map.of(
                "x", loc.getPos().getX(),
                "y", loc.getPos().getY(),
                "z", loc.getPos().getZ()
            )),
            "facing", loc.getFacing().toString()
        ));
    }

    /**
     * Get list of the TARDIM owner's companions
     * @return Table containing the usernames of the companions
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getCompanions() throws LuaException {
        TardimData data = getTardimData();
        Map<Integer, String> companions = new HashMap<>();
        for (int i = 0; i < data.getCompanions().size(); i++) {
            companions.put(i + 1, data.getCompanions().get(i).getUsername());
        }
        return new ObjectLuaTable(companions);
    }

    // === CONTROL METHODS ===

    /**
     * Lock/unlock the TARDIM
     * @param locked    true to lock, false to unlock
     */
    @LuaFunction(mainThread = true)
    public final void setLocked(boolean locked) throws LuaException {
        getTardimData().setLocked(locked);
    }

    /**
     * Set dimension for the TARDIM to travel to
     * @param dimension String of the dimension e.g. "minecraft:overworld"
     */
    @LuaFunction(mainThread = true)
    public final void setDimension(String dimension) throws LuaException {
        TardimData data = getTardimData();

        // Try to resolve dimension name variations using CommandTravel's method
        String key = dimension;
        dimension = toTitleCase(dimension);
        if (TardimManager.DIMENSION_MAP.containsKey(dimension)) {
            key = TardimManager.DIMENSION_MAP.get(dimension);
        } else {
            dimension = dimension.toLowerCase();
        }

        // Validate the dimension path using CommandTravel's method
        if (!CommandTravel.isValidPath(key)) {
            throw new LuaException("Invalid dimension format");
        }
        
        @SuppressWarnings("removal")
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }

        data.getTravelLocation().setLocation(dim);
    }

    /**
     * Set the destination's coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    @LuaFunction(mainThread = true)
    public final void setTravelLocation(int x, int y, int z) throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }

        data.getTravelLocation().setPosition(x, y, z);
    }

    /**
     * Set the rotation of the TARDIM's door
     * @param rotation  String of the door rotation ("north", "south", "east", "west")
     */
    @LuaFunction(mainThread = true)
    public final void setDoorRotation(String rotation) throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }
        
        switch (rotation.toLowerCase()) {
            case "north" -> data.getTravelLocation().setFacing(Direction.NORTH);
            case "east" -> data.getTravelLocation().setFacing(Direction.EAST);
            case "south" -> data.getTravelLocation().setFacing(Direction.SOUTH);
            case "west" -> data.getTravelLocation().setFacing(Direction.WEST);
            default -> throw new LuaException("Invalid door rotation. Must be north, east, south, or west");
        }

        data.save();
    }

    /**
     * Get the rotation of the TARDIM's door
     * @return String of the door rotation ("north", "south", "east", "west")
     */
    @LuaFunction(mainThread = true)
    public final String getDoorRotation() throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            return data.getCurrentLocation().getFacing().toString().toLowerCase();
        }
        
        Direction rotation = data.getTravelLocation().getFacing();
        return rotation.toString().toLowerCase();
    }

    /**
     * Toggle the door rotation (north -> east -> south -> west -> north)
     */
    @LuaFunction(mainThread = true)
    public final void toggleDoorRotation() throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }

        if (data.getTravelLocation().getFacing() == null) {
            data.getTravelLocation().setFacing(Direction.NORTH);
        }

        Direction currentFacing = data.getTravelLocation().getFacing();
        Direction newFacing;
        
        switch (currentFacing) {
            case NORTH -> newFacing = Direction.EAST;
            case EAST -> newFacing = Direction.SOUTH;
            case SOUTH -> newFacing = Direction.WEST;
            case WEST -> newFacing = Direction.NORTH;
            default -> newFacing = Direction.NORTH;
        }
        
        data.getTravelLocation().setFacing(newFacing);
        data.save();
    }

    // === TRAVEL METHODS ===

    /**
     * Dematerialize the TARDIM (take off)
     */
    @LuaFunction(mainThread = true)
    public final void demat() throws LuaException {
        ServerLevel level = getServerLevel();
        TardimData data = getTardimData();

        if (data.isInFlight()) {
            throw new LuaException("TARDIM is already in flight");
        }
        
        Location loc = data.getCurrentLocation();
        @SuppressWarnings("unchecked")
        ServerLevel currentLevel = level.getServer().getLevel(loc.getLevel());
        if (currentLevel == null) {
            throw new LuaException("Cannot access TARDIM's current dimension");
        }
        
        // Destroy the TARDIM using public version's method
        ItemTardim.destroyTardim(currentLevel, loc.getPos(), Direction.NORTH);
        data.setInFlight(true);
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }
        
        data.setTimeEnteredFlight(); // Set the takeoff time
        
        // Play takeoff sound - updated for public version
        level.playSound(null, blockEntity.getBlockPos(), TRDSounds.TARDIM_TAKEOFF.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        data.save();
    }

    /**
     * Materialize the TARDIM at the destination (land)
     */
    @LuaFunction(mainThread = true)
    public final void remat() throws LuaException {
        ServerLevel level = getServerLevel();
        TardimData data = getTardimData();

        if (!data.isInFlight()) {
            throw new LuaException("TARDIM is not in flight");
        }
        
        // Check if enough time has passed since takeoff (10 seconds)
        if (data.getTimeEnteredFlight() >= System.currentTimeMillis() / 1000L - 10L) {
            throw new LuaException("TARDIM is still taking off");
        }
        
        Location loc = data.getTravelLocation();
        if (loc == null) {
            throw new LuaException("No destination set");
        }
        
        @SuppressWarnings("unchecked")
        ServerLevel destinationLevel = level.getServer().getLevel(loc.getLevel());
        if (destinationLevel == null) {
            throw new LuaException("Invalid destination dimension");
        }
        
        // Check fuel requirements
        @SuppressWarnings("unchecked")
        double fuel = data.calculateFuelForJourney(
                level.getServer().getLevel(data.getCurrentLocation().getLevel()), 
                destinationLevel, 
                data.getCurrentLocation().getPos(), 
                loc.getPos()
        );
        
        if (data.getFuel() < fuel) {
            throw new LuaException("Not enough fuel for journey");
        }

        // Find suitable landing location
        destinationLevel.getChunk(loc.getPos());
        BlockPos landingPos = CommandTravel.getLandingPosition(destinationLevel, loc.getPos());
        @SuppressWarnings("unused")
        boolean landingRecalculated = findSuitableLandingSpot(destinationLevel, landingPos);
        
        // Check if we found a valid landing position
        if (!Block.canSupportRigidBlock(destinationLevel, landingPos.below())) {
            throw new LuaException("Could not find suitable landing location");
        }
        
        loc.setPosition(landingPos.getX(), landingPos.getY(), landingPos.getZ());
        
        // Check if landing spot is clear - using public version's method
        if (!Tardim.isPosValid(destinationLevel, loc.getPos())) {
            throw new LuaException("TARDIM landing obstructed");
        }
        
        // Land the TARDIS
        TardimRegistry.TardimBuilder builder = TardimRegistry.getTardimBuilder(data.getTardimID());
        builder.buildTardim(destinationLevel, loc.getPos(), loc.getFacing(), data.getId());
        data.setCurrentLocation(data.getTravelLocation());
        data.setTravelLocation(null);
        data.setInFlight(false);
        data.addFuel(-fuel);
        
        // Play landing sound - updated for public version
        level.playSound(null, blockEntity.getBlockPos(), TRDSounds.TARDIM_LANDING.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        data.save();
    }

    /**
     * Set destination to the TARDIM's owner's spawn point (must be online)
     */
    @LuaFunction(mainThread = true)
    public final void home() throws LuaException {
        ServerLevel level = getServerLevel();
        TardimData data = getTardimData();

        UUID uuid = data.getOwner();
        String username = data.getOwnerName();
        if (uuid == null || username == null) {
            throw new LuaException("TARDIM has no owner");
        }

        PlayerList playerList = level.getServer().getPlayerList();
        ServerPlayer player = playerList.getPlayer(uuid);
        if (player == null) {
            throw new LuaException("TARDIM owner is not online");
        }

        ResourceKey<Level> dim = player.getRespawnDimension();
        BlockPos pos = player.getRespawnPosition();
        if (pos == null) {
            throw new LuaException("TARDIM owner has no home");
        }

        setDimension(dim.location().toString());
        setTravelLocation(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Set destination to a player's location (player must be online)
     * @param username - String of the username of the player
     */
    @LuaFunction(mainThread = true)
    public final void locatePlayer(String username) throws LuaException {
        ServerLevel level = getServerLevel();
        
        PlayerList playerList = level.getServer().getPlayerList();
        ServerPlayer player = playerList.getPlayerByName(username);
        if (player == null) {
            throw new LuaException("Player not found");
        }

        ResourceKey<Level> dim = player.getCommandSenderWorld().dimension();
        BlockPos pos = player.blockPosition();

        setDimension(dim.location().toString());
        setTravelLocation(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Locate a biome and set destination
     * @param biome_str String of the biome e.g. "minecraft:plains"
     */
    @LuaFunction(mainThread = true)
    public final void locateBiome(String biome_str) throws LuaException {
        ServerLevel level = getServerLevel();
        TardimData data = getTardimData();
        
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }

        @SuppressWarnings("removal")
        Optional<Biome> biome = level.getServer()
                .registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getOptional(new ResourceLocation(biome_str));
        
        if (biome.isEmpty()) {
            throw new LuaException("Unknown biome: " + biome_str);
        }

        @SuppressWarnings("unchecked")
        ServerLevel targetLevel = level.getServer().getLevel(data.getTravelLocation().getLevel());
        if (targetLevel == null) {
            throw new LuaException("Invalid dimension for biome search");
        }
        
        BlockPos currentPos = new BlockPos(
                data.getTravelLocation().getPos().getX(),
                targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, data.getTravelLocation().getPos()).getY(),
                data.getTravelLocation().getPos().getZ()
        );
        
        BlockPos biomePos = findNearestBiome(targetLevel, biome.get(), currentPos, 6400, 8);
        if (biomePos == null) {
            throw new LuaException("Biome not found within search radius");
        }
        
        data.getTravelLocation().setPosition(biomePos.getX(), biomePos.getY(), biomePos.getZ());
        data.save();
    }

    /**
     * Add a number to the destination's coordinates
     * @param axis  String of the axis ("x", "y", "z")
     * @param amount    Number to add to the axis
     */
    @LuaFunction(mainThread = true)
    public final void coordAdd(String axis, int amount) throws LuaException {
        TardimData data = getTardimData();
        if (data.getTravelLocation() == null) {
            data.setTravelLocation(new Location(data.getCurrentLocation()));
        }

        switch (axis.toLowerCase()) {
            case "x" -> data.getTravelLocation().addPosition(amount, 0, 0);
            case "y" -> data.getTravelLocation().addPosition(0, amount, 0);
            case "z" -> data.getTravelLocation().addPosition(0, 0, amount);
            default -> throw new LuaException("Invalid axis. Must be x, y, or z");
        }
    }

    // === INFORMATION GATHERING METHODS ===

    /**
     * Get online players
     * @return Table of the online players
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getOnlinePlayers() throws LuaException {
        ServerLevel level = getServerLevel();
        
        PlayerList playerList = level.getServer().getPlayerList();
        Map<Integer, String> players = new HashMap<>();
        for (int i = 0; i < playerList.getPlayers().size(); i++) {
            players.put(i + 1, playerList.getPlayers().get(i).getGameProfile().getName());
        }

        return new ObjectLuaTable(players);
    }

    /**
     * Get all available TARDIM skins
     * @return Table of the available skins
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getSkins() throws LuaException {
        Map<Integer, String> skins = new HashMap<>();
        Iterator<ResourceLocation> iterator = TardimRegistry.getRegistry().keySet().iterator();
        
        int i = 0;
        while (iterator.hasNext()) {
            ResourceLocation key = iterator.next();
            TardimRegistry.TardimBuilder builder = TardimRegistry.getTardimBuilder(key);
            skins.put(i + 1, builder.getDisplayName());
            i++;
        }

        return new ObjectLuaTable(skins);
    }

    /**
     * Get all registered biomes
     * @return Table of all biomes' technical names
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getBiomes() throws LuaException {
        ServerLevel level = getServerLevel();
        
        Map<Integer, String> biomes = new HashMap<>();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Iterator<ResourceLocation> biomeIterator = biomeRegistry.keySet().iterator();
        
        int i = 0;
        while (biomeIterator.hasNext()) {
            biomes.put(i + 1, biomeIterator.next().toString());
            i++;
        }

        return new ObjectLuaTable(biomes);
    }

    /**
     * Get all registered dimensions
     * @return Table of all dimensions' technical names
     */
    @LuaFunction(mainThread = true)
    public final ObjectLuaTable getDimensions() throws LuaException {
        ServerLevel level = getServerLevel();
        
        Map<Integer, String> dimensions = new HashMap<>();
        Iterator<ServerLevel> dimIterator = level.getServer().getAllLevels().iterator();
        
        int i = 0;
        while (dimIterator.hasNext()) {
            dimensions.put(i + 1, dimIterator.next().dimension().location().toString());
            i++;
        }
        
        return new ObjectLuaTable(dimensions);
    }

    // === UTILITY METHODS ===

    /**
     * Helper method to find a suitable landing spot
     */
    private boolean findSuitableLandingSpot(ServerLevel level, BlockPos landingPos) {
        // Try three search patterns with increasing radius
        for (int searchRadius : new int[] {10, 30, 50}) {
            for (int attempt = 0; attempt < 32; attempt++) {
                if (!Block.canSupportRigidBlock(level, landingPos.below())) {
                    BlockPos testPos = landingPos.offset(
                            level.random.nextInt(searchRadius) * (level.random.nextBoolean() ? 1 : -1),
                            0,
                            level.random.nextInt(searchRadius) * (level.random.nextBoolean() ? 1 : -1)
                    );
                    BlockPos newPos = CommandTravel.getLandingPosition(level, testPos);
                    if (Block.canSupportRigidBlock(level, newPos.below())) {
                        landingPos = newPos;
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a position is valid for TARDIM placement - using public version's method
     */
    @SuppressWarnings("unused")
    private boolean isPosValid(Level level, BlockPos pos) {
        return Tardim.isPosValid(level, pos);
    }

    /**
     * Helper to find the nearest biome
     */
    private BlockPos findNearestBiome(ServerLevel level, Biome biome, BlockPos pos, int radius, int step) {
        Pair<BlockPos, Holder<Biome>> result = level.getChunkSource()
                .getGenerator()
                .getBiomeSource()
                .findBiomeHorizontal(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        radius,
                        step,
                        b_val -> b_val.value() == biome,
                        level.random,
                        true,
                        level.getChunkSource().randomState().sampler()
                );
        return result != null ? result.getFirst() : null;
    }

    /**
     * Converts a string to title case (for dimension handling)
     */
    private String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;
        
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        
        return titleCase.toString();
    }

    /**
     * Checks if a string is a valid resource path - using CommandTravel's method
     */
    @SuppressWarnings("unused")
    private boolean isValidPath(String path) {
        return CommandTravel.isValidPath(path);
    }
}
