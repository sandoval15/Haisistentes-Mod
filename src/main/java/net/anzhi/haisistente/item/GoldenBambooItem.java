package  net.anzhi.haisistente.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GoldenBambooItem extends Item {

    public GoldenBambooItem() {
        super(new Item.Properties()
                .stacksTo(64)
                .rarity(Rarity.RARE)
        );
    }

	@Override
	public Component getName(ItemStack stack) {
    	return Component.literal("Golden Bamboo");
	}
}
