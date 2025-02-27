package net.minecraft.client.gui;

import java.io.IOException;
import java.util.List;

import cc.slack.features.modules.impl.other.Tweaks;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import cc.slack.start.Slack;
import cc.slack.features.modules.api.Module;
import cc.slack.utils.drag.DragUtil;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

public class GuiChat extends GuiScreen {
	private static final Logger logger = LogManager.getLogger();
	private List<String> foundPlayerNames = Lists.<String>newArrayList();
	private String historyBuffer = "";

	/**
	 * keeps position of which chat message you will select when you press up, (does
	 * not increase for duplicated messages sent immediately after each other)
	 */
	private int sentHistoryCursor = -1;
	private boolean playerNamesFound;
	private boolean waitingOnAutocomplete;
	private int autocompleteIndex;
	
	private Module draggingModule;
	private boolean dragging;
	private double dragX, dragY;

	/** Chat entry field */
	protected GuiTextField inputField;

	/**
	 * is the text that appears when you press the chat key and the input box
	 * appears pre-filled
	 */
	private String defaultInputFieldText = "";

	public GuiChat() {
	}

	public GuiChat(String defaultText) {
		this.defaultInputFieldText = defaultText;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
		this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
		this.inputField.setMaxStringLength(Slack.getInstance().getModuleManager().getInstance(Tweaks.class).biggerChat.getValue() ? 256 :  100);
		this.inputField.setEnableBackgroundDrawing(false);
		this.inputField.setFocused(true);
		this.inputField.setText(this.defaultInputFieldText);
		this.inputField.setCanLoseFocus(false);
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		this.mc.ingameGUI.getChatGUI().resetScroll();
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		this.inputField.updateCursorCounter();
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the
	 * equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character
	 * on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		this.waitingOnAutocomplete = false;

		if (keyCode == 15) {
			this.autocompletePlayerNames();
		} else {
			this.playerNamesFound = false;
		}

		if (keyCode == 1) {
			this.mc.displayGuiScreen(null);
		} else if (keyCode != 28 && keyCode != 156) {
			if (keyCode == 200) {
				this.getSentHistory(-1);
			} else if (keyCode == 208) {
				this.getSentHistory(1);
			} else if (keyCode == 201) {
				this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
			} else if (keyCode == 209) {
				this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
			} else {
				this.inputField.textboxKeyTyped(typedChar, keyCode);
			}
		} else {
			String s = this.inputField.getText().trim();

			if (!s.isEmpty()) {
				this.sendChatMessage(s);
			}

			this.mc.displayGuiScreen(null);
		}
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int i = Mouse.getEventDWheel();

		if (i != 0) {
			if (i > 1) {
				i = 1;
			}

			if (i < -1) {
				i = -1;
			}

			if (!isShiftKeyDown()) {
				i *= 7;
			}

			this.mc.ingameGUI.getChatGUI().scroll(i);
		}
	}

	/*
	 * protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws
	 * IOException { if (mouseButton == 0) { IChatComponent ichatcomponent =
	 * this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
	 * 
	 * if (this.handleComponentClick(ichatcomponent)) { return; } }
	 * 
	 * this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
	 * super.mouseClicked(mouseX, mouseY, mouseButton); }
	 */

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (mouseButton == 0) {
			IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

			if (this.handleComponentClick(ichatcomponent)) {
				return;
			}
		}

		double[] mousePos = DragUtil.setScaledPosition(mouseX, mouseY);
		mouseX = (int) mousePos[0];
		mouseY = (int) mousePos[1];

		this.inputField.mouseClicked(mouseX, mouseY, mouseButton);

		for (Module module : Slack.getInstance().getModuleManager().getDraggable()) {
			if (!module.isToggle() || module.getPosition() == null)
				continue;
			if (module.getPosition().isInside(mouseX, mouseY) && !dragging) {
				draggingModule = module;
				dragging = true;
				double[] pos = DragUtil.setPosition(module.getPosition().getX(), module.getPosition().getY());
				dragX = mouseX - pos[0];
				dragY = mouseY - pos[1];
				return;
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		dragging = false;
		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Sets the text of the chat
	 * 
	 * @param newChatText     The new chat text to be set
	 * @param shouldOverwrite Determines if the text currently in the chat should be
	 *                        overwritten or appended
	 */
	protected void setText(String newChatText, boolean shouldOverwrite) {
		if (shouldOverwrite) {
			this.inputField.setText(newChatText);
		} else {
			this.inputField.writeText(newChatText);
		}
	}

	public void autocompletePlayerNames() {
		if (this.playerNamesFound) {
			this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

			if (this.autocompleteIndex >= this.foundPlayerNames.size()) {
				this.autocompleteIndex = 0;
			}
		} else {
			int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
			this.foundPlayerNames.clear();
			this.autocompleteIndex = 0;
			String s = this.inputField.getText().substring(i).toLowerCase();
			String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
			this.sendAutocompleteRequest(s1, s);

			if (this.foundPlayerNames.isEmpty()) {
				return;
			}

			this.playerNamesFound = true;
			this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
		}

		if (this.foundPlayerNames.size() > 1) {
			StringBuilder stringbuilder = new StringBuilder();

			for (String s2 : this.foundPlayerNames) {
				if (stringbuilder.length() > 0) {
					stringbuilder.append(", ");
				}

				stringbuilder.append(s2);
			}

			this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
		}

		this.inputField.writeText(this.foundPlayerNames.get(this.autocompleteIndex++));
	}

	private void sendAutocompleteRequest(String p_146405_1_, String p_146405_2_) {
		if (!p_146405_1_.isEmpty()) {
			BlockPos blockpos = null;

			if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				blockpos = this.mc.objectMouseOver.getBlockPos();
			}

			this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_, blockpos));
			this.waitingOnAutocomplete = true;
		}
	}

	/**
	 * input is relative and is applied directly to the sentHistoryCursor so -1 is
	 * the previous message, 1 is the next message from the current cursor position
	 * 
	 * @param msgPos The position of the message in the sent chat history relative
	 *               to the current message.
	 */
	public void getSentHistory(int msgPos) {
		int i = this.sentHistoryCursor + msgPos;
		int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
		i = MathHelper.clamp_int(i, 0, j);

		if (i != this.sentHistoryCursor) {
			if (i == j) {
				this.sentHistoryCursor = j;
				this.inputField.setText(this.historyBuffer);
			} else {
				if (this.sentHistoryCursor == j) {
					this.historyBuffer = this.inputField.getText();
				}

				this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
				this.sentHistoryCursor = i;
			}
		}
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
		this.inputField.drawTextBox();
		IChatComponent ichatcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

		if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
			this.handleComponentHover(ichatcomponent, mouseX, mouseY);
		}

		if (dragging) {
			double x = mouseX - dragX;
			double y = mouseY - dragY;
			double[] pos = DragUtil.setPosition(x, y);
			draggingModule.setXYPosition(x, y);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void onAutocompleteResponse(String[] p_146406_1_) {
		if (this.waitingOnAutocomplete) {
			this.playerNamesFound = false;
			this.foundPlayerNames.clear();

			for (String s : p_146406_1_) {
				if (!s.isEmpty()) {
					this.foundPlayerNames.add(s);
				}
			}

			String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
			String s2 = StringUtils.getCommonPrefix(p_146406_1_);

			if (!s2.isEmpty() && !s1.equalsIgnoreCase(s2)) {
				this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
				this.inputField.writeText(s2);
			} else if (!this.foundPlayerNames.isEmpty()) {
				this.playerNamesFound = true;
				this.autocompletePlayerNames();
			}
		}
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in
	 * single-player
	 */
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public boolean isInside(int mouseX, int mouseY, double x, double y, double x2, double y2) {
		return (mouseX > x && mouseX < x2) && (mouseY > y && mouseY < y2);
	}
}
