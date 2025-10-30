package net.minecraft.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vector3d
{
    public double x;
    public double y;
    public double z;


    public Vector3d add(final double x, final double y, final double z) {
        return new Vector3d(this.x + x, this.y + y, this.z + z);
    }

    public Vector3d add(final Vector3d vector) {
        return add(vector.x, vector.y, vector.z);
    }

    public Vector3d subtract(final double x, final double y, final double z) {
        return add(-x, -y, -z);
    }

    public Vector3d subtract(final Vector3d vector) {
        return add(-vector.x, -vector.y, -vector.z);
    }

    public Vector3d()
    {
        this.x = this.y = this.z = 0.0D;
    }
}
