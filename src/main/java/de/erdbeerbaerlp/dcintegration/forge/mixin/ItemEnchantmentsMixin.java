package de.erdbeerbaerlp.dcintegration.forge.mixin;

import de.erdbeerbaerlp.dcintegration.forge.util.accessors.ShowInTooltipAccessor;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin implements ShowInTooltipAccessor {
    @Shadow @Final boolean showInTooltip;


    public boolean discordIntegrationFabric$showsInTooltip() {
        return showInTooltip;
    }
}
