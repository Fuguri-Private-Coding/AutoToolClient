package fuguriprivatecoding.autotoolrecode.utils.packet;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;

public class PositionResolverComponent implements Imports, EventListener {

    public PositionResolverComponent() {
        Events.register(this);
    }

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketEvent e) {
            Packet packet = e.getPacket();

            if (packet instanceof S14PacketEntity s14 && s14.getEntity(mc.theWorld) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.nx += s14.getPositionX() / 32.0;
                entityLivingBase.ny += s14.getPositionY() / 32.0;
                entityLivingBase.nz += s14.getPositionZ() / 32.0;
                entityLivingBase.posRotIncrements = 3;
            }

            if (packet instanceof S18PacketEntityTeleport s18 && mc.theWorld.getEntityByID(s18.getEntityId()) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.nx = s18.getX() / 32.0;
                entityLivingBase.ny = s18.getY() / 32.0;
                entityLivingBase.nz = s18.getZ() / 32.0;
                entityLivingBase.posRotIncrements = 3;
            }
        }

        if (event instanceof TickEvent) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityLivingBase target) {
                    target.lrx = target.rx;
                    target.lry = target.ry;
                    target.lrz = target.rz;

                    if (target.posRotIncrements > 0) {
                        double d0 = target.rx + (target.nx - target.rx) / target.posRotIncrements;
                        double d1 = target.ry + (target.ny - target.ry) / target.posRotIncrements;
                        double d2 = target.rz + (target.nz - target.rz) / target.posRotIncrements;

                        target.rx = d0;
                        target.ry = d1;
                        target.rz = d2;

                        --target.posRotIncrements;
                    }
                }
            }
        }
    }
}