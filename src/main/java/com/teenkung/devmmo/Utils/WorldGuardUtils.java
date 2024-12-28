package com.teenkung.devmmo.Utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldGuardUtils {


    public static boolean isInRegion(Location location, String regionName) {

        // Get the world object
        World world =  location.getWorld();
        if (world == null) {
            return false;
        }

        // Get the RegionManager for the world
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            return false;
        }

        // Get the set of regions at the player's location
        ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));

        // Check if the specified region is among the applicable regions
        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }

        return false;
    }

}
