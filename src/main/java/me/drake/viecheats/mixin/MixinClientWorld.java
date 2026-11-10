package me.drake.viecheats.mixin;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.events.entity.EntityAddedEvent;
import me.drake.viecheats.events.entity.EntityRemovedEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) {
            VieCheatsMod.EVENT_BUS.post(EntityAddedEvent.get(entity));
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = ((ClientWorld) (Object) this).getEntityById(entityId);
        if (entity != null) {
            VieCheatsMod.EVENT_BUS.post(EntityRemovedEvent.get(entity));
        }
    }
}
