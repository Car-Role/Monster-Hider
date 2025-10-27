package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Monster Hider",
	description = "Hide specific monsters by name or ID using a keybind toggle",
	tags = {"npc", "hide", "monster", "entity"}
)
public class MonsterHiderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private MonsterHiderConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientThread clientThread;

	private boolean hideEnabled = false;
	private final Set<String> npcNamesToHide = new HashSet<>();
	private final Set<Integer> npcIdsToHide = new HashSet<>();

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleKeybind())
	{
		@Override
		public void hotkeyPressed()
		{
			toggleHiding();
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(hotkeyListener);
		hideEnabled = false; // Default to showing all monsters on startup
		updateNpcLists();
		log.debug("Monster Hider started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(hotkeyListener);
		hideEnabled = false;
		showAllNpcs();
		npcNamesToHide.clear();
		npcIdsToHide.clear();
		log.debug("Monster Hider stopped!");
	}

	private void toggleHiding()
	{
		hideEnabled = !hideEnabled;
		updateNpcLists();

		clientThread.invoke(() ->
		{
			if (hideEnabled)
			{
				hideMatchingNpcs();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Monster hiding enabled", null);
			}
			else
			{
				showAllNpcs();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Monster hiding disabled", null);
			}
		});
	}

	private void updateNpcLists()
	{
		npcNamesToHide.clear();
		npcIdsToHide.clear();

		// Parse NPC names
		String namesConfig = config.npcNames();
		if (namesConfig != null && !namesConfig.trim().isEmpty())
		{
			for (String name : namesConfig.split(","))
			{
				String trimmed = name.trim();
				if (!trimmed.isEmpty())
				{
					npcNamesToHide.add(trimmed.toLowerCase());
				}
			}
		}

		// Parse NPC IDs
		String idsConfig = config.npcIds();
		if (idsConfig != null && !idsConfig.trim().isEmpty())
		{
			for (String id : idsConfig.split(","))
			{
				try
				{
					int npcId = Integer.parseInt(id.trim());
					npcIdsToHide.add(npcId);
				}
				catch (NumberFormatException e)
				{
					log.warn("Invalid NPC ID: {}", id);
				}
			}
		}
	}

	private void hideMatchingNpcs()
	{
		if (client.getNpcs() == null)
		{
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc != null && shouldHideNpc(npc))
			{
				npc.setDead(true);
			}
		}
	}

	private void showAllNpcs()
	{
		if (client.getNpcs() == null)
		{
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc != null && npc.isDead())
			{
				npc.setDead(false);
			}
		}
	}

	private boolean shouldHideNpc(NPC npc)
	{
		if (npc == null || npc.getName() == null)
		{
			return false;
		}

		// Check if NPC ID is in the hide list
		if (npcIdsToHide.contains(npc.getId()))
		{
			return true;
		}

		// Check if NPC name is in the hide list
		String npcName = npc.getName().toLowerCase();
		for (String nameToHide : npcNamesToHide)
		{
			if (npcName.contains(nameToHide))
			{
				return true;
			}
		}

		return false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (hideEnabled)
		{
			NPC npc = npcSpawned.getNpc();
			if (shouldHideNpc(npc))
			{
				npc.setDead(true);
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		// Clean up if needed
	}

	@Provides
	MonsterHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MonsterHiderConfig.class);
	}
}
