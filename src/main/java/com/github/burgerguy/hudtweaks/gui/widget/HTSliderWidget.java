package com.github.burgerguy.hudtweaks.gui.widget;

import com.github.burgerguy.hudtweaks.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public abstract class HTSliderWidget extends SliderWidget implements ValueUpdatable {
	private static final int HANDLE_WIDTH = 7;

	private final double step;
	private final String translationKey;

	public HTSliderWidget(double value, double step, String translationKey) {
		// x, y, width are set by the container
		// height is static
		super(-1, -1, -1, 14, LiteralText.EMPTY, value);
		this.step = step;
		this.translationKey = translationKey;
		setAlpha(0.8F);
		updateMessage();
	}
	
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		TextRenderer textRenderer = minecraftClient.textRenderer;
		int x1 = x;
		int y1 = y;
		int x2 = x + width;
		int y2 = y + height;
		DrawableHelper.fill(matrixStack, x1,     y1,     x2,     y1 + 1, 0xFF000000);
		DrawableHelper.fill(matrixStack, x1,     y2,     x2,     y2 - 1, 0xFF000000);
		DrawableHelper.fill(matrixStack, x1,     y1 + 1, x1 + 1, y2 - 1, 0xFF000000);
		DrawableHelper.fill(matrixStack, x2,     y1 + 1, x2 - 1, y2 - 1, 0xFF000000);
		renderBg(matrixStack, minecraftClient, mouseX, mouseY);
		int j = active ? 0x00FFFFFF : 0x00A0A0A0;
		drawCenteredString(matrixStack, textRenderer, textRenderer.trimToWidth(getMessage(), width).getString(), x + width / 2, y + (height - 8) / 2, j | MathHelper.ceil(alpha * 255.0F) << 24);
		if (!active) {
			DrawableHelper.fill(matrixStack, x1, y1, x2, y2, 0x50303030);
		}
	}
	
	@Override
	protected void renderBg(MatrixStack matrixStack, MinecraftClient client, int mouseX, int mouseY) {
		int x1 = x + (int) (value * (width - HANDLE_WIDTH));
		int y1 = y;
		int x2 = x1 + HANDLE_WIDTH;
		int y2 = y1 + height;
		int color = isHovered() && active ? 0xFFFFFFFF : 0xFF000000;
		DrawableHelper.fill(matrixStack, x1,     y1,     x2,     y1 + 1, color);
		DrawableHelper.fill(matrixStack, x1,     y2,     x2,     y2 - 1, color);
		DrawableHelper.fill(matrixStack, x1,     y1 + 1, x1 + 1, y2 - 1, color);
		DrawableHelper.fill(matrixStack, x2,     y1 + 1, x2 - 1, y2 - 1, color);
		DrawableHelper.fill(matrixStack, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x7F9F9F9F);
	}
	
	@Override
	public void onClick(double mouseX, double mouseY) {
		setValueFromMouse(mouseX);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean bl = keyCode == 263;
		if (bl || keyCode == 262) {
			double f = bl ? -step : step;
			setValue(value + f / (width - HANDLE_WIDTH));
			return true;
		}
		
		return false;
	}
	
	protected void setValueFromMouse(double mouseX) {
		setValue((mouseX - (x + (HANDLE_WIDTH / 2.0D - 1.0D))) / (width - HANDLE_WIDTH));
	}
	
	public void setValue(double newValue) {
		double oldValue = value;
		value = MathHelper.clamp(newValue, 0.0D, 1.0D);
		if (oldValue != value) {
			applyValue();
		}
		
		updateMessage();
	}
	
	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		setValueFromMouse(mouseX);
	}

	@Override
	protected void updateMessage() {
		setMessage(new TranslatableText("hudtweaks.options.relative_pos.display", Util.RELATIVE_POS_FORMATTER.format(value)));
	}
}
