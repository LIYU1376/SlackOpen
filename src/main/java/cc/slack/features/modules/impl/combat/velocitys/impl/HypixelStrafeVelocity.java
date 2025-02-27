// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.impl.player.PostStrafeEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class HypixelStrafeVelocity implements IVelocity {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.hurtTime == 9) {
            MovementUtil.strafe(MovementUtil.getSpeed() * 0.8f);
        }
    }

    @Override
    public String toString() {
        return "Hypixel Damage Strafe";
    }
}
