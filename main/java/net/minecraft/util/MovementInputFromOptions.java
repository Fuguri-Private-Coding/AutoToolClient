package net.minecraft.util;

import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveEvent;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn) {
        gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        MoveButtonEvent event = MoveButtonEvent.getInstance();
        event.setForward(gameSettings.keyBindForward.isKeyDown());
        event.setBack(gameSettings.keyBindBack.isKeyDown());
        event.setLeft(gameSettings.keyBindLeft.isKeyDown());
        event.setRight(gameSettings.keyBindRight.isKeyDown());
        event.setJump(gameSettings.keyBindJump.isKeyDown());
        event.setSneak(gameSettings.keyBindSneak.isKeyDown());
        event.call();

        moveStrafe = 0.0F;
        moveForward = 0.0F;

        if (event.isForward()) {
            ++moveForward;
        }

        if (event.isBack()) {
            --moveForward;
        }

        if (event.isLeft()) {
            ++moveStrafe;
        }

        if (event.isRight()) {
            --moveStrafe;
        }

        jump = event.isJump();
        sneak = event.isSneak();

        MoveEvent moveEvent = MoveEvent.getInstance();
        moveEvent.setForward(moveForward);
        moveEvent.setStrafe(moveStrafe);
        moveEvent.call();

        moveStrafe = moveEvent.getStrafe();
        moveForward = moveEvent.getForward();

        if (sneak) {
            moveStrafe *= moveEvent.getSneakSlowDown();
            moveForward *= moveEvent.getSneakSlowDown();
        }
    }
}
