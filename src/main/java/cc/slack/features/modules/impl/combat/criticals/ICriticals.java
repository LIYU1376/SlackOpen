package cc.slack.features.modules.impl.combat.criticals;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.*;
import net.minecraft.client.Minecraft;

public interface ICriticals {
    Minecraft mc = Minecraft.getMinecraft();

    default void onEnable() {
    }

    ;

    default void onDisable() {
    }

    ;

    default void onMove(MoveEvent event) {
    }

    ;

    default void onPacket(PacketEvent event) {
    }

    ;

    default void onCollide(CollideEvent event) {
    }

    ;

    default void onUpdate(UpdateEvent event) {
    }

    ;

    default void onMotion(MotionEvent event) {
    }

    ;

    default void onAttack(AttackEvent event) {

    }

    ;
}
