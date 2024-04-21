package de.erdbeerbaerlp.dcintegration.forge.api;

import de.erdbeerbaerlp.dcintegration.common.api.DiscordEventHandler;
import net.neoforged.neoforge.event.ServerChatEvent;

public abstract class ForgeDiscordEventHandler extends DiscordEventHandler {
    /**
     * Gets called before a minecraft message gets sent to discord
     *
     * @return true to cancel default code execution
     */
    public abstract boolean onMcChatMessage(ServerChatEvent event);
}
