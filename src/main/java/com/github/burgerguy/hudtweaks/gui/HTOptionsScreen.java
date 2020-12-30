package com.github.burgerguy.hudtweaks.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import com.github.burgerguy.hudtweaks.config.ConfigHelper;
import com.github.burgerguy.hudtweaks.gui.HudElement.HudElementWidget;
import com.github.burgerguy.hudtweaks.gui.widget.ArrowButtonWidget;
import com.github.burgerguy.hudtweaks.gui.widget.ElementLabelWidget;
import com.github.burgerguy.hudtweaks.gui.widget.SidebarWidget;
import com.github.burgerguy.hudtweaks.util.Util;

import io.netty.util.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class HTOptionsScreen extends Screen {
	private static final int SIDEBAR_WIDTH = 116;
	private static final int SIDEBAR_COLOR = 0x60424242;
	
	private static boolean isOpen = false;
	
	private final Screen prevScreen;
	private final SidebarWidget sidebar;
	private ElementLabelWidget elementLabel;
	
	private HudElementWidget focusedHudElement;
	
	public HTOptionsScreen(Screen prevScreen) {
		super(new TranslatableText("hudtweaks.options"));
		this.prevScreen = prevScreen;
		
		sidebar = new SidebarWidget(this, SIDEBAR_WIDTH, SIDEBAR_COLOR);
	}
	
	// overridden to stop defocusing
	public void init(MinecraftClient client, int width, int height) {
		this.client = client;
		this.itemRenderer = client.getItemRenderer();
		this.textRenderer = client.textRenderer;
		this.width = width;
		this.height = height;
		this.buttons.clear();
		this.children.clear();
		this.init();
	}
	
	@Override
	protected void init() {
		super.init();
		
		// normal drawables are cleared 
		sidebar.clearGlobalDrawables();
		
		isOpen = true;

		for (HudElement element : HudContainer.getElements()) {
			Element widget = element.createWidget(this);
			if (widget != null) {
				children.add(widget);
			}
		}
		
		// This makes sure that the smallest elements get selected first if there are multiple on top of eachother.
		// We also want normal elements to be the first to be selected.
		children.sort((e1, e2) -> {
			boolean isHudElement1 = e1 instanceof HudElementWidget;
			boolean isHudElement2 = e2 instanceof HudElementWidget;
			if (isHudElement1 && !isHudElement2) {
				return 1;
			} else if (!isHudElement1 && isHudElement2 || (!isHudElement1 || !isHudElement2)) {
				return -1;
			} else {
				HudElement he1 = ((HudElementWidget) e1).getParent();
				HudElement he2 = ((HudElementWidget) e2).getParent();
				return Double.compare(
						he1.getWidth(client) * he1.getHeight(client),
						he2.getWidth(client) * he2.getHeight(client)
						);
			}
		});
		
		// this is added to the start of the list so it is selected before anything else
		children.add(0, sidebar);
		
		elementLabel = new ElementLabelWidget(sidebar.width / 2, height - 17, sidebar.width - 42);
		ArrowButtonWidget leftArrow = new ArrowButtonWidget(5, height - 21, true, new TranslatableText("hudtweaks.options.previous_element.name"), b -> {
			changeHudElementFocus(false);
		});
		ArrowButtonWidget rightArrow = new ArrowButtonWidget(sidebar.width - 21, height - 21, false, new TranslatableText("hudtweaks.options.next_element.name"), b -> {
			changeHudElementFocus(true);
		});
		sidebar.addGlobalDrawable(elementLabel);
		sidebar.addGlobalDrawable(leftArrow);
		sidebar.addGlobalDrawable(rightArrow);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrixStack);
		
		// reverse order
		for (int i = children.size() - 1; i >= 0; i--) {
			Element element = children.get(i);
			if (element instanceof Drawable && !(element instanceof AbstractButtonWidget)) {
				((Drawable) element).render(matrixStack, mouseX, mouseY, delta);
			}
		}
		
		super.render(matrixStack, mouseX, mouseY, delta);
	}
	
	@Override
	public void renderBackground(MatrixStack matrixStack, int vOffset) {
		if (client.world == null) {
			renderBackgroundTexture(vOffset);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return Util.patchedMouseClicked(mouseX, mouseY, button, this);
	}
	
	@Override
	public void onClose() {
		ConfigHelper.trySaveConfig();
		if (client.world == null) {
			client.openScreen(prevScreen);
		} else {
			client.openScreen(null);
		}
		isOpen = false;
	}
	
	@Override
	public void setFocused(Element focused) {
		if (focused instanceof HudElementWidget && !focused.equals(focusedHudElement)) {
			focusedHudElement = (HudElementWidget) focused;
			sidebar.clearDrawables();
			((HudElementWidget) focused).getParent().fillSidebar(sidebar);
			elementLabel.setHudElement(focusedHudElement.getParent());
		}
		
		if (focused == null) {
			focusedHudElement = null;
			sidebar.clearDrawables();
			elementLabel.setHudElement(null);
		}
		
		super.setFocused(focused);
	}
	
	private final List<HudElementWidget> tempHudElements = new ArrayList<>();
	
	private void changeHudElementFocus(boolean lookForwards) {		
		tempHudElements.clear();
		for (Element element : this.children()) {
			if (element instanceof HudElementWidget) {
				tempHudElements.add((HudElementWidget) element);
			}
		}
		
		int newIdx = 0;
		if (focusedHudElement != null) {
			int curIdx;
			if (focusedHudElement != null && (curIdx = tempHudElements.indexOf(focusedHudElement)) >= 0) {
				newIdx = curIdx + (lookForwards ? 1 : 0);
			}
		} else {
			if (lookForwards) {
				newIdx = 0;
			} else {
				newIdx = tempHudElements.size();
			}
		}
		
		ListIterator<HudElementWidget> listIterator = tempHudElements.listIterator(newIdx);
		BooleanSupplier hasNearbySupplier = lookForwards ? listIterator::hasNext : listIterator::hasPrevious;
		Supplier<HudElementWidget> elementSupplier = lookForwards ? listIterator::next : listIterator::previous;
		
		HudElementWidget currentElement = null;
		do {
			try {
				if (!hasNearbySupplier.get()) {
					return; // keep focus if none nearby
				}
			} catch (Exception ignored) {
				return;
			}
			
			currentElement = elementSupplier.get();
		} while (!currentElement.changeFocus(lookForwards));
		
		this.setFocused(currentElement);
	}
	
	@Override
	public void tick() {
		for (Element element : children()) {
			if (element instanceof TickableElement) {
				((TickableElement) element).tick();
			}
			if (element instanceof TextFieldWidget) {
				((TextFieldWidget) element).tick();
			}
		}
	}
	
	public boolean isHudElementFocused(HudElementWidget element) {// TODO: allow changing focus of elements with arrows, make tab only change focus for sidebar
		if (element == null || focusedHudElement == null) {
			return false;
		}
		return focusedHudElement.equals(element);
	}
	
	public void updateSidebarValues() {
		sidebar.updateValues();
	}
	
	public static boolean isOpen() {
		return isOpen;
	}
	
}
