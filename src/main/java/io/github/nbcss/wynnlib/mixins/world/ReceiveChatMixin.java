package io.github.nbcss.wynnlib.mixins.world;

import io.github.nbcss.wynnlib.events.PlayerReceiveChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ReceiveChatMixin {
    @Inject(method = "onProfilelessChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/message/MessageHandler;onProfilelessMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V"), cancellable = true)
    public void onGameMessage(ProfilelessChatMessageS2CPacket packet, CallbackInfo ci) {
        Text message = packet.message();
        PlayerReceiveChatEvent event = new PlayerReceiveChatEvent(message);
        PlayerReceiveChatEvent.Companion.handleEvent(event);
        if (event.getCancelled()){
            ci.cancel();
        }
    }
}
