package me.hackclient.utils.animation;

import me.hackclient.utils.timer.StopWatch;
import org.lwjgl.util.vector.Vector3f;

public class Animation3D {
    public double x, y, z, endX, endY, endZ;
    final StopWatch stopWatch;

    public Animation3D() {
        endX = endY = endZ = x = y = z = 0;
        stopWatch = new StopWatch();
    }

    public Animation3D(double x, double y, double z, double endX, double endY, double endZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        stopWatch = new StopWatch();
    }

    public void update(float smooth) {
        smooth /= 1000f;
        smooth *= stopWatch.reachedMS();
        Vector3f delta = getDelta();

        x += delta.x * smooth;
        y += delta.y * smooth;
        z += delta.z * smooth;

        stopWatch.reset();
    }

    public Vector3f getDelta() {
        return new Vector3f(
                (float) (endX - x),
                (float) (endY - y),
                (float) (endZ - z)
        );
    }
}
