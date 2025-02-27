// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render;

import java.util.Collection;

import cc.slack.events.impl.render.RenderEvent;
import cc.slack.events.impl.render.RenderScoreboard;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.start.Slack;
import cc.slack.utils.drag.DragUtil;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.font.MCFontRenderer;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatFormatting;
import org.lwjgl.input.Mouse;

import static net.minecraft.client.gui.Gui.drawRect;

@ModuleInfo(name = "Scoreboard", category = Category.RENDER)
public class ScoreboardModule extends Module {

	private final BooleanValue noscoreboard = new BooleanValue("No Scoreboard", false);
	private final BooleanValue roundedValue = new BooleanValue("Rounded", false);
	public final BooleanValue resetPos = new BooleanValue("Reset Position", false);

	private final BooleanValue textShadow = new BooleanValue("Text Shadow", false);
	private final BooleanValue lineNumbersValue = new BooleanValue("Line Numbers", true);
	private final ModeValue<String> scoreboardFont = new ModeValue<>("Font", new String[]{"Minecraft", "Apple", "Poppins", "Roboto"});
	private static final NumberValue<Integer> scoreboardFontScale = new NumberValue<>("Font Scale", 18, 1, 30, 1);
	private final NumberValue<Float> xValue = new NumberValue<>("Xpos", 0F, -300F, 0F, 1F);
	private final NumberValue<Float> yValue = new NumberValue<>("Ypos", 160F, 1.0F, 300.0F, 1F);

	double posX = 0.0D;
	double posY = 30.0D;

	private MCFontRenderer appleFontRenderer;
	private MCFontRenderer poppinsFontRenderer;
	private MCFontRenderer robotoFontRenderer;

	private int lastFontScaleValue = -1;

	private boolean dragging = false;
	private float dragX = 0, dragY = 0;


	public ScoreboardModule() {
		addSettings(noscoreboard, roundedValue, resetPos,textShadow, lineNumbersValue, scoreboardFont, scoreboardFontScale, xValue, yValue);
	}

	@Listen
	public void onRenderScoreboard(RenderScoreboard event) {
		event.cancel();
	}

	private void updateFontRenderers() {
		int currentScaleValue = scoreboardFontScale.getValue();
		if (currentScaleValue != lastFontScaleValue) {
			appleFontRenderer = Fonts.getFontRenderer("Apple", currentScaleValue);
			poppinsFontRenderer = Fonts.getFontRenderer("Poppins", currentScaleValue);
			robotoFontRenderer = Fonts.getFontRenderer("Roboto", currentScaleValue);
			lastFontScaleValue = currentScaleValue;
		}
	}

	@Listen
	public void onRender(RenderEvent event) {
		if (resetPos.getValue()) {
			posX = 0D;
			posY = 160D;
			Slack.getInstance().getModuleManager().getInstance(ScoreboardModule.class).resetPos.setValue(false);
		}

		if (event.getState() != RenderEvent.State.RENDER_2D) return;
		if (noscoreboard.getValue()) return;

		int x = xValue.getValue().intValue();
		int y = yValue.getValue().intValue();

		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
		int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

		if (dragging) {
			xValue.setValue((float) (mouseX - dragX));
			yValue.setValue((float) (mouseY - dragY));
		}


		updateFontRenderers();

		ScaledResolution scaledRes = new ScaledResolution(mc);
		Scoreboard scoreboard = mc.theWorld.getScoreboard();
		ScoreObjective scoreobjective = null;
		ScoreObjective objective = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

		if (objective == null) return;

		double[] pos = DragUtil.setScaledPosition(this.posX, this.posY);
		scoreboard = objective.getScoreboard();
		Collection<Score> collection = scoreboard.getSortedScores(objective);

		double i = 70;
		if (mc.MCfontRenderer.getStringWidth(objective.getDisplayName()) > i) {
			i = mc.MCfontRenderer.getStringWidth(objective.getDisplayName());
		}
		double width = i;
		for (Score score2 : collection) {
			ScorePlayerTeam scoreplayerteam2 = scoreboard.getPlayersTeam(score2.getPlayerName());
			String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam2, score2.getPlayerName()) + ": " + ChatFormatting.RED + score2.getScorePoints();

			if (width < mc.MCfontRenderer.getStringWidth(s1)) {
				width = mc.MCfontRenderer.getStringWidth(s1);
			}
		}

		int height = collection.size() * 9 + 16;
		if (roundedValue.getValue()) {
			RenderUtil.drawRoundedRect((float) pos[0], (float) pos[1], (float) (pos[0] + width), (float) (pos[1] + height), 8F, 1342177280);
		} else {
			drawRect(pos[0], pos[1] + 2, pos[0] + width, pos[1] + 12, 1610612736); // top
			drawRect(pos[0], pos[1] + 2, (pos[0] + width), pos[1] + height, 1342177280);
		}

		switch (scoreboardFont.getValue()) {
			case "Minecraft":
				mc.MCfontRenderer.drawString(objective.getDisplayName(), (float) (pos[0] + width / 2 - mc.MCfontRenderer.getStringWidth(objective.getDisplayName()) / 2), (float) pos[1] + 4, -1, textShadow.getValue());
				break;
			case "Apple":
				appleFontRenderer.drawString(objective.getDisplayName(), (float) (pos[0] + width / 2 - appleFontRenderer.getStringWidth(objective.getDisplayName()) / 2), (float) pos[1] + 4, -1, textShadow.getValue());
				break;
			case "Poppins":
				poppinsFontRenderer.drawString(objective.getDisplayName(), (float) (pos[0] + width / 2 - poppinsFontRenderer.getStringWidth(objective.getDisplayName()) / 2), (float) pos[1] + 4, -1, textShadow.getValue());
				break;
			case "Roboto":
				robotoFontRenderer.drawString(objective.getDisplayName(), (float) (pos[0] + width / 2 - robotoFontRenderer.getStringWidth(objective.getDisplayName()) / 2), (float) pos[1] + 4, -1, textShadow.getValue());
				break;
		}

		int j = 0;
		for (Score score1 : collection) {
			++j;
			ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
			String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
			String s2 = (lineNumbersValue.getValue() ? ChatFormatting.RED + "" + score1.getScorePoints() : "");

			switch (scoreboardFont.getValue()) {
				case "Minecraft":
					mc.MCfontRenderer.drawString(s1, (float) pos[0] + 3, (float) (pos[1] + height - 9.2 * j), -1, textShadow.getValue());
					mc.MCfontRenderer.drawString(s2, (float) (pos[0] + width - mc.MCfontRenderer.getStringWidth(s2)), (float) (pos[1] + height - 9 * j), -1, textShadow.getValue());
					break;
				case "Apple":
					appleFontRenderer.drawString(s1, (float) pos[0] + 3, (float) (pos[1] + height - 9.2 * j), -1, textShadow.getValue());
					appleFontRenderer.drawString(s2, (float) (pos[0] + width - appleFontRenderer.getStringWidth(s2)), (float) (pos[1] + height - 9 * j), -1, textShadow.getValue());
					break;
				case "Poppins":
					poppinsFontRenderer.drawString(s1, (float) pos[0] + 3, (float) (pos[1] + height - 9.2 * j), -1, textShadow.getValue());
					poppinsFontRenderer.drawString(s2, (float) (pos[0] + width - poppinsFontRenderer.getStringWidth(s2)), (float) (pos[1] + height - 9 * j), -1, textShadow.getValue());
					break;
				case "Roboto":
					robotoFontRenderer.drawString(s1, (float) pos[0] + 3, (float) (pos[1] + height - 9.2 * j), -1, textShadow.getValue());
					robotoFontRenderer.drawString(s2, (float) (pos[0] + width - robotoFontRenderer.getStringWidth(s2)), (float) (pos[1] + height - 9 * j), -1, textShadow.getValue());
					break;
			}
		}
		handleMouseInput(mouseX, mouseY, x, y, (int) width, height);
	}

	private void handleMouseInput(int mouseX, int mouseY, int rectX, int rectY, int rectWidth, int rectHeight) {
		if (mc.currentScreen instanceof GuiChat) {
			if (Mouse.isButtonDown(0)) {
				if (!dragging) {
					if (mouseX >= rectX && mouseX <= rectX + rectWidth &&
							mouseY >= rectY && mouseY <= rectY + rectHeight) {
						dragging = true;
						dragX = mouseX - xValue.getValue();
						dragY = mouseY - yValue.getValue();
					}
				}
			} else {
				dragging = false;
			}
		}
	}
}
