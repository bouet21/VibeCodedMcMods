package com.chainores.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class GuideBookItem extends Item {

    public static Consumer<PlayerEntity> CLIENT_SCREEN_OPENER = null;

    public GuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient() && CLIENT_SCREEN_OPENER != null) {
            CLIENT_SCREEN_OPENER.accept(user);
        }
        return ActionResult.SUCCESS;
    }
}
