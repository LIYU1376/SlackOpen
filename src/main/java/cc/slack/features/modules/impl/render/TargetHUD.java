// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render;

import java.awt.Color;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.start.Slack;
import cc.slack.utils.drag.DragUtil;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector4d;

@ModuleInfo(name = "TargetHUD", category = Category.RENDER)
public class TargetHUD extends Module {

	private final ModeValue<String> mode = new ModeValue<>(new String[] {"Slack", "Classic", "Classic2", "New"});
	private final BooleanValue roundedValue = new BooleanValue("Rounded", false);
	public final BooleanValue resetPos = new BooleanValue("Reset Position", false);

	private final BooleanValue followTarget = new BooleanValue("Follow Target", false);
	private final BooleanValue smoothFollow = new BooleanValue("Smooth Follow", false);

	private final NumberValue<Double> posX = new NumberValue<>("Xpos", 50.0, -300.0, 300.0, 1.0);
	private final NumberValue<Double> posY = new NumberValue<>("Ypos", -20.0, -300.0, 300.0, 1.0);

	private int x = 0;
	private int y = 0;
	public double health;
	public double smoothSpeed;

	public TargetHUD() {
		addSettings(mode, roundedValue, resetPos,followTarget, smoothFollow, posX, posY);
	}

	private EntityPlayer target;
	private int ticksSinceAttack;

	@Override
	public void onEnable() {
		target = null;
	}

	@Listen
	public void onUpdate(UpdateEvent event) {
		ticksSinceAttack++;

		if (ticksSinceAttack > 20) {
			target = null;
			y = -100;
		}

		if (mc.getCurrentScreen() instanceof GuiChat) {
			target = mc.thePlayer;
			ticksSinceAttack = 18;
		}
	}

	@Listen
	public void onPacket(PacketEvent event) {
		if (event.getPacket() instanceof C02PacketUseEntity) {
			C02PacketUseEntity wrapper = event.getPacket();
			if (wrapper.getEntityFromWorld(mc.theWorld) instanceof EntityPlayer
					&& wrapper.getAction() == C02PacketUseEntity.Action.ATTACK) {
				ticksSinceAttack = 0;
				target = (EntityPlayer) wrapper.getEntityFromWorld(mc.theWorld);
			}
		}
	}

	@Listen
	public void onRender(RenderEvent event) {
		smoothSpeed = 3.3f;
		if (resetPos.getValue()) {
			posX.setValue(50d);
			posY.setValue(-20d);
			Slack.getInstance().getModuleManager().getInstance(TargetHUD.class).resetPos.setValue(false);
		}

		if (event.getState() != RenderEvent.State.RENDER_2D) return;

		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		double renderX = posX.getValue() + sr.getScaledWidth() / 2.0;
		double renderY = posY.getValue() + sr.getScaledHeight() / 2.0;

		if(target != null) {
			if (health < target.getHealth()) {
				health = target.getHealth();
			}

			if (health > target.getHealth()) {
				health -= 0.01 + (health - (double) Math.round(target.getHealth() * 10) / 10) / smoothSpeed;
			}
		}

		if (target == null)
			return;

		if (followTarget.getValue() && target != mc.thePlayer) {
			try {
				Vector4d pos4 = RenderUtil.getProjectedEntity(target, event.getPartialTicks(), 0.7);
				mc.entityRenderer.setupOverlayRendering();
				if (smoothFollow.getValue() && y != -100) {
					int gx = ((int) Math.max(pos4.x, pos4.z) + 10);
					int gy = (int) pos4.y;
					gx = MathHelper.clamp_int(gx, 0, sr.getScaledWidth() - 120);
					gy = MathHelper.clamp_int(gy, 0, sr.getScaledHeight() - 50);

					x += (gx - x) / Math.pow(smoothSpeed, Minecraft.getDebugFPS() / 20);
					y += (gy - y) / Math.pow(smoothSpeed, Minecraft.getDebugFPS() / 20);

				} else {
					x = ((int) Math.max(pos4.x, pos4.z) + 10);
					y = (int) pos4.y;
					x = MathHelper.clamp_int(x, 0, sr.getScaledWidth() - 120);
					y = MathHelper.clamp_int(y, 0, sr.getScaledHeight() - 50);
				}
			} catch (Exception ignored) {
				// entity is missing
			}
		} else {
			x = (int) renderX;
			y = (int) renderY;
		}

		String targetName = target.getCommandSenderName();
		double offset = -(target.hurtTime * 20);
		double healthPercent = target.getHealth() / target.getMaxHealth();
		Color color = new Color(255, (int) (255 + offset), (int) (255 + offset));
		if (target.hurtTime == 0) {
			color = new Color(255, 255, 255);
		}
		Boolean winning = target.getHealth() < mc.thePlayer.getHealth();
		Color c = ColorUtil.getColor();

		if(health > 0) {
			switch (mode.getValue().toLowerCase()) {
				case "classic":
					if (!roundedValue.getValue()) {
						drawRect(x, y, 120, 40, new Color(0, 0, 0, 120).getRGB());
						Fonts.poppins18.drawString(targetName, x + 40, y + 8, c.getRGB());
						GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
								color.getAlpha() / 255F);
						mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
						Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 30, 30, 24, 24);
						GlStateManager.color(1, 1, 1, 1);

						drawRect(x + 40, y + 20, 70, 15, new Color(255, 255, 255, 120).getRGB());

						drawRect(x + 40, y + 20, (int) (70 * (target.getHealth() / target.getMaxHealth())), 15,
								c.getRGB());

						String s = (int) (healthPercent * 100) + "%";
						Fonts.poppins18.drawString(s, x + 40 + (70 / 2) - (Fonts.poppins18.getStringWidth(s) / 2),
								y + 20 + (15 / 2) - (Fonts.poppins18.getHeight() / 2) + 1, -1);
					} else {
						drawRoundedRect(x, y, 120, 40, 6, new Color(0, 0, 0, 120).getRGB());
						mc.MCfontRenderer.drawString(targetName, x + 40, y + 8, c.getRGB());
						GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
								color.getAlpha() / 255F);
						mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
						Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 30, 30, 24, 24);
						GlStateManager.color(1, 1, 1, 1);

						drawRoundedRect(x + 40, y + 20, 70, 15, 2, new Color(255, 255, 255, 120).getRGB());

						drawRoundedRect(x + 40, y + 20, (int) (70 * (target.getHealth() / target.getMaxHealth())), 15, 2,
								c.getRGB());

						String shp = (int) (healthPercent * 100) + "%";
						mc.MCfontRenderer.drawString(shp, x + 40 + (70 / 2) - (mc.MCfontRenderer.getStringWidth(shp) / 2),
								y + 20 + (15 / 2) - (mc.MCfontRenderer.FONT_HEIGHT / 2) + 1, -1);
						mc.MCfontRenderer.drawString(shp, x + 40 + (70 / 2) - (mc.MCfontRenderer.getStringWidth(shp) / 2),
								y + 20 + (15 / 2) - (mc.MCfontRenderer.FONT_HEIGHT / 2) + 1, -1);
					}
					break;
				case "classic2":
					if (!roundedValue.getValue()) {
						drawRect(x, y, 120, 50, new Color(0, 0, 0, 120).getRGB());

						mc.MCfontRenderer.drawString(targetName, x + 35, y + 8, c.getRGB());
						mc.MCfontRenderer.drawString(String.format("%.2f", target.getHealth()), x + 35, y + 18, c.getRGB());
						mc.MCfontRenderer.drawString(winning ? "W" : "L", x + 107, y + 18, c.getRGB());

						GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
								color.getAlpha() / 255F);
						mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
						Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 25, 25, 24, 24);
						GlStateManager.color(1, 1, 1, 1);

						drawRect(x + 5, y + 35, 110, 10, new Color(255, 255, 255, 120).getRGB());
						drawRect(x + 5, y + 35, (int) (110 * (target.getHealth() / target.getMaxHealth())), 10,
								c.getRGB());
					} else {
						drawRoundedRect(x, y, 120, 50, 4, new Color(0, 0, 0, 120).getRGB());

						mc.MCfontRenderer.drawString(targetName, x + 35, y + 8, c.getRGB());
						mc.MCfontRenderer.drawString(String.format("%.2f", target.getHealth()), x + 35, y + 18, c.getRGB());
						mc.MCfontRenderer.drawString(winning ? "W" : "L", x + 107, y + 18, c.getRGB());

						GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
								color.getAlpha() / 255F);
						mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
						Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 25, 25, 24, 24);
						GlStateManager.color(1, 1, 1, 1);

						drawRoundedRect(x + 5, y + 35, 110, 10, 2, new Color(255, 255, 255, 120).getRGB());
						drawRoundedRect(x + 5, y + 35, (int) (110 * (target.getHealth() / target.getMaxHealth())), 10, 2,
								c.getRGB());
					}
					break;
				case "slack":
					drawRoundedRect(x, y, 162, 40, 10, new Color(0, 0, 0, 150).getRGB());
					GlStateManager.color(1, 1, 1, 1);
					GlStateManager.color(1, 1, 1, 1);

					Fonts.apple18.drawString(targetName, x + 40, y + 9, new Color(255, 255, 255, 255).getRGB());
					Fonts.apple18.drawString(String.format("%.1f", target.getHealth()), x + 139, y + 24, new Color(255, 255, 255, 255).getRGB());

					GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
							color.getAlpha() / 255F);
					mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
					Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 30, 30, 24, 24);
					GlStateManager.color(1, 1, 1, 1);

					drawRoundedRect(x + 40, y + 23, 95, 9, 3, new Color(151, 151, 151, 40).getRGB());
					GlStateManager.color(1, 1, 1, 1);
					drawRoundedRect(x + 40, y + 23, (int) ((health * 5 - 4.5)), 9, 3,
							c.getRGB());
					GlStateManager.color(1, 1, 1, 1);
					RenderUtil.drawRoundedRectBorder(x + 40, y + 23, x + 40 + 95, y + 23 + 9, 3, new Color(230, 230, 230, 200).getRGB(), 1);
					GlStateManager.color(1, 1, 1, 1);
					RenderUtil.drawRoundedRectBorder(x + 39, y + 22, x + 41 + 95, y + 23 + 10, 3, new Color(30, 30, 30, 100).getRGB(), 1);
					break;
				case "new":
					drawRoundedRect(x, y, 165, 40, 5, ColorUtil.getMaterial(true).getRGB());
					GlStateManager.color(1, 1, 1, 1);
					GlStateManager.color(1, 1, 1, 1);

					Fonts.sfRoundedBold20.drawString(targetName, x + 40, y + 12, new Color(255, 255, 255, 255).getRGB());
					Fonts.sfRoundedBold18.drawString(String.format("%.1f", target.getHealth()), x + 139, y + 27, new Color(255, 255, 255, 255).getRGB());

					GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F,
							color.getAlpha() / 255F);
					mc.getTextureManager().bindTexture(((AbstractClientPlayer) target).getLocationSkin());
					Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 30, 30, 24, 24);
					GlStateManager.color(1, 1, 1, 1);

					drawRoundedRect(x + 40, y + 27, 95, 7, 3, ColorUtil.getMaterial(false).getRGB());
					GlStateManager.color(1, 1, 1, 1);
					drawRoundedRect(x + 40, y + 27, (int) ((health * 5 - 4.5)), 7, 3,
							c.getRGB());
					GlStateManager.color(1, 1, 1, 1);
					break;
			}
		}
	}
	
	@Override
	public DragUtil getPosition() {
		if (target == null)
			return null;
		
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		double renderX = posX.getValue() + sr.getScaledWidth() / 2.0;
		double renderY = posY.getValue() + sr.getScaledHeight() / 2.0;
		double[] pos = DragUtil.setScaledPosition(renderX, renderY);
		return new DragUtil(pos[0], pos[1], 120, 50, 1);
	}
	
	@Override
	public void setXYPosition(double x, double y) {
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		posX.setValue(x - sr.getScaledWidth() / 2.0);
		posY.setValue(y - sr.getScaledHeight() / 2.0);
	}

	private void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
		RenderUtil.drawRoundedRect(x, y, x + width, y + height, radius, color);
	}

	private void drawRect(int x, int y, int width, int height, int color) {
		Gui.drawRect(x, y, x + width, y + height, color);
	}

}
