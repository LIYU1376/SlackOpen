package net.minecraft.network.play.client;

import java.io.IOException;

import cc.slack.features.modules.impl.other.Tweaks;
import cc.slack.start.Slack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C01PacketChatMessage implements Packet<INetHandlerPlayServer>
{
    private String message;

    public C01PacketChatMessage()
    {
    }

    public C01PacketChatMessage(String messageIn)
    {
        int maxLength = Slack.getInstance().getModuleManager().getInstance(Tweaks.class).biggerChat.getValue() ? 256 : 100;

        if (messageIn.length() > maxLength) {
            messageIn = messageIn.substring(0, maxLength);
        }

        this.message = messageIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.message = buf.readStringFromBuffer(100);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(this.message);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processChatMessage(this);
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String s) {
        this.message = s;
    }
}
