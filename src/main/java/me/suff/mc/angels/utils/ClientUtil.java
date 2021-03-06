package me.suff.mc.angels.utils;

import me.suff.mc.angels.WeepingAngels;
import me.suff.mc.angels.client.models.entity.*;
import me.suff.mc.angels.client.renders.entities.AngelRender;
import me.suff.mc.angels.client.renders.entities.AnomalyRender;
import me.suff.mc.angels.client.renders.entities.CGRender;
import me.suff.mc.angels.client.renders.tileentities.CoffinRenderer;
import me.suff.mc.angels.client.renders.tileentities.PlinthTileRender;
import me.suff.mc.angels.client.renders.tileentities.SnowArmTileRender;
import me.suff.mc.angels.client.renders.tileentities.StatueRender;
import me.suff.mc.angels.common.WAObjects;
import me.suff.mc.angels.common.entities.AngelEnums;
import me.suff.mc.angels.common.entities.WeepingAngelEntity;
import me.suff.mc.angels.common.items.AngelSpawnerItem;
import me.suff.mc.angels.common.items.DetectorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.HashMap;
import java.util.Map;

public class ClientUtil {

    private static final EntityModel< WeepingAngelEntity > VIO_1 = new ModelAngel();
    private static final EntityModel< WeepingAngelEntity > ED = new ModelAngelEd();
    private static final EntityModel< WeepingAngelEntity > CHERUB = new ModelAngelChild();
    private static final EntityModel< WeepingAngelEntity > A_DIZZLE = new ModelClassicAngel();
    private static final EntityModel< WeepingAngelEntity > VIO_2 = new ModelAngelMel();
    private static final EntityModel< WeepingAngelEntity > VILLAGER = new ModelWeepingVillager();
    private static final EntityModel< WeepingAngelEntity > ANGELA_MC = new ModelAngelaAngel();

    private static final Map< AngelEnums.AngelType, EntityModel< WeepingAngelEntity > > MODEL_MAP = new HashMap<>();

    static {
        MODEL_MAP.put(AngelEnums.AngelType.CHERUB, CHERUB); // ED
        MODEL_MAP.put(AngelEnums.AngelType.ED, ED);// ED
        MODEL_MAP.put(AngelEnums.AngelType.ANGELA_MC, ANGELA_MC); //ANGELA
        MODEL_MAP.put(AngelEnums.AngelType.A_DIZZLE, A_DIZZLE); //A_DIZZLE
        MODEL_MAP.put(AngelEnums.AngelType.VILLAGER, VILLAGER); //DOC
        MODEL_MAP.put(AngelEnums.AngelType.VIO_1, VIO_1); //VIOLET
        MODEL_MAP.put(AngelEnums.AngelType.VIO_2, VIO_2);//VIOLET
    }


    public static EntityModel< WeepingAngelEntity > getModelForAngel(AngelEnums.AngelType angelType) {
        return MODEL_MAP.get(angelType);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent soundIn, float volumeSfx) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(soundIn, volumeSfx));
    }

    public static void doClientStuff() {
        ClientRegistry.bindTileEntityRenderer(WAObjects.Tiles.SNOW_ANGEL.get(), SnowArmTileRender::new);
        ClientRegistry.bindTileEntityRenderer(WAObjects.Tiles.PLINTH.get(), PlinthTileRender::new);
        ClientRegistry.bindTileEntityRenderer(WAObjects.Tiles.STATUE.get(), StatueRender::new);
        ClientRegistry.bindTileEntityRenderer(WAObjects.Tiles.COFFIN.get(), CoffinRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(WAObjects.EntityEntries.WEEPING_ANGEL.get(), AngelRender::new);
        RenderingRegistry.registerEntityRenderingHandler(WAObjects.EntityEntries.ANOMALY.get(), AnomalyRender::new);
        RenderingRegistry.registerEntityRenderingHandler(WAObjects.EntityEntries.CHRONODYNE_GENERATOR.get(), (EntityRendererManager entityRendererManager) -> new CGRender(entityRendererManager, Minecraft.getInstance().getItemRenderer()));

        RenderTypeLookup.setRenderLayer(WAObjects.Blocks.SNOW_ANGEL.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(WAObjects.Blocks.PLINTH.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(WAObjects.Blocks.STATUE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(WAObjects.Blocks.KONTRON_ORE.get(), RenderType.getCutout());

        ItemModelsProperties.registerProperty(WAObjects.Items.TIMEY_WIMEY_DETECTOR.get(), new ResourceLocation("angle"), (itemStack, clientWorld, livingEntity) -> DetectorItem.getTime(itemStack));

        ItemModelsProperties.registerProperty(WAObjects.Items.ANGEL_SPAWNER.get(), new ResourceLocation(WeepingAngels.MODID, "angel_type"), (itemStack, clientWorld, livingEntity) -> {
            if (itemStack == null || itemStack.isEmpty()) {
                return 0;
            }
            AngelEnums.AngelType type = AngelSpawnerItem.getType(itemStack);
            return type.ordinal();
        });
    }

}
