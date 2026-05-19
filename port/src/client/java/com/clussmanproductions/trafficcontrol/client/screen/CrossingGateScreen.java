package com.clussmanproductions.trafficcontrol.client.screen;

import com.clussmanproductions.trafficcontrol.ModScreens;
import com.clussmanproductions.trafficcontrol.block.entity.CrossingGateBlockEntity;
import com.clussmanproductions.trafficcontrol.screen.CrossingGateScreenHandler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Config screen for a crossing gate: edits the arm length and the delay before
 * the gate lowers. On confirm the values are sent to the server, which clamps
 * and applies them to the gate's block entity.
 */
public class CrossingGateScreen extends HandledScreen<CrossingGateScreenHandler> {
	private TextFieldWidget lengthField;
	private TextFieldWidget delayField;

	public CrossingGateScreen(CrossingGateScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = 200;
		this.backgroundHeight = 110;
	}

	@Override
	protected void init() {
		super.init();
		this.playerInventoryTitleY = -1000;

		lengthField = new TextFieldWidget(textRenderer, x + 110, y + 28, 60, 18,
			Text.literal("length"));
		lengthField.setText(Integer.toString(handler.gateLength));
		lengthField.setTextPredicate(CrossingGateScreen::isValidNumber);
		addDrawableChild(lengthField);

		delayField = new TextFieldWidget(textRenderer, x + 110, y + 54, 60, 18,
			Text.literal("delay"));
		delayField.setText(Integer.toString(handler.closeDelayTicks));
		delayField.setTextPredicate(CrossingGateScreen::isValidNumber);
		addDrawableChild(delayField);

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> apply())
			.dimensions(x + 50, y + 80, 100, 20)
			.build());
	}

	private static boolean isValidNumber(String text) {
		return text.isEmpty() || text.matches("\\d{1,3}");
	}

	private void apply() {
		int length = parse(lengthField, CrossingGateBlockEntity.DEFAULT_GATE_LENGTH,
			CrossingGateBlockEntity.MIN_GATE_LENGTH, CrossingGateBlockEntity.MAX_GATE_LENGTH);
		int delay = parse(delayField, CrossingGateBlockEntity.DEFAULT_CLOSE_DELAY,
			CrossingGateBlockEntity.MIN_CLOSE_DELAY, CrossingGateBlockEntity.MAX_CLOSE_DELAY);

		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBlockPos(handler.pos);
		buf.writeVarInt(length);
		buf.writeVarInt(delay);
		ClientPlayNetworking.send(ModScreens.CROSSING_GATE_CONFIG, buf);
		close();
	}

	private static int parse(TextFieldWidget field, int fallback, int min, int max) {
		String text = field.getText();
		if (text.isEmpty()) {
			return fallback;
		}
		try {
			return MathHelper.clamp(Integer.parseInt(text), min, max);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		// Outer rectangle acts as a 1px border around the darker panel.
		context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF606060);
		context.fill(x + 1, y + 1, x + backgroundWidth - 1, y + backgroundHeight - 1, 0xF0202020);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(textRenderer, title, 8, 8, 0xFFFFFF, false);
		context.drawText(textRenderer, Text.literal("Gate length (blocks)"), 8, 33, 0xC0C0C0, false);
		context.drawText(textRenderer, Text.literal("Close delay (ticks)"), 8, 59, 0xC0C0C0, false);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
}
