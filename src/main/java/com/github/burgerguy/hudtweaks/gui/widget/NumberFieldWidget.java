package com.github.burgerguy.hudtweaks.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class NumberFieldWidget extends HTTextFieldWidget implements ValueUpdatable {
	public NumberFieldWidget(Text text) {
		super(MinecraftClient.getInstance().textRenderer, -1, -1, -1, 14, text);
		setChangedListener(this::changedListener);
		updateValue();
	}
	
	@Override
	public void write(String string) {
		super.write(stripInvalidChars(string));
	}
	
	/**
	 * This filters out everything except for the characters 0123456789.-+
	 */
	private static boolean isValidChar(char chr) {
		return chr >= 48 && chr <= 57 || chr == 46 || chr == 45 || chr == 43;
	}
	
	private static String stripInvalidChars(String input) {
		StringBuilder sb = new StringBuilder();
		char[] charArray = input.toCharArray();
		
		for (char chr : charArray) {
			if (isValidChar(chr)) {
				sb.append(chr);
			}
		}
		
		return sb.toString();
	}

	protected abstract void applyValue(double d);

	protected void changedListener(String s) {
		if (s.equals("")) {
			applyValue(0);
		} else {
			try {
				double d = Double.parseDouble(s);
				applyValue(d);
			} catch(NumberFormatException ignored) {}
		}
	}
}
