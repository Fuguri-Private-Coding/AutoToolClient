package net.minecraft.network.play.server;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

@Getter
@Setter
public class S18PacketEntityTeleport implements Packet<INetHandlerPlayClient>
{
    int entityId, x, y, z;
    byte yaw, pitch;
    boolean onGround;

    public S18PacketEntityTeleport() {}

    public S18PacketEntityTeleport(Entity entityIn) {
        entityId = entityIn.getEntityId();
        x = MathHelper.floor_double(entityIn.posX * 32.0D);
        y = MathHelper.floor_double(entityIn.posY * 32.0D);
        z = MathHelper.floor_double(entityIn.posZ * 32.0D);
        yaw = (byte)((int)(entityIn.rotationYaw * 256.0F / 360.0F));
        pitch = (byte)((int)(entityIn.rotationPitch * 256.0F / 360.0F));
        onGround = entityIn.onGround;
    }

    public S18PacketEntityTeleport(int entityIdIn, int posXIn, int posYIn, int posZIn, byte yawIn, byte pitchIn, boolean onGroundIn) {
        entityId = entityIdIn;
        x = posXIn;
        y = posYIn;
        z = posZIn;
        yaw = yawIn;
        pitch = pitchIn;
        onGround = onGroundIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        entityId = buf.readVarIntFromBuffer();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readByte();
        pitch = buf.readByte();
        onGround = buf.readBoolean();
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(onGround);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityTeleport(this);
    }
}
