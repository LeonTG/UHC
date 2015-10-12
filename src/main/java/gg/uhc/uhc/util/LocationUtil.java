package gg.uhc.uhc.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocationUtil {

    protected static boolean damagesPlayer(Material material) {
        switch (material) {
            case LAVA:
            case STATIONARY_LAVA:
            case CACTUS:
            case FIRE:
                return true;
            default:
                return false;
        }
    }

    protected static boolean canStandOn(Material material) {
        switch (material) {
            // all of these are 'solid' according to Material but I'm treating them as 'unsolid'
            // as you can fall through them on teleport
            case TRAP_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case WOODEN_DOOR:
            case IRON_TRAPDOOR:
            // wtf bukkit you can't even stand on these, they have no hitbox
            case WALL_SIGN:
            case SIGN_POST:
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case WALL_BANNER:
            case STANDING_BANNER:
                return false;
            default:
                return material.isSolid();
        }
    }

    /**
     * Checks for the highest safe to stand on block with 2 un-solid blocks above it (excluding above world height)
     *
     * Does not teleport on to non-solid blocks or blocks that can damage the player.
     *
     * Only teleports into water if it is not at head height (feet only)
     *
     * If the world type is NETHER then searching will start at 128 instead of the world max height to avoid the
     * bedrock roof.
     *
     * @return -1 if no valid location found, otherwise coordinate with non-air Y coord with 2 air blocks above it
     */
    public static int findHighestTeleportableY(World world, int x, int z) {
        Location startingLocation = new Location(world, x, world.getEnvironment() == World.Environment.NETHER ? 128 : world.getMaxHeight(), z);

        boolean above2WasSafe = false;
        boolean aboveWasSafe = false;
        boolean above2WasWater = false;
        boolean aboveWasWater = false;

        Block currentBlock = startingLocation.getBlock();

        Material type;
        boolean damagesPlayer, canStandOn;
        while (currentBlock.getY() >= 0) {
            type = currentBlock.getType();

            // get info about the current block
            damagesPlayer = damagesPlayer(type);
            canStandOn = canStandOn(type);

            // valid block if it has 2 safe blocks above it, it doesn't damage the player,
            // is safe to stand on and there isn't any water in the head space
            if (above2WasSafe && aboveWasSafe && !above2WasWater && !damagesPlayer && canStandOn) {
                return currentBlock.getY();
            }

            // move safe blocks
            above2WasSafe = aboveWasSafe;
            aboveWasSafe = !canStandOn && !damagesPlayer;

            // move water blocks
            above2WasWater = aboveWasWater;
            aboveWasWater = type == Material.WATER || type == Material.STATIONARY_WATER;

            // move down a block and run again
            currentBlock = currentBlock.getRelative(BlockFace.DOWN);
        }

        return -1;
    }
}
