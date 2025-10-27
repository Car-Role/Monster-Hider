package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;

@ConfigGroup("monsterhider")
public interface MonsterHiderConfig extends Config
{
	@ConfigItem(
		keyName = "toggleKeybind",
		name = "Toggle Keybind",
		description = "Keybind to toggle hiding monsters in the lists"
	)
	default Keybind toggleKeybind()
	{
		return new ModifierlessKeybind(java.awt.event.KeyEvent.VK_H, 0);
	}

	@ConfigItem(
		keyName = "npcNames",
		name = "NPC Names",
		description = "Comma-separated list of NPC names to hide (e.g., 'Goblin, Cow, Guard')"
	)
	default String npcNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "npcIds",
		name = "NPC IDs",
		description = "Comma-separated list of NPC IDs to hide (e.g., '100, 101, 102')"
	)
	default String npcIds()
	{
		return "";
	}
}
