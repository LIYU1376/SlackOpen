// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.steps.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.steps.IStep;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VerusStep implements IStep {

    @Override
    public void onUpdate(UpdateEvent event) {
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42f, mc.thePlayer.posZ, mc.thePlayer.onGround));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.85f, mc.thePlayer.posZ, mc.thePlayer.onGround));
        mc.thePlayer.stepHeight = 1f;
        mc.timer.timerSpeed = 0.91f;
    }

    @Override
    public String toString() {
        return "Verus";
    }
}
