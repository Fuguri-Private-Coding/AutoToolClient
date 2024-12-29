package net.minecraft.util;

import me.hackclient.Client;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.event.events.MoveEvent;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        MoveButtonEvent event = new MoveButtonEvent(
                gameSettings.keyBindForward.isKeyDown(),
                gameSettings.keyBindBack.isKeyDown(),
                gameSettings.keyBindLeft.isKeyDown(),
                gameSettings.keyBindRight.isKeyDown(),
                gameSettings.keyBindJump.isKeyDown(),
                gameSettings.keyBindSneak.isKeyDown()
        );
        Client.INSTANCE.getObjectsCaller().onEvent(event);

        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (event.isForward())
        {
            ++this.moveForward;
        }

        if (event.isBack())
        {
            --this.moveForward;
        }

        if (event.isLeft())
        {
            ++this.moveStrafe;
        }

        if (event.isRight())
        {
            --this.moveStrafe;
        }

        this.jump = event.isJump();
        this.sneak = event.isSneak();

        MoveEvent moveEvent = new MoveEvent(moveForward, moveStrafe);
        Client.INSTANCE.getObjectsCaller().onEvent(moveEvent);
        moveStrafe = moveEvent.getStrafe();
        moveForward = moveEvent.getForward();

        if (this.sneak)
        {
            this.moveStrafe *= 0.3F;
            this.moveForward *= 0.3F;
        }
    }
}
