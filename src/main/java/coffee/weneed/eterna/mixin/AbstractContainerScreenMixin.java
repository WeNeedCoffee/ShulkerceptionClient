package coffee.weneed.eterna.mixin;

import java.util.*;

import coffee.weneed.eterna.BlockUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
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
public abstract class AbstractContainerScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {
	public AbstractContainerScreenMixin(T screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
	}
	private static Map<String, Integer> lastChest = new HashMap<>();

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("TAIL"))
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {

		try {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.textRenderer.draw(matrices, "-List of Items-", super.width - 200, 30, 0x1ffffff);
			drawListItems();
			List<Map.Entry<String, Integer>> stuff = new ArrayList<>();
			for (Map.Entry<String, Integer> e : lastChest.entrySet()) {
				stuff.add(e);
			}
			Collections.sort(stuff, Comparator.comparing(Map.Entry<String, Integer>::getValue).thenComparing(Map.Entry<String, Integer>::getKey));
			Collections.reverse(stuff);
			int i = 0;
			for (Map.Entry<String, Integer> e : stuff) {
				ItemStack is = BlockUtil.getItemOrBlock(e.getKey().toLowerCase().replace(" ", "_")).asItem().getDefaultStack();
				if (is.getItem().getName().asString().toLowerCase() != "air") {
					i++;
					this.itemRenderer.renderInGui(is, super.width - 200, 40 + i * 15);
					this.textRenderer.draw(matrices, e.getValue() + "x    " + is.getItem().getName().asString(), super.width - 180, 40 + i * 15, 0xfffff1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static ArrayList<ItemStack> listInvItems(List<Slot> i)  {
		ArrayList<ItemStack> nestDeal = new ArrayList<ItemStack>();
		for (Slot slot : i) {
			if (!i.isEmpty()) nestDeal.addAll(test(slot.getStack()));
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
		return nestDeal;
	}


	private static Map<String, Integer> listItemsCount(List<ItemStack> istacks) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		try {
			for (ItemStack ist : istacks) {
				Integer c = map.get(ist.getItem().getDefaultStack().getName().getString());
				c = (c == null ? 0 : c);
				if (c != null) map.remove(ist.getItem().getDefaultStack().getName().getString());
				map.put(ist.getItem().getDefaultStack().getName().getString(), c + ist.getCount());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	private static long lastChestDraw = 0;

	private void drawListItems() {
		if (System.currentTimeMillis() - lastChestDraw > 2000) {
			lastChest = listItemsCount(listInvItems(this.handler.slots));
			lastChestDraw = System.currentTimeMillis();

			//System.out.println(lastSortedChest);
		}
	}

}