package com.github.standobyte.jojo.mixin.client.keybindui;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.utils.SettingsField;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyEntryMixin {
	@Shadow @Final private KeyMapping key;
	@Shadow @Final private Button changeButton;
	@Shadow private boolean hasCollision = false;

	protected Button jojo_ripples$holdToggleButton;
	protected List<Button> jojo_ripples$extraButtons;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void jojo_rippples$addButtons(KeyBindsList keybindsList, KeyMapping key, Component name, CallbackInfo ci) {
		String keyName = key.getName();
		boolean refreshAgain = false;
		
		SettingsField<Boolean> holdOrToggle = VanillaKeybinds.HOLD_OR_TOGGLE.get(keyName);
		if (holdOrToggle != null) {
			jojo_ripples$holdToggleButton = Button.builder(name, button -> {
				holdOrToggle.set(!holdOrToggle.get());
			})
			.bounds(0, 0, 100, 20)
			.build();
			
			if (jojo_ripples$extraButtons == null) jojo_ripples$extraButtons = new ArrayList<>();
			jojo_ripples$extraButtons.add(jojo_ripples$holdToggleButton);
			refreshAgain = true;
		}
		
		if (refreshAgain) refreshEntry();
	}
	
	@Shadow abstract void refreshEntry();

	@Inject(method = "refreshEntry", at = @At("TAIL"))
	public void jojo_ripples$modifyKeybindEntry(CallbackInfo ci) {
		String keyName = this.key.getName();
		if (VanillaKeybinds.ADD_DESC_TOOLTIP.contains(keyName)) {
			MutableComponent description = Component.translatable(keyName + ".desc");
			Tooltip tooltip = changeButton.getTooltip();
			if (tooltip != null) {
				description = description
						.append(CommonComponents.NEW_LINE)
						.append(CommonComponents.NEW_LINE)
						.append(tooltip.message.copy());
			}
			changeButton.setTooltip(Tooltip.create(description));
		}
	}
	
	@Inject(method = "render", at = @At("TAIL"))
	public void jojo_ripples$renderButtons(GuiGraphics guiGraphics, int index,
            int top, int left, int width, int height,
            int mouseX, int mouseY, boolean hovering, float partialTick, 
            CallbackInfo ci) {
		if (jojo_ripples$holdToggleButton != null) {
			String keyName = this.key.getName();
			SettingsField<Boolean> holdOrToggle = VanillaKeybinds.HOLD_OR_TOGGLE.get(keyName);
			if (holdOrToggle != null) {
				jojo_ripples$holdToggleButton.setMessage(Component.translatable(holdOrToggle.get() ? "options.key.toggle" : "options.key.hold"));
			}
			
			int x = left - jojo_ripples$holdToggleButton.getWidth() - 10;
			int y = top - 2;
			this.jojo_ripples$holdToggleButton.setPosition(x, y);
			this.jojo_ripples$holdToggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
	
	@Inject(method = "children", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$THIS_IS_SO_FUCKING_STUPID(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (jojo_ripples$extraButtons != null) {
			List<? extends GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(jojo_ripples$extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
	@Inject(method = "narratables", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$WHY_DID_THEY_NOT_JUST_MAKE_A_FUCKING_FIELD(CallbackInfoReturnable<List<? extends GuiEventListener>> ci) {
		if (jojo_ripples$extraButtons != null) {
			List<? extends GuiEventListener> mutable = new ArrayList<>(ci.getReturnValue());
			mutable.addAll(jojo_ripples$extraButtons);
			ci.setReturnValue(mutable);
		}
	}
	
}
