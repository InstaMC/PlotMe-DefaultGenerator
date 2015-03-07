package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FILL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.FOR_SALE_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.GROUND_LEVEL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PATH_WIDTH;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_FLOOR_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PLOT_SIZE;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.PROTECTED_WALL_BLOCK;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.UNCLAIMED_WALL;
import static com.worldcretornica.plotme.defaultgenerator.DefaultWorldConfigPath.WALL_BLOCK;

import com.worldcretornica.plotme_abstractgenerator.WorldGenConfig;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitAbstractGenManager;
import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import com.worldcretornica.plotme_core.PlotId;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlotManager extends BukkitAbstractGenManager {

    public DefaultPlotManager(DefaultGenerator instance, WorldGenConfig wgc) {
        super(instance, wgc);
    }

    @Override
    public PlotId getPlotId(Location loc) {
        int posx = loc.getBlockX();
        int posz = loc.getBlockZ();
        int pathSize = wgc.getInt(PATH_WIDTH);
        int size = wgc.getInt(PLOT_SIZE) + pathSize;

        return internalgetPlotId(pathSize, size, posx, posz);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillRoad(PlotId id1, PlotId id2, World world) {
        Location bottomPlot1 = getPlotBottomLoc(world, id1);
        Location topPlot1 = getPlotTopLoc(world, id1);
        Location bottomPlot2 = getPlotBottomLoc(world, id2);
        Location topPlot2 = getPlotTopLoc(world, id2);

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        int h = wgc.getInt(GROUND_LEVEL);
        int wallId = wgc.getBlockRepresentation(UNCLAIMED_WALL).getId();
        byte wallValue = wgc.getBlockRepresentation(UNCLAIMED_WALL).getData();
        int fillId = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();
        byte fillValue = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getData();
        int plotSize = wgc.getInt(PLOT_SIZE);

        if (bottomPlot1.getBlockX() == bottomPlot2.getBlockX()) {
            minX = bottomPlot1.getBlockX();
            maxX = topPlot1.getBlockX();

            minZ = Math.min(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ()) + plotSize;
            maxZ = Math.max(topPlot1.getBlockZ(), topPlot2.getBlockZ()) - plotSize;
        } else {
            minZ = bottomPlot1.getBlockZ();
            maxZ = topPlot1.getBlockZ();

            minX = Math.min(bottomPlot1.getBlockX(), bottomPlot2.getBlockX()) + plotSize;
            maxX = Math.max(topPlot1.getBlockX(), topPlot2.getBlockX()) - plotSize;
        }

        boolean isWallX = (maxX - minX) > (maxZ - minZ);

        if (isWallX) {
            minX--;
            maxX++;
        } else {
            minZ--;
            maxZ++;
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = h; y < 255; y++) {
                    if (y >= (h + 2)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    } else if (y == (h + 1)) {
                        if (isWallX && (x == minX || x == maxX) || !isWallX && (z == minZ || z == maxZ)) {
                            world.getBlockAt(x, y, z).setTypeIdAndData(wallId, wallValue, true);
                        } else {
                            world.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    } else {
                        world.getBlockAt(x, y, z).setTypeIdAndData(fillId, fillValue, true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fillMiddleRoad(PlotId id1, PlotId id2, World world) {
        Location bottomPlot1 = getPlotBottomLoc(world, id1);
        Location topPlot1 = getPlotTopLoc(world, id1);
        Location bottomPlot2 = getPlotBottomLoc(world, id2);
        Location topPlot2 = getPlotTopLoc(world, id2);

        int height = wgc.getInt(GROUND_LEVEL);
        int fillId = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK).getId();

        int minX = Math.min(topPlot1.getBlockX(), topPlot2.getBlockX());
        int maxX = Math.max(bottomPlot1.getBlockX(), bottomPlot2.getBlockX());

        int minZ = Math.min(topPlot1.getBlockZ(), topPlot2.getBlockZ());
        int maxZ = Math.max(bottomPlot1.getBlockZ(), bottomPlot2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = height; y < 255; y++) {
                    if (y >= (height + 1)) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    } else {
                        world.getBlockAt(x, y, z).setTypeId(fillId);
                    }
                }
            }
        }
    }

    @Override
    public void setOwnerDisplay(World world, PlotId id, String line1, String line2, String line3, String line4) {
        Location pillar = new Location(world, bottomX(id, world) - 1, wgc.getInt(GROUND_LEVEL) + 1, bottomZ(id, world) - 1);

        Block bsign = pillar.clone().add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR);
        bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setSellerDisplay(World world, PlotId id, String line1, String line2, String line3, String line4) {
        removeSellerDisplay(world, id);

        Location pillar = new Location(world, bottomX(id, world) - 1, wgc.getInt(GROUND_LEVEL) + 1, bottomZ(id, world) - 1);

        Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR);
        bsign.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 4, false);

        Sign sign = (Sign) bsign.getState();

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);

        sign.update(true);
    }

    @Override
    public void removeOwnerDisplay(World world, PlotId id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, wgc.getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        Block bsign = pillar.add(0, 0, -1).getBlock();
        bsign.setType(Material.AIR);
    }

    @Override
    public void removeSellerDisplay(World world, PlotId id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, wgc.getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        Block bsign = pillar.clone().add(-1, 0, 0).getBlock();
        bsign.setType(Material.AIR);

    }

    @Override
    public void removeAuctionDisplay(World world, PlotId id) {
        Location bottom = getPlotBottomLoc(world, id);

        Location pillar = new Location(world, bottom.getX() - 1, wgc.getInt(GROUND_LEVEL) + 1, bottom.getZ() - 1);

        Block bsign = pillar.clone().add(-1, 0, 1).getBlock();
        bsign.setType(Material.AIR);
    }

    @Override
    public Location getPlotBottomLoc(World world, PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int plotSize = wgc.getInt(PLOT_SIZE);
        int pathWidth = wgc.getInt(PATH_WIDTH);

        int x = (px * (plotSize + pathWidth)) - (plotSize) - ((int) Math.floor(pathWidth / 2));
        int z = pz * (plotSize + pathWidth) - (plotSize) - ((int) Math.floor(pathWidth / 2));

        return new Location(world, x, 0, z);
    }

    @Override
    public Location getPlotTopLoc(World world, PlotId id) {
        int px = id.getX();
        int pz = id.getZ();

        int plotSize = wgc.getInt(PLOT_SIZE);
        int pathWidth = wgc.getInt(PATH_WIDTH);

        int x = px * (plotSize + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;
        int z = pz * (plotSize + pathWidth) - ((int) Math.floor(pathWidth / 2)) - 1;

        return new Location(world, x, 256, z);
    }

    @Override
    public void clear(Location bottom, Location top) {
        int roadHeight = wgc.getInt(GROUND_LEVEL);
        BukkitBlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BukkitBlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX = bottom.getBlockX();
        int topX = top.getBlockX();
        int bottomZ = bottom.getBlockZ();
        int topZ = top.getBlockZ();

        World world = bottom.getWorld();

        clearEntities(bottom, top);

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 1; y < 255; y++) {
                    block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.BEACON || block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST
                            || block.getType() == Material.BREWING_STAND || block.getType() == Material.DISPENSER
                            || block.getType() == Material.FURNACE || block.getType() == Material.DROPPER || block.getType() == Material.HOPPER) {
                        InventoryHolder holder = (InventoryHolder) block.getState();
                        holder.getInventory().clear();
                    }

                    if (y < roadHeight) {
                        if (block.getTypeId() != (int) fillBlock.getId()) {
                            block.setTypeIdAndData(fillBlock.getId(), fillBlock.getData(), true);
                        }
                    } else if (y == roadHeight) {
                        if (block.getTypeId() != (int) floorBlock.getId()) {
                            block.setTypeIdAndData(floorBlock.getId(), floorBlock.getData(), true);
                        }
                    } else if ((y != (roadHeight + 1) || (x != bottomX - 1 && x != topX + 1 && z != bottomZ - 1 && z != topZ + 1))
                            && block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        refreshPlotChunks(world, getPlotId(bottom));
    }

    @Override
    public Long[] clear(Location bottom, Location top, long maxBlocks, Long[] start) {
        clearEntities(bottom, top);

        int roadHeight = wgc.getInt(GROUND_LEVEL);
        BukkitBlockRepresentation fillBlock = wgc.getBlockRepresentation(FILL_BLOCK);
        BukkitBlockRepresentation floorBlock = wgc.getBlockRepresentation(PLOT_FLOOR_BLOCK);

        int bottomX;
        int topX = top.getBlockX();
        int bottomZ;
        int topZ = top.getBlockZ();

        long nbBlockClearedBefore = 0;

        World world = bottom.getWorld();

        if (start == null) {
            bottomX = bottom.getBlockX();
            bottomZ = bottom.getBlockZ();
        } else {
            bottomX = start[0].intValue();
            bottomZ = start[2].intValue();
            nbBlockClearedBefore = start[3];
        }

        long nbBlockCleared = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                Block block = world.getBlockAt(x, 0, z);
                if (!block.getType().equals(Material.BEDROCK)) {
                    block.setType(Material.BEDROCK);
                }
                block.setBiome(Biome.PLAINS);

                for (int y = 1; y < 255; y++) {
                    block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.BEACON
                            || block.getType() == Material.CHEST
                            || block.getType() == Material.BREWING_STAND
                            || block.getType() == Material.DISPENSER
                            || block.getType() == Material.FURNACE
                            || block.getType() == Material.DROPPER
                            || block.getType() == Material.TRAPPED_CHEST
                            || block.getType() == Material.HOPPER) {
                        InventoryHolder holder = (InventoryHolder) block.getState();
                        holder.getInventory().clear();
                    }

                    if (y < roadHeight) {
                        if (block.getTypeId() != (int) fillBlock.getId()) {
                            block.setTypeIdAndData(fillBlock.getId(), fillBlock.getData(), true);
                        }
                    } else if (y == roadHeight) {
                        if (block.getTypeId() != (int) floorBlock.getId()) {
                            block.setTypeIdAndData(floorBlock.getId(), floorBlock.getData(), true);
                        }
                    } else if ((y != roadHeight + 1 || x != bottomX - 1 && x != topX + 1 && z != bottomZ - 1 && z != topZ + 1)
                            && block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }

                    nbBlockCleared++;

                    if (nbBlockCleared >= maxBlocks) {
                        return new Long[]{(long) x, (long) y, (long) z, nbBlockClearedBefore + nbBlockCleared};
                    }
                }
            }
            bottomZ = bottom.getBlockZ();
        }

        refreshPlotChunks(world, getPlotId(bottom));
        return null;
    }

    @Override
    public void adjustPlotFor(World world, PlotId id, boolean claimed, boolean protect, boolean forSale) {
        List<String> wallIds = new ArrayList<>();

        int roadHeight = wgc.getInt(GROUND_LEVEL);

        String claimedId = wgc.getString(WALL_BLOCK);
        String wallId = wgc.getString(UNCLAIMED_WALL);
        String protectedWallId = wgc.getString(PROTECTED_WALL_BLOCK);
        String forsaleWallId = wgc.getString(FOR_SALE_WALL_BLOCK);

        if (protect) {
            wallIds.add(protectedWallId);
        }
        if (forSale && !wallIds.contains(forsaleWallId)) {
            wallIds.add(forsaleWallId);
        }
        if (claimed && !wallIds.contains(claimedId)) {
            wallIds.add(claimedId);
        }
        if (wallIds.isEmpty()) {
            wallIds.add(wallId);
        }

        int ctr = 0;

        Location bottom = getPlotBottomLoc(world, id);
        Location top = getPlotTopLoc(world, id);

        int x;
        int z;

        String currentBlockId;
        Block block;

        for (x = bottom.getBlockX() - 1; x < top.getBlockX() + 1; x++) {
            z = bottom.getBlockZ() - 1;
            currentBlockId = wallIds.get(ctr);
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentBlockId);
        }

        for (z = bottom.getBlockZ() - 1; z < top.getBlockZ() + 1; z++) {
            x = top.getBlockX() + 1;
            currentBlockId = wallIds.get(ctr);
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentBlockId);
        }

        for (x = top.getBlockX() + 1; x > bottom.getBlockX() - 1; x--) {
            z = top.getBlockZ() + 1;
            currentBlockId = wallIds.get(ctr);
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentBlockId);
        }

        for (z = top.getBlockZ() + 1; z > bottom.getBlockZ() - 1; z--) {
            x = bottom.getBlockX() - 1;
            currentBlockId = wallIds.get(ctr);
            if (ctr == wallIds.size() - 1) {
                ctr = 0;
            } else {
                ctr += 1;
            }
            block = world.getBlockAt(x, roadHeight + 1, z);
            setWall(block, currentBlockId);
        }
    }

    @SuppressWarnings("deprecation")
    private void setWall(Block block, String currentBlockId) {

        int blockId;
        byte blockData = 0;

        if (currentBlockId.contains(":")) {
            try {
                blockId = Integer.parseInt(currentBlockId.substring(0, currentBlockId.indexOf(":")));
                blockData = Byte.parseByte(currentBlockId.substring(currentBlockId.indexOf(":") + 1));
            } catch (NumberFormatException e) {
                blockId = wgc.getBlockRepresentation(UNCLAIMED_WALL).getId();
                blockData = wgc.getBlockRepresentation(UNCLAIMED_WALL).getData();
            }
        } else {
            try {
                blockId = Integer.parseInt(currentBlockId);
            } catch (NumberFormatException e) {
                blockId = wgc.getBlockRepresentation(UNCLAIMED_WALL).getId();
            }
        }

        block.setTypeIdAndData(blockId, blockData, true);
    }

    @Override
    public Location getPlotHome(World world, PlotId id) {
        Location bottom = getPlotBottomLoc(world, id);
        Location top = getPlotTopLoc(world, id);
        return new Location(world, (top.getX() + bottom.getX() + 1) / 2, wgc.getInt(GROUND_LEVEL) + 2, (top.getZ() + bottom.getZ() + 1) / 2);
    }

    @Override
    public Location getPlotMiddle(World world, PlotId id) {
        Location bottom = getPlotBottomLoc(world, id);
        Location top = getPlotTopLoc(world, id);

        double x = (top.getX() + bottom.getX() + 1) / 2;
        double y = getRoadHeight() + 1;
        double z = (top.getZ() + bottom.getZ() + 1) / 2;

        return new Location(world, x, y, z);
    }
}
