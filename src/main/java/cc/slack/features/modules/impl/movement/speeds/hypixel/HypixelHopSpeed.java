// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.start.Slack;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.KillAura;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class HypixelHopSpeed implements ISpeed {

    int jumpTick = 0;
    boolean wasSlow = false;

    float yaw = 0f;
    float speed = 0f;

    boolean ok = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.hurtTime > 7) ok = false;
        if (mc.thePlayer.onGround) {
            ok = mc.thePlayer.posY % 1 == 0;
            if (MovementUtil.isMoving()) {
                wasSlow = false;
                if (jumpTick > 7) jumpTick = 5;
                mc.thePlayer.jump();
                MovementUtil.strafe(0.465f + jumpTick * 0.007f);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.46f + jumpTick * 0.008f + 0.023f * (amplifier + 1));
                }
                yaw = MovementUtil.getDirection();
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
            } else {
                jumpTick = 0;
            }
        } else {
            if (mc.thePlayer.offGroundTicks == 1) {
                MovementUtil.strafe(0.34f);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtil.strafe(0.37f);
                }
                return;
            }
            mc.thePlayer.motionX *= 1.0005;
            mc.thePlayer.motionZ *= 1.0005;

            if (mc.thePlayer.offGroundTicks < 13) {
                if (mc.thePlayer.motionY < 0) {
                    if (jumpTick < 5 && !Slack.getInstance().getModuleManager().getInstance(Speed.class).enabledTime.hasReached(7000)) {
                        mc.timer.timerSpeed = 1.07f + (float) Math.random() * 0.07f;
                    } else {
                        mc.timer.timerSpeed = 1f;
                    }
                } else {
                    mc.timer.timerSpeed = 0.95f;
                }
            } else {
                mc.timer.timerSpeed = 1f;
            }

            if (Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelTest.getValue()) {
                if (ok) {
                    if (mc.thePlayer.offGroundTicks == 4) {
                        mc.thePlayer.motionY -= 0.03;
                    } else if (mc.thePlayer.offGroundTicks == 6) {
                        mc.thePlayer.motionY -= 0.2;
                    }
                }
            }

            if (Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelSemiStrafe.getValue()) {
                if (mc.thePlayer.offGroundTicks == 6) {
                    MovementUtil.strafe(MovementUtil.getSpeed());
                }
            }
        }

        if (Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelGlide.getValue()) {
            if (mc.thePlayer.onGround) {
                speed = 1F;
            }

            final int[] allowedAirTicks = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};

            if (!PlayerUtil.isOverAir(mc.thePlayer.posX, mc.thePlayer.posY - 0.4 + 1, mc.thePlayer.posZ)) {
                for (final int allowedAirTick : allowedAirTicks) {
                    if (mc.thePlayer.offGroundTicks == allowedAirTick && allowedAirTick <= 9 + 15 && MovementUtil.isMoving()) {
                        mc.thePlayer.motionY = 0;
                        MovementUtil.strafe(0.2873f * speed);

                        speed *= 0.98F;

                    }
                }
            }
        }

    }

    @Override
    public void onPacket (PacketEvent event) {
        Packet packet = event.getPacket();
        if (mc.thePlayer.onGround && packet instanceof C03PacketPlayer && Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelTest.getValue()) {
            ((C03PacketPlayer) packet).onGround = true;
            ((C03PacketPlayer) packet).y += 0.0000000001;
            event.setPacket(packet);
        }
    }

    @Override
    public String toString() {
        return "Hypixel";
    }
}
