package com.github.standobyte.jojo.client.ui.powerhud.tooltip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class MultiLineScreenTooltip extends Tooltip {
	protected static final int MAX_WIDTH = 170;
	protected final List<Component> message;
	@Nullable
	protected List<FormattedCharSequence> cachedTooltip;
	@Nullable
	protected Language splitWithLanguage;

	public MultiLineScreenTooltip(Component firstLine, Component... message) {
		super(firstLine, firstLine);
		this.message = new ArrayList<>();
		this.message.add(firstLine);
		Collections.addAll(this.message, message);
	}

	@Override
	public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
		Language language = Language.getInstance();
		if (this.cachedTooltip == null || language != this.splitWithLanguage) {
			this.cachedTooltip = this.message.stream().map(line -> minecraft.font.split(line, MAX_WIDTH))
					.flatMap(Collection::stream)
					.toList();
			this.splitWithLanguage = language;
		}

		return this.cachedTooltip;
	}

}
