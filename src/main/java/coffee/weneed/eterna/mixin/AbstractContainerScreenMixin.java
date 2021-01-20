package coffee.weneed.eterna.mixin;

import java.util.*;

import coffee.weneed.eterna.BlockUtil;
import coffee.weneed.utils.MathUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;

@Mixin(GenericContainerScreen.class)
public abstract class AbstractContainerScreenMixin<GenericContainerScreenHandler> extends HandledScreen<net.minecraft.screen.GenericContainerScreenHandler> {
	public AbstractContainerScreenMixin(net.minecraft.screen.GenericContainerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
	}
	private static Map<String, Integer> lastChest = new HashMap<>();

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("TAIL"))
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {

		try {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.textRenderer.draw(matrices, "-List of Items-", 100, 30, 0x1ffffff);
			drawListItems();
			List<Map.Entry<String, Integer>> stuff = new ArrayList<>();
			for (Map.Entry<String, Integer> e : lastChest.entrySet()) {
				stuff.add(e);
			}
			Collections.sort(stuff, Comparator.comparing(Map.Entry<String, Integer>::getValue).thenComparing(Map.Entry<String, Integer>::getKey));
			Collections.reverse(stuff);
			int i = 0;
			int z = 0;
			for (Map.Entry<String, Integer> e : stuff) {
				ItemStack is = BlockUtil.getItemOrBlock(e.getKey().toLowerCase().replace(" ", "_")).asItem().getDefaultStack();
				//is.setCount(e.getValue());
				if (!is.getItem().getName().getString().equalsIgnoreCase("air") && e.getValue() > 0) {
					z++;
					if (z > 15) {
						z = 1;
						i++;
					}
					if (i > 30){
						break;
					}
					this.itemRenderer.renderInGui(is, (z * 21), 40 + i * 20);
					String count = MathUtil.shortString(e.getValue(), 4);
					renderGuiItemOverlay(this.textRenderer, is, (z * 21), 43 + i * 20, count);

					//this.textRenderer.draw(matrices, e.getValue() + "x " + is.getItem().getName().getString(), super.width - 180, 40 + i * 15, 0xfffff1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
		if (!stack.isEmpty()) {
			MatrixStack matrixStack = new MatrixStack();
			if (stack.getCount() != 1 || countLabel != null) {
				String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
				matrixStack.translate(0.0D, 0.0D, (double) (this.itemRenderer.zOffset + 200.0F));
				VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
				renderer.draw((String) string, (float) (x + 8 - (renderer.getWidth(string) / 2)), (float) (y + 6 + 3), 16777215, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
				immediate.draw();
			}
		}
	}

	private ArrayList<ItemStack> listInvItems(Inventory inv)  {
		ArrayList<ItemStack> nestDeal = new ArrayList<ItemStack>();
		for (int i = 0; i < inv.size(); i++) {
			if (!inv.getStack(i).isEmpty()) nestDeal.addAll(test(inv.getStack(i)));
		}
		return nestDeal;
	}

	private static ArrayList<ItemStack> test(ItemStack ist) {
		ArrayList<ItemStack> nestDeal = new ArrayList<>();
		if (ist.getTag() != null && !ist.getTag().getCompound("BlockEntityTag").getList("Items", 10).isEmpty()) {
			if (ist.getCount() > 1) {
				List<ItemStack> its = shulkerTraversal(ist);
				for (int e = 0; e < ist.getCount(); e++) {
					nestDeal.addAll(its);
				}
			} else {
				nestDeal.addAll(shulkerTraversal(ist));
			}
		} else {
			nestDeal.add(ist);
		}
		return nestDeal;
	}

	private static ArrayList<ItemStack> shulkerTraversal(ItemStack shulker) {
		ArrayList<ItemStack> nestDeal = new ArrayList<ItemStack>();
		ListTag lt = shulker.getTag().getCompound("BlockEntityTag").getList("Items", 10);
		for (int j = 0; j < lt.size(); j++) {
			nestDeal.addAll(test(ItemStack.fromTag(lt.getCompound(j))));
		}
		nestDeal.add(shulker);
		return nestDeal;
	}


	private static Map<String, Integer> listItemsCount(List<ItemStack> istacks) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		try {
			for (ItemStack ist : istacks) {
				String id = Registry.ITEM.getId(ist.getItem()).getPath();
				Integer c = map.get(id);
				c = (c == null ? 0 : c);
				if (c != null) map.remove(id);
				map.put(id, c + ist.getCount());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	private static long lastChestDraw = 0;

	private void drawListItems() {
		if (System.currentTimeMillis() - lastChestDraw > 500) {
			lastChest = listItemsCount(listInvItems(this.handler.getInventory()));
			lastChestDraw = System.currentTimeMillis();

			//System.out.println(lastSortedChest);
		}
	}

}