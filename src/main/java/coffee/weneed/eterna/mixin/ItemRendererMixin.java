package coffee.weneed.eterna.mixin;


import coffee.weneed.eterna.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.render.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.resource.SynchronousResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements SynchronousResourceReloadListener {
	@Shadow
	@Final
	ItemModels models;

	@Inject(method =  { "renderItem" }, at = {@At("HEAD")}, cancellable = true)
	private void renderItemMixin(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (stack.getItem().getDefaultStack().getTranslationKey().endsWith("shulker_box")) {
			String name = stack.getName().asString();
			ItemStack st;
			String e = name.replace(" ", "_").split(":")[0].replace("[^a-zA-Z0-9/-]+", "").toLowerCase();

			Item t = BlockUtil.getItemOrBlock(e).asItem();
			if (!t.equals(Items.AIR)) {
				st = t.getDefaultStack();
				st.addEnchantment(Enchantments.VANISHING_CURSE, 1);
				st.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
			} else {
				st = stack;
			}
			if (!st.equals(stack)) {
				stack = st;
				String it = "minecraft:" + Registry.ITEM.getId(st.getItem()).getPath().toLowerCase() + "#inventory";
				model = this.models.getModelManager().getModel(new ModelIdentifier(it));

			}
			if (!stack.isEmpty()) {
				matrices.push();
				boolean bl = renderMode == ModelTransformation.Mode.GUI || renderMode == ModelTransformation.Mode.GROUND || renderMode == ModelTransformation.Mode.FIXED;
				if (stack.getItem() == Items.TRIDENT && bl) {
					model = this.models.getModelManager().getModel(new ModelIdentifier("minecraft:trident#inventory"));
				}

				model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
				matrices.translate(-0.5D, -0.5D, -0.5D);
				//boolean bl1 = model.isSideLit();
				/*if (bl1) {
					DiffuseLighting.disableGuiDepthLighting();
				}*/
				if (model.isBuiltin() || stack.getItem() == Items.TRIDENT && !bl) {
					BuiltinModelItemRenderer.INSTANCE.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
				} else {
					boolean bl3;
					if (renderMode != ModelTransformation.Mode.GUI && !renderMode.isFirstPerson() && stack.getItem() instanceof BlockItem) {
						Block block = ((BlockItem)stack.getItem()).getBlock();
						bl3 = !(block instanceof TransparentBlock) && !(block instanceof StainedGlassPaneBlock);
					} else {
						bl3 = true;
					}

					RenderLayer renderLayer = RenderLayers.getItemLayer(stack, bl3);
					VertexConsumer vertexConsumer4;
					if (stack.getItem() == Items.COMPASS && stack.hasGlint()) {
						matrices.push();
						MatrixStack.Entry entry = matrices.peek();
						if (renderMode == ModelTransformation.Mode.GUI) {
							entry.getModel().multiply(0.5F);
						} else if (renderMode.isFirstPerson()) {
							entry.getModel().multiply(0.75F);
						}

						if (bl3) {
							vertexConsumer4 = ItemRenderer.getDirectCompassGlintConsumer(vertexConsumers, renderLayer, entry);
						} else {
							vertexConsumer4 = ItemRenderer.getCompassGlintConsumer(vertexConsumers, renderLayer, entry);
						}

						matrices.pop();
					} else if (bl3) {
						vertexConsumer4 = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
					} else {
						vertexConsumer4 = ItemRenderer.getItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
					}

					this.renderBakedItemModel(model, stack, 15728880, overlay, matrices, vertexConsumer4);
				}
				/*if (bl1) {
					DiffuseLighting.enableGuiDepthLighting();
				}*/

				matrices.pop();
			}
			ci.cancel();
		}
	}

	@Shadow
	void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {

	}


}
