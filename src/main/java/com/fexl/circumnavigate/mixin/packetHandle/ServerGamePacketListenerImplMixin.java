/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.packetHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Unique
	private static final Logger LOGGER = LogUtils.getLogger();
	@Unique
	ServerGamePacketListenerImpl thiz = (ServerGamePacketListenerImpl) (Object) this;

	@Shadow
	public ServerPlayer player;

	@Redirect(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
	public Vec3 unwrapVec(Vec3 instance, Vec3 vec) {
		WorldTransformer transformer = player.serverLevel().getTransformer();
		return instance.subtract(transformer.translateVecFromBounds(instance, vec));
	}

	@ModifyArg(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V"), index = 0)
	public BlockPos wrapBlockPos(BlockPos pos) {
		return player.serverLevel().getTransformer().translateBlockToBounds(pos);
	}

	//TODO: implementations of net/minecraft/world/item/Item$useOn will have to be modified in the future for support. HangingEntity, LeadItem, etc.
	@Redirect(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundUseItemOnPacket;getHitResult()Lnet/minecraft/world/phys/BlockHitResult;"))
	public BlockHitResult wrapLocationAndBlockPos(ServerboundUseItemOnPacket instance) {
		WorldTransformer transformer = player.serverLevel().getTransformer();
		BlockHitResult blockHit = instance.getHitResult();

		return new BlockHitResult(transformer.translateVecToBounds(blockHit.getLocation()), blockHit.getDirection(), transformer.translateBlockToBounds(blockHit.getBlockPos()), blockHit.isInside());
	}

	// TODO: dont override the whole method, just the part that needs to be changed
	@Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
	public void handleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread(packet, thiz, player.serverLevel());

		WorldTransformer transformer = player.serverLevel().getTransformer();
		ci.cancel();

		boolean bl;
		if (ServerGamePacketListenerImpl.containsInvalidValues(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYRot(0.0f), packet.getXRot(0.0f))) {
			thiz.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
			return;
		}
		//------------------------------------------------------
		//Disconnect the player if they move outside of the border bounds
		/**
		 if(transformer.xTransformer.isCoordOverLimit(packet.getX(transformer.centerX*16)) || transformer.zTransformer.isCoordOverLimit(packet.getZ(transformer.centerZ*16))) {
		 thiz.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
		 return;
		 }**/
		//------------------------------------------------------
		ServerLevel serverLevel = player.serverLevel();
		if (player.wonGame) {
			return;
		}
		if (thiz.tickCount == 0) {
			thiz.resetPosition();
		}
		if (thiz.awaitingPositionFromClient != null) {
			if (thiz.tickCount - thiz.awaitingTeleportTime > 20) {
				thiz.awaitingTeleportTime = thiz.tickCount;
				thiz.teleport(thiz.awaitingPositionFromClient.x, thiz.awaitingPositionFromClient.y, thiz.awaitingPositionFromClient.z, thiz.player.getYRot(), thiz.player.getXRot());
			}
			return;
		}
		thiz.awaitingTeleportTime = thiz.tickCount;

		//Wrap x to bounds
		double d = ServerGamePacketListenerImpl.clampHorizontal(transformer.xTransformer.wrapCoordToLimit(packet.getX(thiz.player.getX())));

		double e = ServerGamePacketListenerImpl.clampVertical(packet.getY(thiz.player.getY()));

		//Wrap z to bounds
		double f = ServerGamePacketListenerImpl.clampHorizontal(transformer.zTransformer.wrapCoordToLimit(packet.getZ(thiz.player.getZ())));

		//Set the client relative position
		thiz.player.setClientX(ServerGamePacketListenerImpl.clampHorizontal(packet.getX(thiz.player.getClientX())));
		thiz.player.setClientZ(ServerGamePacketListenerImpl.clampHorizontal(packet.getZ(thiz.player.getClientZ())));

		float g = Mth.wrapDegrees(packet.getYRot(thiz.player.getYRot()));
		float h = Mth.wrapDegrees(packet.getXRot(thiz.player.getXRot()));
		if (thiz.player.isPassenger()) {
			thiz.player.absMoveTo(thiz.player.getX(), thiz.player.getY(), thiz.player.getZ(), g, h);
			thiz.player.serverLevel().getChunkSource().move(thiz.player);
			return;
		}
		double i = thiz.player.getX();
		double k = thiz.player.getZ();
		double j = thiz.player.getY();
		double l = d - thiz.firstGoodX;
		double m = e - thiz.firstGoodY;
		double n = f - thiz.firstGoodZ;
		if (Math.abs(l) + 0.0625 * 2 > transformer.xWidth) {
			l = 0.0;
		}

		if (Math.abs(n) + 0.0625 * 2 > transformer.zWidth) {
			n = 0.0;
		}
		double o = thiz.player.getDeltaMovement().lengthSqr();
		double p = l * l + m * m + n * n;
		if (thiz.player.isSleeping()) {
			if (p > 1.0) {
				thiz.teleport(thiz.player.getX(), thiz.player.getY(), thiz.player.getZ(), g, h);
			}
			return;
		}
		if (serverLevel.tickRateManager().runsNormally()) {
			++thiz.receivedMovePacketCount;
			int q = thiz.receivedMovePacketCount - thiz.knownMovePacketCount;
			if (q > 5) {
				LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object) thiz.player.getName().getString(), (Object) q);
				q = 1;
			}
			if (!(thiz.player.isChangingDimension() || thiz.player.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) && thiz.player.isFallFlying())) {
				float r;
				float f2 = r = thiz.player.isFallFlying() ? 300.0f : 100.0f;
				if (p - o > (double) (r * (float) q) && !thiz.isSingleplayerOwner()) { //!thiz.isSingleplayerOwner()
					LOGGER.warn("{} moved too quickly! {},{},{}", thiz.player.getName().getString(), l, m, n);
					thiz.teleport(thiz.player.getX(), thiz.player.getY(), thiz.player.getZ(), thiz.player.getYRot(), thiz.player.getXRot());
					return;
				}
			}
		}
		AABB aABB = thiz.player.getBoundingBox();
		l = d - thiz.lastGoodX;
		m = e - thiz.lastGoodY;
		n = f - thiz.lastGoodZ;
		boolean bl2 = bl = m > 0.0;
		if (thiz.player.onGround() && !packet.isOnGround() && bl) {
			thiz.player.jumpFromGround();
		}
		boolean bl22 = thiz.player.verticalCollisionBelow;
		thiz.player.move(MoverType.PLAYER, new Vec3(l, m, n));
		double s = m;
		l = d - thiz.player.getX();
		m = e - thiz.player.getY();
		if (m > -0.5 || m < 0.5) {
			m = 0.0;
		}
		n = f - thiz.player.getZ();

		//TODO doesn't work in worlds where the min and max bounds aren't opposites (i.g. -32 -> 32)
		//------------------------------------------------------
		//Don't invalidate the player movement if they moved across the world border
		if (Math.abs(l) + 0.0625 * 2 > transformer.xWidth) {
			l = 0.0;
		}

		if (Math.abs(n) + 0.0625 * 2 > transformer.zWidth) {
			n = 0.0;
		}
		//------------------------------------------------------
		p = l * l + m * m + n * n;
		boolean bl3 = false;
		if (!thiz.player.isChangingDimension() && p > 0.0625 && !thiz.player.isSleeping() && !thiz.player.gameMode.isCreative() && thiz.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
			bl3 = true;
			LOGGER.warn("{} moved wrongly!", (Object) thiz.player.getName().getString());
		}

		if (!thiz.player.noPhysics && !thiz.player.isSleeping() && (bl3 && serverLevel.noCollision(thiz.player, aABB)) || thiz.isPlayerCollidingWithAnythingNew(serverLevel, aABB, d, e, f)) {
			thiz.teleport(i, j, k, g, h);
			thiz.player.doCheckFallDamage(thiz.player.getX() - i, thiz.player.getY() - j, thiz.player.getZ() - k, packet.isOnGround());
			return;
		}
		thiz.player.absMoveTo(d, e, f, g, h);
		thiz.clientIsFloating = s >= -0.03125 && !bl22 && thiz.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !thiz.server.isFlightAllowed() && !thiz.player.getAbilities().mayfly && !thiz.player.hasEffect(MobEffects.LEVITATION) && !thiz.player.isFallFlying() && !thiz.player.isAutoSpinAttack() && thiz.noBlocksAround(thiz.player);
		thiz.player.serverLevel().getChunkSource().move(thiz.player);
		thiz.player.doCheckFallDamage(thiz.player.getX() - i, thiz.player.getY() - j, thiz.player.getZ() - k, packet.isOnGround());
		thiz.player.setKnownMovement(new Vec3(thiz.player.getX() - i, thiz.player.getY() - j, thiz.player.getZ() - k));
		if (bl) {
			thiz.player.resetFallDistance();
		}
		thiz.player.checkMovementStatistics(thiz.player.getX() - i, thiz.player.getY() - j, thiz.player.getZ() - k);
		thiz.lastGoodX = thiz.player.getX();
		thiz.lastGoodY = thiz.player.getY();
		thiz.lastGoodZ = thiz.player.getZ();
	}


	@Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true)
	public void handleMoveVehicle(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread(packet, thiz, player.serverLevel());
		WorldTransformer transformer = player.serverLevel().getTransformer();
		ci.cancel();

		if (ServerGamePacketListenerImpl.containsInvalidValues(packet.getX(), packet.getY(), packet.getZ(), packet.getYRot(), packet.getXRot())) {
			thiz.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
			return;
		}

		Entity entity = thiz.player.getRootVehicle();
		if (entity != thiz.player && entity.getControllingPassenger() == thiz.player && entity == thiz.lastVehicle) {
			ServerLevel serverLevel = thiz.player.serverLevel();
			double d = entity.getX();
			double e = entity.getY();
			double f = entity.getZ();
			// Warp x and z to bounds
			double g = ServerGamePacketListenerImpl.clampHorizontal(transformer.xTransformer.wrapCoordToLimit(packet.getX()));
			double h = ServerGamePacketListenerImpl.clampVertical(packet.getY());
			double i = ServerGamePacketListenerImpl.clampHorizontal(transformer.zTransformer.wrapCoordToLimit(packet.getZ()));
			float j = Mth.wrapDegrees(packet.getYRot());
			float k = Mth.wrapDegrees(packet.getXRot());
			double l = g - thiz.vehicleFirstGoodX;
			double m = h - thiz.vehicleFirstGoodY;
			double n = i - thiz.vehicleFirstGoodZ;
			if (Math.abs(l) + 0.0625 * 2 > transformer.xWidth) {
				l = 0.0;
			}

			if (Math.abs(n) + 0.0625 * 2 > transformer.zWidth) {
				n = 0.0;
			}
			double o = entity.getDeltaMovement().lengthSqr();
			double p = l * l + m * m + n * n;
			if (p - o > 100.0 && !thiz.isSingleplayerOwner()) {
				LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), thiz.player.getName().getString(), l, m, n);
				thiz.send(new ClientboundMoveVehiclePacket(entity));
				return;
			}
			boolean bl = serverLevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
			l = g - thiz.vehicleLastGoodX;
			m = h - thiz.vehicleLastGoodY - 1.0E-6;
			n = i - thiz.vehicleLastGoodZ;
			boolean bl2 = entity.verticalCollisionBelow;
			if (entity instanceof LivingEntity livingEntity && livingEntity.onClimbable()) {
				livingEntity.resetFallDistance();
			}

			entity.move(MoverType.PLAYER, new Vec3(l, m, n));
			l = g - entity.getX();
			m = h - entity.getY();
			if (m > -0.5 || m < 0.5) {
				m = 0.0;
			}
			n = i - entity.getZ();

			// TODO doesn't work in worlds where the min and max bounds aren't opposites (i.g. -32 -> 32)
			// Don't invalidate the vehicle movement if it moved across the world border
			//------------------------------------------------------
			if (Math.abs(l) + 0.0625 * 2 > transformer.xWidth) {
				l = 0.0;
			}

			if (Math.abs(n) + 0.0625 * 2 > transformer.zWidth) {
				n = 0.0;
			}
			//------------------------------------------------------
			p = l * l + m * m + n * n;
			boolean bl3 = false;
			if (p > 0.0625) {
				bl3 = true;
				LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), thiz.player.getName().getString(), Math.sqrt(p));
			}
			entity.absMoveTo(g, h, i, j, k);
			boolean bl4 = serverLevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
			if (bl && (bl3 || !bl4)) {
				entity.absMoveTo(d, e, f, j, k);
				thiz.send(new ClientboundMoveVehiclePacket(entity));
				return;
			}
			thiz.player.serverLevel().getChunkSource().move(thiz.player);
			thiz.player.checkMovementStatistics(thiz.player.getX() - d, thiz.player.getY() - e, thiz.player.getZ() - f);
			thiz.clientVehicleIsFloating = m >= -0.03125 && !bl2 && !thiz.server.isFlightAllowed() && !entity.isNoGravity() && thiz.noBlocksAround(entity);
			thiz.vehicleLastGoodX = entity.getX();
			thiz.vehicleLastGoodY = entity.getY();
			thiz.vehicleLastGoodZ = entity.getZ();
		}
	}
}
