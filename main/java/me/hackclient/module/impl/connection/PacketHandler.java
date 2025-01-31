package me.hackclient.module.impl.connection;

import lombok.Getter;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.network.Packet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "PacketHandler",
        category = Category.CONNECTION,
        hide = true
)
@Getter
public class PacketHandler extends Module {

    static final
    List<Doubles<Packet, Long>>
            serverPacketBuffer = new CopyOnWriteArrayList<>(),
            clientPacketBuffer = new CopyOnWriteArrayList<>();

    @Override
    public boolean isToggled() {
        return true;
    }

    @Override
    public void toggle() {}
}
