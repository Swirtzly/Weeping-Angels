package me.suff.angels.proxy;

import me.suff.angels.common.world.generation.WorldGenCatacombs;
import me.suff.angels.common.world.generation.generators.WorldGenOres;
import me.suff.angels.utils.AngelUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
	
	
	public void preInit() {
		GameRegistry.registerWorldGenerator(new WorldGenCatacombs(), 0);
		GameRegistry.registerWorldGenerator(new WorldGenOres(), 0);
	}
	
	public void init() {
	
	}
	
	public void postInit() {
		AngelUtils.setupLightItems();
	}
	
}