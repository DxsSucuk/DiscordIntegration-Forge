package de.erdbeerbaerlp.dcintegration.forge.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.compat.FloodgateUtils;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.MinecraftPermission;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;
import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.LOGGER;

@Mixin(PlayerList.class)
public class MixinPlayerLogin {
    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true, require = 1)
    private void canLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Component> cir) {
        LOGGER.info("Can login?");
        if (Configuration.instance().linking.whitelistMode && ServerLifecycleHooks.getCurrentServer().usesAuthentication()) {
            LOGGER.info("Whitelist mode enabled");
            LinkManager.checkGlobalAPI(profile.getId());
            final dcshadow.net.kyori.adventure.text.Component eventKick = INSTANCE.callEventO((e) -> e.onPlayerJoin(profile.getId()));
            if(eventKick != null){
                final String jsonComp = GsonComponentSerializer.gson().serialize(eventKick).replace("\\\\n", "\n");
                try {
                    final Component comp = ComponentArgument.textComponent().parse(new StringReader(jsonComp));
                    cir.setReturnValue(comp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(DiscordIntegration.INSTANCE.getServerInterface().playerHasPermissions(profile.getId(), MinecraftPermission.BYPASS_WHITELIST,MinecraftPermission.ADMIN)){
                LOGGER.info("Player has permissions");
                return;
            }
            try {
                if (!LinkManager.isPlayerLinked(profile.getId())) {
                    LOGGER.info("Player is not linked, kicking");
                    cir.setReturnValue(Component.literal(Localization.instance().linking.notWhitelistedCode.replace("%code%",""+(FloodgateUtils.isBedrockPlayer(profile.getId()) ? LinkManager.genBedrockLinkNumber(profile.getId()) :LinkManager.genLinkNumber(profile.getId())))));
                }else if(!DiscordIntegration.INSTANCE.canPlayerJoin(profile.getId())){
                    LOGGER.info("Player cannot join");
                    cir.setReturnValue(Component.literal(Localization.instance().linking.notWhitelistedRole));
                }
                LOGGER.info("Hmm?!?");
            } catch (IllegalStateException e) {
                cir.setReturnValue(Component.literal("An error occured\nPlease check Server Log for more information\n\n" + e));
            }
        }else{
            LOGGER.info("Whitelist mode or online mode disabled");
        }
    }
}
