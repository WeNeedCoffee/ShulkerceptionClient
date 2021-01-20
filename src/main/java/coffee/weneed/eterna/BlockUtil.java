/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package coffee.weneed.eterna;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import coffee.weneed.utils.NetUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.json.JSONException;
import org.json.JSONObject;

public enum BlockUtil
{
	;




	public static String getName(Block block)
	{
		return Registry.BLOCK.getId(block).toString();
	}

	public static Block getBlockFromName(String name)
	{
		try
		{
			return Registry.BLOCK.get(new Identifier(name));

		}catch(InvalidIdentifierException e)
		{
			return Blocks.AIR;
		}
	}


	public static String getID(String name) {

		return getID(getItemOrBlock(name).asItem());

	}

	static File f = new File("./ids.json");

	static Map<Integer, String> blocks = new HashMap<>();
	static Map<Integer, String> items = new HashMap<>();
	static {
		try {
			call();
		} catch (JSONException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public static void call() throws JSONException, IOException {
		for (Item it : Registry.ITEM) {
			items.put(Registry.ITEM.getRawId(it), Registry.ITEM.getId(it).getPath());
		}

		for (Block b : Registry.BLOCK) {
			if (!items.containsValue(Registry.BLOCK.getId(b).getPath())) blocks.put(Registry.BLOCK.getRawId(b), Registry.BLOCK.getId(b).getPath());
		}


		if (f.exists())	f.delete();
		FileOutputStream fos = new FileOutputStream(f);
		JSONObject json = new JSONObject().put("blocks", blocks);
		fos.write(json.put("items", items).toString(4).getBytes());
		fos.flush();
		fos.close();
		f.setLastModified(System.currentTimeMillis());

	}

	public static String getID(Item item) {
		String name = item.getName().asString();
		for (Integer i : items.keySet()) {
			if (items.get(i).equalsIgnoreCase(name.replace(" ", "_"))) {
				return i + "i";
			}
		}

		for (Integer i : blocks.keySet()) {
			if (blocks.get(i).equalsIgnoreCase(name)) {
				return i + "b";
			}
		}
		return "0i";
	}
	public static Item getItemFromName(String name) throws InvalidIdentifierException {
			return Registry.ITEM.get(new Identifier(name));

	}
	public static ItemConvertible getItemOrBlock(String name) {
		try {
			if (name.matches("\\d+i")) {
				return getItemFromName(items.get(Integer.valueOf(name.replace("i", ""))).replace(" ", "_").toLowerCase());
			} else if (name.matches("\\d+b")) {
				return getBlockFromName(blocks.get(Integer.valueOf(name.replace("b", ""))).replace(" ", "_").toLowerCase());
			}
		} catch (InvalidIdentifierException e){
			//returns below
		} catch (Exception e) {

			return Items.DEAD_FIRE_CORAL;
		}
		Item i = getItemFromName(name);
		if (!i.equals(Items.AIR))
			return i;
		Block b = getBlockFromName(name);
		return b;
	}
}
