package coffee.weneed.eterna.mixin;

import com.google.gson.JsonParseException;
import io.netty.util.internal.StringUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin  {
	@Redirect(method = "Lnet/minecraft/item/ItemStack;toHoverableText()Lnet/minecraft/text/Text;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;",
					ordinal = 0)
			)
	public Text getName2(ItemStack item) {
		return getText();
	}

	@Redirect(
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;",
					ordinal = 0),
			method = "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;")
	public Text getName1(ItemStack item) {
		return getText();
	}
	private Text getText() {
		CompoundTag compoundTag = getSubTag("display");
		if (compoundTag != null && compoundTag.contains("Name", 8)) {
			try {
				Text text = Text.Serializer.fromJson(compoundTag.getString("Name"));
				if (text != null) {
					String s = StringUtil.substringAfter(text.asString(), ":".charAt(0));
					if (s != null) text = Text.of(s);
					return text;
				}

				compoundTag.remove("Name");
			} catch (JsonParseException var3) {
				compoundTag.remove("Name");
			}
		}
		return getItem().getName();
	}

	@Shadow
	private CompoundTag getSubTag(String display) {
		return null;
	}

	@Shadow
	private Item getItem() {
		return null;
	}

}
