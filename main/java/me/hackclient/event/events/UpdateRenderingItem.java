package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.CancelableEvent;
import net.minecraft.item.ItemStack;

@Getter
@Setter
@AllArgsConstructor
public class UpdateRenderingItem extends CancelableEvent {
    ItemStack stack;
}
