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

	static Map<Integer, String> dupes = new HashMap<>();
	static Map<Integer, String> blocks = new HashMap<>();
	static Map<Integer, String> items = new HashMap<>();
	static {
		/*try {
			if (!f.exists()) {
				call();
			}
		} catch (JSONException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}*/
		JSONObject json;
		try {
			InputStream in = BlockUtil.class.getResourceAsStream("/assets/ids.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String s = reader.lines().collect(Collectors.joining());
			json = new JSONObject(s);
			System.out.println(json);
			for (String e : json.getJSONObject("items").keySet()) {
				items.put(Integer.valueOf(e), json.optJSONObject("items").getString(e));
			}
			int d = 0;
			int b = 0;
			for (String e : json.getJSONObject("blocks").keySet()) {
				if (items.containsValue(json.optJSONObject("blocks").getString(e))){
					dupes.put(d++, json.optJSONObject("blocks").getString(e));
					continue;
				}
				blocks.put(b++, json.optJSONObject("blocks").getString(e));
			}



			if (f.exists())	f.delete();
			FileOutputStream fos = new FileOutputStream(f);
			JSONObject js = new JSONObject().put("blocks", blocks).put("items", items).put("dupes", dupes);
			fos.write(js.toString().getBytes());
			fos.flush();
			fos.close();
			f.setLastModified(System.currentTimeMillis());
		} catch (JSONException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void call() throws JSONException, IOException {
		Map<Integer, String> bids = new HashMap<>();
		int i = 0;
		for (Block b : Registry.BLOCK) {
			bids.put(i, Registry.BLOCK.getId(b).getPath());
			i++;
		}

		Map<Integer, String> iids = new HashMap<>();
		i = 0;
		for (Item it : Registry.ITEM) {
			iids.put(i, Registry.ITEM.getId(it).getPath());
			i++;
		}
		if (f.exists())	f.delete();
		FileOutputStream fos = new FileOutputStream(f);
		JSONObject json = new JSONObject().put("blocks", bids);
		fos.write(json.put("items", iids).toString().getBytes());
		fos.flush();
		fos.close();
		f.setLastModified(System.currentTimeMillis());

	}

	public static String getID(Item item) {
		String name = item.getName().asString();
		for (int i : items.keySet()) {
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
	public static Item getItemFromName(String name) {
		try {
			return Registry.ITEM.get(new Identifier(name));

		} catch (InvalidIdentifierException e) {
			return Items.AIR;
		}
	}
	public static ItemConvertible getItemOrBlock(String name) {
		if (name.matches("\\d+i")) {
			return getItemFromName(items.get(name.replace("i", "")).replace(" ", "_").toLowerCase());
		} else if (name.matches("\\d+b")) {
			return getBlockFromName(blocks.get(name.replace("b", "")).replace(" ", "_").toLowerCase());
		}
		Item i = getItemFromName(name);
		if (!i.equals(Items.AIR))
			return i;
		Block b = getBlockFromName(name);
		return b;
	}
	public static ArrayList<BlockPos> getAllInBox(BlockPos from, BlockPos to)
	{
		ArrayList<BlockPos> blocks = new ArrayList<>();

		BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
				Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
		BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
				Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

		for(int x = min.getX(); x <= max.getX(); x++)
			for(int y = min.getY(); y <= max.getY(); y++)
				for(int z = min.getZ(); z <= max.getZ(); z++)
					blocks.add(new BlockPos(x, y, z));

		return blocks;
	}
}
