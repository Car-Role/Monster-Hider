package com.monsterhider;

import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Monster Hider",
	description = "Hide specific monsters by name using a keybind toggle",
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

	@Inject
	private Hooks hooks;

	private volatile boolean hideEnabled = false;
	private final Set<String> npcNamesToHide = new HashSet<>();

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private boolean hotkeyPressed = false;
	private final KeyListener keyListener = new KeyListener()
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!config.toggleKeybind().matches(e))
			{
				return;
			}

			// Don't fire while the user is focused on a text input widget
			// (bank search, world-map search, dialog name input, report abuse, etc.).
			if (client.getFocusedInputFieldWidget() != null)
			{
				return;
			}

			if (hotkeyPressed)
			{
				return;
			}

			hotkeyPressed = true;
			toggleHiding();
			// Intentionally do NOT consume the event so the character still
			// reaches the game (e.g. lands in the chat input as normal).
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			if (config.toggleKeybind().matches(e))
			{
				hotkeyPressed = false;
			}
		}

		@Override
		public void focusLost()
		{
			hotkeyPressed = false;
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(keyListener);
		hooks.registerRenderableDrawListener(drawListener);
		hideEnabled = false; // Default to showing all monsters on startup
		hotkeyPressed = false;
		updateNpcList();
		log.debug("Monster Hider started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		keyManager.unregisterKeyListener(keyListener);
		hideEnabled = false;
		hotkeyPressed = false;
		npcNamesToHide.clear();
		log.debug("Monster Hider stopped!");
	}

	private void toggleHiding()
	{
		hideEnabled = !hideEnabled;
		updateNpcList();

		clientThread.invoke(() ->
		{
			String message = hideEnabled ? "Monster hiding enabled" : "Monster hiding disabled";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		});
	}

	private void updateNpcList()
	{
		npcNamesToHide.clear();

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
	}

	private boolean shouldHideNpc(NPC npc)
	{
		if (npc == null || npc.getName() == null)
		{
			return false;
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

	// Render-callback hook: return false to skip drawing the renderable.
	// Using Hooks.RenderableDrawListener instead of npc.setDead(true) ensures
	// hiding works reliably for bosses and for NPCs going through state changes
	// (freezing, phase transitions, shadow-realm swaps, etc.).
	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (!hideEnabled)
		{
			return true;
		}

		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;
			if (shouldHideNpc(npc))
			{
				return false;
			}
		}

		return true;
	}

	@Provides
	MonsterHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MonsterHiderConfig.class);
	}
}
