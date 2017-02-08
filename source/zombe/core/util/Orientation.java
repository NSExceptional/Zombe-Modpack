package zombe.core.util;


public class Orientation {

    public float yaw;
    public float pitch;
    public float roll;

    public Orientation(float yaw, float pitch) {
        this(yaw, pitch, 0f);
    }

    public Orientation(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }
}

