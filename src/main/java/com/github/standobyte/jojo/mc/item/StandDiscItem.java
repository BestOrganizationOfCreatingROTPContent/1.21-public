package com.github.standobyte.jojo.mc.item;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mc.item.component.StandWrittenOnDisc;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StandDiscItem extends Item {

	public StandDiscItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		StandWrittenOnDisc discStand = item.get(ModItemDataComponents.DISC_STAND.get());
		if (discStand == null || !discStand.isValid()) return;
		
		Component standName = discStand.getStandName();
		if (standName != null) {
			tooltip.add(standName);
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			ItemStack discItem = player.getItemInHand(hand);
			StandWrittenOnDisc discStand = discItem.get(ModItemDataComponents.DISC_STAND.get());
			if (discStand == null || !discStand.isValid()) return InteractionResult.FAIL;
			
			PowerClass.STAND.attachPower(player);
			StandPower stand = PowerClass.STAND.get(player);
			if (stand != null) {
				stand.setStandInstance(Optional.of(discStand.copyStandInstance()));
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.CONSUME;
	}

}
