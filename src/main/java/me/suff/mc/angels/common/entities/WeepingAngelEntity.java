package me.suff.mc.angels.common.entities;

import me.suff.mc.angels.api.EventAngelBreakEvent;
import me.suff.mc.angels.client.poses.WeepingAngelPose;
import me.suff.mc.angels.common.WAObjects;
import me.suff.mc.angels.common.entities.attributes.WAAttributes;
import me.suff.mc.angels.common.misc.WAConstants;
import me.suff.mc.angels.config.WAConfig;
import me.suff.mc.angels.utils.AngelUtils;
import me.suff.mc.angels.utils.NBTPatcher;
import me.suff.mc.angels.utils.WATeleporter;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class WeepingAngelEntity extends QuantumLockBaseEntity {

    private static final DataParameter< String > TYPE = EntityDataManager.createKey(WeepingAngelEntity.class, DataSerializers.STRING);
    private static final DataParameter< String > CURRENT_POSE = EntityDataManager.createKey(WeepingAngelEntity.class, DataSerializers.STRING);
    private static final DataParameter< String > VARIENT = EntityDataManager.createKey(WeepingAngelEntity.class, DataSerializers.STRING);
    private static final DataParameter< Float > LAUGH = EntityDataManager.createKey(WeepingAngelEntity.class, DataSerializers.FLOAT);
    private static final Predicate< Difficulty > DIFFICULTY = (difficulty) -> difficulty == Difficulty.EASY;
    private final SoundEvent[] CHILD_SOUNDS = new SoundEvent[]{SoundEvents.ENTITY_VEX_AMBIENT, WAObjects.Sounds.LAUGHING_CHILD.get()};
    public long timeSincePlayedSound = 0;

    public WeepingAngelEntity(EntityType< ? extends QuantumLockBaseEntity > type, World world) {
        this(world);
    }

    public WeepingAngelEntity(World world) {
        super(world, WAObjects.EntityEntries.WEEPING_ANGEL.get());
        goalSelector.addGoal(0, new BreakDoorGoal(this, DIFFICULTY));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 50.0F));
        experienceValue = WAConfig.CONFIG.xpGained.get();
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return func_234295_eP_().
                createMutableAttribute(Attributes.ATTACK_DAMAGE, WAConfig.CONFIG.damage.get()).
                createMutableAttribute(Attributes.MAX_HEALTH, 50D).
                createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 9999999.0D).
                createMutableAttribute(Attributes.MOVEMENT_SPEED, WAConfig.CONFIG.moveSpeed.get()).
                createMutableAttribute(WAAttributes.BLOCK_BREAK_RANGE.get(), WAConfig.CONFIG.blockBreakRange.get()).
                createMutableAttribute(Attributes.ARMOR, 2.0D);
    }

    public void dropAngelStuff() {
        if (world.isRemote()) return;
        AngelUtils.dropEntityLoot(this, this.attackingPlayer);
        entityDropItem(getHeldItemMainhand());
        entityDropItem(getHeldItemOffhand());
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    protected void registerData() {
        super.registerData();
        getDataManager().register(TYPE, AngelUtils.randomType().name());
        getDataManager().register(CURRENT_POSE, WeepingAngelPose.getRandomPose(AngelUtils.RAND).name());
        getDataManager().register(VARIENT, AngelUtils.randomVarient().name());
        getDataManager().register(LAUGH, rand.nextFloat());

    }

    public String getVarient() {
        return getDataManager().get(VARIENT);
    }

    public void setVarient(AngelVariants varient) {
        getDataManager().set(VARIENT, varient.name());
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld serverWorld, DifficultyInstance difficultyInstance, SpawnReason spawnReason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT compoundNBT) {
        playSound(WAObjects.Sounds.ANGEL_AMBIENT.get(), 0.5F, 1.0F);
        return super.onInitialSpawn(serverWorld, difficultyInstance, spawnReason, livingEntityData, compoundNBT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BLOCK_STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WAObjects.Sounds.ANGEL_DEATH.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isCherub() && ticksExisted % AngelUtils.secondsToTicks(2) == 0) {
            return CHILD_SOUNDS[rand.nextInt(CHILD_SOUNDS.length)];
        }
        return null;
    }

    @Override
    public float getEyeHeight(Pose p_213307_1_) {
        return isCherub() ? 0.5F : 1.3F;
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return isCherub() ? new EntitySize(0.8F, 0.8F, true) : super.getSize(poseIn);
    }

    @Override
    public boolean isChild() {
        return isCherub();
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) entity;
            if (!isCherub()) {
                // Teleport Only
                if (WAConfig.CONFIG.justTeleport.get()) {
                    teleportInteraction(serverPlayerEntity);
                    return true;
                }

                // Chance to teleport/deal damage
                boolean shouldTeleport = rand.nextInt(10) < 5 && getHealth() > 5 && !isInCatacomb();
                if (shouldTeleport) {
                    teleportInteraction(serverPlayerEntity);
                } else {
                    dealDamage(serverPlayerEntity);
                }
                return true;
            } else {
                //Child Behaviour
                if (WAConfig.CONFIG.torchBlowOut.get()) {
                    AngelUtils.removeLightFromHand(serverPlayerEntity, this);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCatacomb() {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos catacomb = serverWorld.getWorld().func_241117_a_(WAObjects.Structures.CATACOMBS.get(), getPosition(), 100, false);

            if (catacomb == null) {
                return false;
            }

            return getDistanceSq(catacomb.getX(), catacomb.getY(), catacomb.getZ()) < 50;
        }

        return false;
    }

    public void dealDamage(PlayerEntity playerMP) {
        if (getHealth() > 5) {
            playerMP.attackEntityFrom(WAObjects.ANGEL, 4.0F);
            heal(4.0F);
        } else {
            playerMP.attackEntityFrom(WAObjects.ANGEL_NECK_SNAP, 4.0F);
            heal(2.0F);
        }


        // Steals keys from the player
        if (getHeldItemMainhand().isEmpty() && rand.nextBoolean()) {
            for (int i = 0; i < playerMP.inventory.getSizeInventory(); i++) {
                ItemStack stack = playerMP.inventory.getStackInSlot(i);
                if (stack.getItem().isIn(AngelUtils.THEFT)) {
                    setHeldItem(Hand.MAIN_HAND, playerMP.inventory.getStackInSlot(i).copy());
                    playerMP.inventory.getStackInSlot(i).setCount(0);
                    playerMP.container.detectAndSendChanges();
                    return;
                }
            }
        }
    }

    public long getTimeSincePlayedSound() {
        return timeSincePlayedSound;
    }

    public void setTimeSincePlayedSound(long timeSincePlayedSound) {
        this.timeSincePlayedSound = timeSincePlayedSound;
    }

    @Override
    protected boolean canDropLoot() {
        return true;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        entityDropItem(getHeldItemMainhand());
        entityDropItem(getHeldItemOffhand());

        if (getAngelType() == AngelEnums.AngelType.ANGELA_MC) {
            AngelVariants angelVarient = AngelVariants.valueOf(getVarient());
            entityDropItem(angelVarient.getDropStack());
        }

    }

    @Override
    public void setMotionMultiplier(BlockState state, Vector3d motionMultiplierIn) {
        if (!state.isIn(Blocks.COBWEB)) {
            super.setMotionMultiplier(state, motionMultiplierIn);
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putString(WAConstants.POSE, getAngelPose());
        compound.putString(WAConstants.TYPE, getAngelType().name());
        compound.putString(WAConstants.VARIENT, getVarient());
        compound.putFloat(WAConstants.LAUGH, getLaugh());
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        NBTPatcher.angelaToVillager(compound, WAConstants.TYPE);

        if (compound.contains(WAConstants.POSE))
            setPose(WeepingAngelPose.getPose(compound.getString(WAConstants.POSE)));

        if (compound.contains(WAConstants.LAUGH))
            setLaugh(serializeNBT().getFloat(WAConstants.LAUGH));

        if (compound.contains(WAConstants.TYPE)) setType(compound.getString(WAConstants.TYPE));

        if (compound.contains(WAConstants.VARIENT))
            setVarient(AngelVariants.valueOf(compound.getString(WAConstants.VARIENT)));
    }

    @Override
    public void notifyDataManagerChange(DataParameter< ? > key) {
        super.notifyDataManagerChange(key);
        if (TYPE.equals(key)) {
            recalculateSize();
        }
    }

    @Override
    public void invokeSeen(PlayerEntity player) {
        super.invokeSeen(player);
        if (player instanceof ServerPlayerEntity && getSeenTime() == 1 && getPrevPos().toLong() != getPosition().toLong()) {
            setPrevPos(getPosition());
            boolean canPlaySound = !player.isCreative() && getTimeSincePlayedSound() == 0 || System.currentTimeMillis() - getTimeSincePlayedSound() >= 20000;
            // Play Sound
            if (canPlaySound) {
                if (WAConfig.CONFIG.playSeenSounds.get() && player.getDistance(this) < 15) {
                    setTimeSincePlayedSound(System.currentTimeMillis());
                    ((ServerPlayerEntity) player).connection.sendPacket(new SPlaySoundEffectPacket(WAObjects.Sounds.ANGEL_SEEN.get(), SoundCategory.HOSTILE, player.getPosX(), player.getPosY(), player.getPosZ(), 0.1F, 1.0F));
                }
            }

            if (getAngelType() != AngelEnums.AngelType.VIO_1) {
                setPose(WeepingAngelPose.getRandomPose(AngelUtils.RAND));
            } else {
                setPose(Objects.requireNonNull(rand.nextBoolean() ? WeepingAngelPose.ANGRY : WeepingAngelPose.HIDING));
            }
        }
    }


    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (!blockIn.getMaterial().isLiquid()) {
            BlockState blockstate = this.world.getBlockState(pos.up());
            SoundType soundtype = blockstate.getBlock() == Blocks.SNOW ? blockstate.getSoundType(world, pos, this) : blockIn.getSoundType(world, pos, this);

            if (isCherub()) {
                if (world.rand.nextInt(5) == 4) {
                    playSound(WAObjects.Sounds.CHILD_RUN.get(), 0.1F, soundtype.getPitch());
                }
            } else if (WAConfig.CONFIG.playScrapeSounds.get() && world.rand.nextInt(5) == 4) {
                playSound(WAObjects.Sounds.STONE_SCRAP.get(), 0.1F, soundtype.getPitch());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(!world.isRemote){
            System.out.println(isMovementBlocked());
        }

        if (getSeenTime() == 0 || world.isAirBlock(getPosition().down())) {
            setNoAI(false);
        }

        if (ticksExisted % 500 == 0 && getAttackTarget() == null && getSeenTime() == 0) {
            setPose(Objects.requireNonNull(WeepingAngelPose.HIDING));
        }

        if (WAConfig.CONFIG.blockBreaking.get() && isSeen() && world.getGameRules().get(GameRules.MOB_GRIEFING).get()) {
            double range = getAttributeValue(WAAttributes.BLOCK_BREAK_RANGE.get());
            replaceBlocks(getBoundingBox().grow(range, 3, range));
        }
    }

    @Override
    protected PathNavigator createNavigator(World worldIn) {
        GroundPathNavigator navigator = new GroundPathNavigator(this, worldIn);
        navigator.setCanSwim(false);
        navigator.setBreakDoors(true);
        navigator.setAvoidSun(false);
        navigator.setSpeed(1.0D);
        return navigator;
    }

    private void replaceBlocks(AxisAlignedBB box) {
        if (world.isRemote || ticksExisted % 100 != 0) return;

        if (world.getLight(getPosition()) == 0) {
            return;
        }

        for (Iterator< BlockPos > iterator = BlockPos.getAllInBox(new BlockPos(box.maxX, box.maxY, box.maxZ), new BlockPos(box.minX, box.minY, box.minZ)).iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            ServerWorld serverWorld = (ServerWorld) world;
            BlockState blockState = serverWorld.getBlockState(pos);
            if (isAllowed(blockState, pos)) {

                if (blockState.getBlock().isIn(AngelUtils.BANNED_BLOCKS) || blockState.getBlock() == Blocks.LAVA) {
                    continue;
                }

                if (blockState.getBlock() == Blocks.TORCH || blockState.getBlock() == Blocks.REDSTONE_TORCH || blockState.getBlock() == Blocks.GLOWSTONE) {
                    AngelUtils.playBreakEvent(this, pos, Blocks.AIR.getDefaultState());
                    return;
                }

                if (blockState.getBlock() == Blocks.REDSTONE_LAMP) {
                    if (blockState.get(RedstoneLampBlock.LIT)) {
                        AngelUtils.playBreakEvent(this, pos, blockState.with(RedstoneLampBlock.LIT, false));
                        return;
                    }
                }


                if (blockState.getBlock() instanceof NetherPortalBlock || blockState.getBlock() instanceof EndPortalBlock) {
                    if (getHealth() < getMaxHealth()) {
                        heal(0.5F);
                        Vector3d start = getPositionVec();
                        Vector3d end = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                        Vector3d path = start.subtract(end);
                        for (int i = 0; i < 10; ++i) {
                            double percent = i / 10.0;
                            ((ServerWorld) world).spawnParticle(ParticleTypes.PORTAL, pos.getX() + 0.5 + path.getX() * percent, pos.getY() + 1.3 + path.getY() * percent, pos.getZ() + 0.5 + path.z * percent, 20, 0, 0, 0, 0);
                        }
                        return;
                    }
                }

                if (blockState.getLightValue() > 0 && !(blockState.getBlock() instanceof NetherPortalBlock) && !(blockState.getBlock() instanceof EndPortalBlock)) {
                    AngelUtils.playBreakEvent(this, pos, Blocks.AIR.getDefaultState());
                    return;
                }
            }
        }
    }

    private void teleportInteraction(ServerPlayerEntity player) {
        if (world.isRemote) return;
        AngelUtils.EnumTeleportType type = WAConfig.CONFIG.teleportType.get();
        switch (type) {

            case DONT:
                attackEntityAsMob(player);
                break;
            case STRUCTURES:
                Objects.requireNonNull(world.getServer()).enqueue(new TickDelayedTask(0, () -> {
                    if (!WATeleporter.handleStructures(player)) {
                        dealDamage(player);
                    }
                }));
                break;
            case RANDOM_PLACE:
                double x = player.getPosX() + rand.nextInt(WAConfig.CONFIG.teleportRange.get());
                double z = player.getPosZ() + rand.nextInt(WAConfig.CONFIG.teleportRange.get());

                ServerWorld teleportWorld = WAConfig.CONFIG.angelDimTeleport.get() ? WATeleporter.getRandomDimension(rand) : (ServerWorld) player.world;
                ChunkPos chunkPos = new ChunkPos(new BlockPos(x, 0, z));
                teleportWorld.forceChunk(chunkPos.x, chunkPos.z, true);

                teleportWorld.getServer().enqueue(new TickDelayedTask(0, () -> {
                    BlockPos blockPos = WATeleporter.findSafePlace(player, teleportWorld, new BlockPos(x, player.getPosY(), z));

                    if (AngelUtils.isOutsideOfBorder(teleportWorld, blockPos)) {
                        dealDamage(player);
                        return;
                    }

                    if (teleportWorld != null) {
                        WATeleporter.teleportPlayerTo(player, blockPos, teleportWorld);
                        teleportWorld.forceChunk(chunkPos.x, chunkPos.z, false);
                        heal(10);
                    }
                }));
                break;
        }
    }

    @Override
    public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
        return worldIn.getDifficulty() != Difficulty.PEACEFUL && this.isValidLightLevel() && super.canSpawn(worldIn, spawnReasonIn);
    }


    protected boolean isValidLightLevel() {
        BlockPos blockpos = new BlockPos(this.getPosX(), this.getBoundingBox().minY, this.getPosZ());
        if (this.world.getLightFor(LightType.SKY, blockpos) > this.rand.nextInt(32)) {
            return false;
        } else {
            int i = this.world.isThundering() ? this.world.getNeighborAwareLightSubtracted(blockpos, 10) : this.world.getLight(blockpos);
            return i <= this.rand.nextInt(8);
        }
    }

    public String getAngelPose() {
        return getDataManager().get(CURRENT_POSE);
    }

    public void setPose(WeepingAngelPose weepingAngelPose) {
        getDataManager().set(CURRENT_POSE, weepingAngelPose.name());
    }

    public boolean isCherub() {
        return getAngelType() == AngelEnums.AngelType.CHERUB;
    }

    public AngelEnums.AngelType getAngelType() {
        String type = getDataManager().get(TYPE);
        return type.isEmpty() ? AngelEnums.AngelType.ANGELA_MC : AngelEnums.AngelType.valueOf(type);
    }

    public void setType(String angelType) {
        getDataManager().set(TYPE, angelType);
    }

    public void setType(AngelEnums.AngelType angelType) {
        setType(angelType.name());
    }


    @Override
    protected void onDeathUpdate() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            hurtTime = 0;
            this.setDead();
            playSound(getDeathSound(), 1, 1);
        }
        for (int i = 0; i < 20; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState()), this.getPosXRandom(1.0D), this.getPosYRandom(), this.getPosZRandom(1.0D), d0, d1, d2);
        }
    }

    public boolean isAllowed(BlockState blockState, BlockPos blockPos) {
        EventAngelBreakEvent eventAngelBreakEvent = new EventAngelBreakEvent(this, blockState, blockPos);
        MinecraftForge.EVENT_BUS.post(eventAngelBreakEvent);
        return !eventAngelBreakEvent.isCanceled();
    }

    public float getLaugh() {
        return getDataManager().get(LAUGH);
    }

    public void setLaugh(float laugh) {
        getDataManager().set(LAUGH, laugh);
    }

    public enum AngelVariants {
        MOSSY(new ItemStack(Blocks.VINE)), NORMAL(new ItemStack(Blocks.COBBLESTONE)), RUSTED(new ItemStack(Blocks.GRANITE)), RUSTED_NO_ARM(new ItemStack(Blocks.GRANITE)), RUSTED_NO_WING(new ItemStack(Blocks.GRANITE)), RUSTED_HEADLESS(true, new ItemStack(Blocks.GRANITE));

        private final boolean headless;
        private final ItemStack dropStack;

        AngelVariants(ItemStack stack) {
            this(false, stack);
        }

        AngelVariants(boolean b, ItemStack stack) {
            headless = b;
            this.dropStack = stack;
        }

        public ItemStack getDropStack() {
            return dropStack;
        }

        public boolean isHeadless() {
            return headless;
        }
    }

}
