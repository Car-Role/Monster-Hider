package com.monsterhider;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("monsterhider")
public interface MonsterHiderConfig extends Config
{
	@ConfigItem(
		keyName = "toggleKeybind",
		name = "Toggle Keybind",
		description = "Keybind to toggle hiding monsters in the list. The hotkey does not consume the key press, so binds that produce a printable character (e.g. plain H) will also type into the chat input."
	)
	default Keybind toggleKeybind()
	{
		return new Keybind(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK);
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
}
