package me.suff.mc.angels.common.entities;

import me.suff.mc.angels.common.WAObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class AnomalyEntity extends MobEntity {

    private static final DataParameter< Integer > TIME_ALIVE = EntityDataManager.createKey(AnomalyEntity.class, DataSerializers.VARINT);

    public AnomalyEntity(World worldIn) {
        super(WAObjects.EntityEntries.ANOMALY.get(), worldIn);
    }

    public AnomalyEntity(EntityType< ? > type, World world) {
        this(world);
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
    }

    @Override
    protected boolean isMovementBlocked() {
        return true;
    }

    public int getTimeAlive() {
        return getDataManager().get(TIME_ALIVE);
    }

    public void setTimeAlive(int timeAlive) {
        getDataManager().set(TIME_ALIVE, timeAlive);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {

            setNoAI(true);
            if (ticksExisted == 1) {
                playSound(WAObjects.Sounds.TELEPORT.get(), 1.0F, 1.0F);
            }

            if (ticksExisted > getTimeAlive()) {
                remove();
            }

            for (WeepingAngelEntity weepingAngelEntity : world.getEntitiesWithinAABB(WeepingAngelEntity.class, getBoundingBox().grow(10))) {
                BlockPos pos = getPosition().subtract(weepingAngelEntity.getPosition());
                Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).normalize();
                weepingAngelEntity.setNoAI(false);
                weepingAngelEntity.setMotion(vec.scale(0.15D));
            }
        }
    }


    @Override
    protected void collideWithEntity(Entity entityIn) {
        super.collideWithEntity(entityIn);

        if (entityIn instanceof WeepingAngelEntity) {
            WeepingAngelEntity weepingAngelEntity = (WeepingAngelEntity) entityIn;
            weepingAngelEntity.setSilent(true);
            weepingAngelEntity.attackEntityFrom(WAObjects.GENERATOR, Integer.MAX_VALUE);
        }
    }

    @Override
    protected void registerData() {
        super.registerData();
        getDataManager().register(TIME_ALIVE, 100);
    }
}
