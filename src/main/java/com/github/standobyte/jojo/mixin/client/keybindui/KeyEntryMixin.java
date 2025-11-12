package com.github.standobyte.jojo.mixin.client.keybindui;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.input.VanillaKeybinds;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyEntryMixin {
	@Shadow @Final private KeyMapping key;
	@Shadow @Final private Button changeButton;
	@Shadow private boolean hasCollision = false;

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
}
