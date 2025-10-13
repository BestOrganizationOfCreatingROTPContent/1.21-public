package com.github.standobyte.jojo.mechanics.entitycontrol.tmp;

import com.github.standobyte.jojo.mechanics.entitycontrol.ServerEntityController;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ControllerDebugItem extends Item {

	public ControllerDebugItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!level.isClientSide()) {
        	ActionTarget target = HitResultUtil.clipEntityLook(player, e -> !e.isSpectator() && e.isPickable(), 0);
        	Entity entity = target.getEntity();
        	if (entity instanceof Mob) {
        		ServerEntityController.setServerControlTarget(player, entity, "mob");
        	}
        }
		return InteractionResultHolder.consume(item);
	}

	
//	@Override
//	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
//		tooltip.add(Component.literal("this is probably really broken currently").withStyle(ChatFormatting.GRAY));
//	}

}