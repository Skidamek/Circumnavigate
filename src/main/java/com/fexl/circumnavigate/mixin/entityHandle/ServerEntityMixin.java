/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow @Final private ServerLevel level;
	@Shadow @Final public Entity entity;
	@Shadow @Final private int updateInterval;
	@Shadow @Final private boolean trackDelta;
	@Shadow @Final private Consumer<Packet<?>> broadcast;
	@Shadow @Final private VecDeltaCodec positionCodec;
	@Shadow private int lastSentYRot;
	@Shadow private int lastSentXRot;
	@Shadow private int lastSentYHeadRot;
	@Shadow private Vec3 lastSentMovement;
	@Shadow private int tickCount;
	@Shadow private int teleportDelay;
	@Shadow private List<Entity> lastPassengers;
	@Shadow private boolean wasRiding;
	@Shadow private boolean wasOnGround;

	@Shadow
	private static Stream<Entity> removedPassengers(List<Entity> initialPassengers, List<Entity> currentPassengers) {
		return null;
	}

	@Shadow
	protected abstract void sendDirtyEntityData();
	@Shadow
	protected abstract void broadcastAndSend(Packet<?> packet);

	@Inject(method = "removePairing", at = @At("HEAD"))
	public void removePairing(ServerPlayer player, CallbackInfo ci) {
		StringBuilder builder = new StringBuilder("Remove: ");
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : elements) {
			builder.append(element).append(" |-> ");
		}

		//System.out.println(builder);
	}
	/**
	 * Determines entity movement and returns it in packets.
	 */
	@Inject(method = "sendChanges", at = @At("HEAD"), cancellable = true)
	public void wrapChanges(CallbackInfo ci) {
		WorldTransformer transformer = level.getTransformer();
		ci.cancel();

		List<Entity> list = this.entity.getPassengers();
		if (!list.equals(this.lastPassengers)) {
			this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
			removedPassengers(list, this.lastPassengers).forEach(entity -> {
				if (entity instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
				}
			});
			this.lastPassengers = list;
		}

		if (this.entity instanceof ItemFrame itemFrame && this.tickCount % 10 == 0) {
			ItemStack itemStack = itemFrame.getItem();
			if (itemStack.getItem() instanceof MapItem) {
				MapId mapId = itemStack.get(DataComponents.MAP_ID);
				MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.level);
				if (mapItemSavedData != null) {
					for (ServerPlayer serverPlayer : this.level.players()) {
						mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
						Packet<?> packet = mapItemSavedData.getUpdatePacket(mapId, serverPlayer);
						if (packet != null) {
							serverPlayer.connection.send(packet);
						}
					}
				}
			}

			this.sendDirtyEntityData();
		}

		if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
			if (this.entity.isPassenger()) {
				int i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
				int j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
				boolean bl = Math.abs(i - this.lastSentYRot) >= 1 || Math.abs(j - this.lastSentXRot) >= 1;
				if (bl) {
					this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.onGround()));
					this.lastSentYRot = i;
					this.lastSentXRot = j;
				}

				this.positionCodec.setBase(this.entity.trackingPosition());
				this.sendDirtyEntityData();
				this.wasRiding = true;
			} else {
				this.teleportDelay++;
				int i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
				int j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
				Vec3 vec3 = this.entity.trackingPosition();
				boolean bl2 = this.positionCodec.delta(vec3).lengthSqr() >= 7.6293945E-6F;
				Packet<?> packet2 = null;
				boolean bl3 = bl2 || this.tickCount % 60 == 0;
				boolean bl4 = Math.abs(i - this.lastSentYRot) >= 1 || Math.abs(j - this.lastSentXRot) >= 1;
				boolean bl5 = false;
				boolean bl6 = false;
				long l = this.positionCodec.encodeX(vec3);
				long m = this.positionCodec.encodeY(vec3);
				long n = this.positionCodec.encodeZ(vec3);
				//------------------------------ Changes
				Vec3 decodedPositionCodec = this.positionCodec.decode(0L, 0L, 0L);
				double wrappedSqr = transformer.distanceToSqrWrappedCoord(decodedPositionCodec, vec3);
				//boolean bl7 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
				boolean bl7 = wrappedSqr > 8;
				//-----------------------------------
				if (bl7 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
					this.wasOnGround = this.entity.onGround();
					this.teleportDelay = 0;
					packet2 = new ClientboundTeleportEntityPacket(this.entity);
					bl5 = true;
					bl6 = true;
				} else if ((!bl3 || !bl4) && !(this.entity instanceof AbstractArrow)) {
					if (bl3) {
						packet2 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), this.entity.onGround());
						bl5 = true;
					} else if (bl4) {
						packet2 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.onGround());
						bl6 = true;
					}
				} else {
					packet2 = new ClientboundMoveEntityPacket.PosRot(
						this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), (byte)i, (byte)j, this.entity.onGround()
					);
					bl5 = true;
					bl6 = true;
				}

				if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0) {
					Vec3 vec32 = this.entity.getDeltaMovement();
					double d = vec32.distanceToSqr(this.lastSentMovement);
					if (d > 1.0E-7 || d > 0.0 && vec32.lengthSqr() == 0.0) {
						this.lastSentMovement = vec32;
						if (this.entity instanceof AbstractHurtingProjectile abstractHurtingProjectile) {
							this.broadcast
								.accept(
									new ClientboundBundlePacket(
										List.of(
											new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement),
											new ClientboundProjectilePowerPacket(abstractHurtingProjectile.getId(), abstractHurtingProjectile.accelerationPower)
										)
									)
								);
						} else {
							this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
						}
					}
				}

				if (packet2 != null) {
					this.broadcast.accept(packet2);
				}

				this.sendDirtyEntityData();
				if (bl5) {
					this.positionCodec.setBase(vec3);
				}

				if (bl6) {
					this.lastSentYRot = i;
					this.lastSentXRot = j;
				}

				this.wasRiding = false;
			}

			int ix = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
			if (Math.abs(ix - this.lastSentYHeadRot) >= 1) {
				this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)ix));
				this.lastSentYHeadRot = ix;
			}

			this.entity.hasImpulse = false;
		}

		this.tickCount++;
		if (this.entity.hurtMarked) {
			this.entity.hurtMarked = false;
			this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
		}
	}
}
