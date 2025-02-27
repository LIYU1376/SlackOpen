// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.speeds.vanilla;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.utils.player.MovementUtil;

public class StrafeSpeed implements ISpeed {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
            mc.thePlayer.jump();
        }
        MovementUtil.strafe();
    }

    @Override
    public String toString() {
        return "Strafe";
    }
}
