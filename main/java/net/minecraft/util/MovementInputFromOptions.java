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
        gameSettings = gameSettingsIn;
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

        moveStrafe = 0.0F;
        moveForward = 0.0F;

        if (event.isForward())
        {
            ++moveForward;
        }

        if (event.isBack())
        {
            --moveForward;
        }

        if (event.isLeft())
        {
            ++moveStrafe;
        }

        if (event.isRight())
        {
            --moveStrafe;
        }

        jump = event.isJump();
        sneak = event.isSneak();

        if (sneak) {
            moveStrafe *= 0.3F;
            moveForward *= 0.3F;
        }
        
        MoveEvent moveEvent = new MoveEvent(moveForward, moveStrafe);
        Client.INSTANCE.getObjectsCaller().onEvent(moveEvent);
        moveStrafe = moveEvent.getStrafe();
        moveForward = moveEvent.getForward();
    }
}
