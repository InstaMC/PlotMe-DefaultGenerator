package com.worldcretornica.plotme.defaultgenerator;

import static com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath.X_TRANSLATION;
import static com.worldcretornica.plotme_abstractgenerator.AbstractWorldConfigPath.Z_TRANSLATION;

import com.worldcretornica.plotme_abstractgenerator.bukkit.BukkitBlockRepresentation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultChunkGenerator extends ChunkGenerator {

    private final List<BlockPopulator> blockPopulators = new ArrayList<>(2);
    private final ConfigurationSection wgc;

    private final int plotSize;
    private final int pathSize;
    private final int roadHeight;
    private final short wall;
    private final short floorMain;
    private final short floorAlt;
    private final short plotFloor;
    private final short filling;

    public DefaultChunkGenerator(BukkitDefaultGenerator instance, String worldName) {
        wgc = instance.createConfigSection(worldName.toLowerCase());
        plotSize = wgc.getInt(DefaultWorldConfigPath.PLOT_SIZE.key());
        pathSize = wgc.getInt(DefaultWorldConfigPath.PATH_WIDTH.key());
        roadHeight = wgc.getInt(DefaultWorldConfigPath.GROUND_LEVEL.key());
        blockPopulators.add(new DefaultRoadPopulator(wgc, plotSize, pathSize, roadHeight));
        blockPopulators.add(new DefaultContentPopulator(wgc, plotSize, pathSize, roadHeight));
        wall = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.UNCLAIMED_WALL.key(), "44:7"));
        floorMain = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.ROAD_MAIN_BLOCK.key(), "5"));
        floorAlt = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.ROAD_ALT_BLOCK.key(), "5:2"));
        plotFloor = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.PLOT_FLOOR_BLOCK.key(), "2"));
        filling = BukkitBlockRepresentation.getBlockId(wgc.getString(DefaultWorldConfigPath.FILL_BLOCK.key(), "3"));


    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return blockPopulators;
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes) {
        double size = plotSize + pathSize;

        double n1;
        double n2;
        double n3;
        int mod2 = 0;

        if (pathSize % 2 == 1) {
            n1 = Math.ceil((double) pathSize / 2) - 2;
            n2 = Math.ceil((double) pathSize / 2) - 1;
            n3 = Math.ceil((double) pathSize / 2);
            mod2 = -1;
        } else {
            n1 = Math.floor((double) pathSize / 2) - 2;
            n2 = Math.floor((double) pathSize / 2) - 1;
            n3 = Math.floor((double) pathSize / 2);
        }

        int mod1 = 1;
        short[][] result = new short[16][];
        int height = roadHeight + 2;
        for (int x = 0; x < 16; x++) {
            int valx = (cx << 4) + x;

            for (int z = 0; z < 16; z++) {
                int valz = (cz << 4) + z;

                setBlock(result, x, 0, z, (short) 7);
                biomes.setBiome(x, z, Biome.PLAINS);

                for (int y = 1; y < height; y++) {
                    if (y == roadHeight) {
                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) {//middle+3
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                setBlock(result, x, y, z, floorMain);
                            } else {
                                setBlock(result, x, y, z, filling);
                            }
                        } else if ((valx - n2 + mod1) % size == 0 || (valx + n2 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0
                                    || (valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorMain);
                            } else {
                                setBlock(result, x, y, z, floorAlt);
                            }
                        } else if ((valx - n1 + mod1) % size == 0 || (valx + n1 + mod2) % size == 0) //middle+2
                        {
                            if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0 || (valz - n1 + mod1) % size == 0
                                    || (valz + n1 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                setBlock(result, x, y, z, floorMain);
                            }
                        } else {
                            boolean found = false;
                            for (double i = n1; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                setBlock(result, x, y, z, floorMain);
                            } else if ((valz - n2 + mod1) % size == 0 || (valz + n2 + mod2) % size == 0) {
                                setBlock(result, x, y, z, floorAlt);
                            } else {
                                boolean found2 = false;
                                for (double i = n1; i >= 0; i--) {
                                    if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                        found2 = true;
                                        break;
                                    }
                                }

                                if (found2) {
                                    setBlock(result, x, y, z, floorMain);
                                } else {
                                    boolean found3 = false;
                                    for (double i = n3; i >= 0; i--) {
                                        if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
                                            found3 = true;
                                            break;
                                        }
                                    }

                                    if (found3) {
                                        setBlock(result, x, y, z, floorMain);
                                    } else {
                                        setBlock(result, x, y, z, plotFloor);
                                    }
                                }
                            }
                        }
                    } else if (y == (roadHeight + 1)) {
                        if ((valx - n3 + mod1) % size == 0 || (valx + n3 + mod2) % size == 0) //middle+3
                        {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                setBlock(result, x, y, z, wall);
                            }
                        } else {
                            boolean found = false;
                            for (double i = n2; i >= 0; i--) {
                                if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found && ((valz - n3 + mod1) % size == 0 || (valz + n3 + mod2) % size == 0)) {
                                setBlock(result, x, y, z, wall);
                            }
                        }
                    } else {
                        setBlock(result, x, y, z, filling);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, wgc.getInt(X_TRANSLATION.key()), wgc.getInt(DefaultWorldConfigPath.GROUND_LEVEL.key()) + 2,
                wgc.getInt(Z_TRANSLATION.key()));
    }

    protected void setBlock(short[][] result, int x, int y, int z, short blockId) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][(y & 0xF) << 8 | (z << 4) | x] = blockId;
    }
}
