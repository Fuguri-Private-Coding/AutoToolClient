package net.minecraft.util;

import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveEvent;
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
        event.call();

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

        MoveEvent moveEvent = new MoveEvent(moveForward, moveStrafe, jump, sneak,0.3f);
        moveEvent.call();
        moveStrafe = moveEvent.getStrafe();
        moveForward = moveEvent.getForward();
        jump = moveEvent.isJump();
        sneak = moveEvent.isSneak();

        if (sneak) {
            moveStrafe *= moveEvent.getSneakSlowDown();
            moveForward *= moveEvent.getSneakSlowDown();
        }
    }
}
