package club.towr5291.robotconfig;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all the hardware for a drive base.
 * In this case that robot is a Pushbot.
 * See PushbotTeleopTank_Iterative and others classes starting with "Pushbot" for usage examples.
 *
 * This hardware class assumes the following device names have been configured on the robot:
 * Note:  All names are lower case and some have single spaces between words.
 *
 * Modern Robotics Gyro: "mrgyro"
 *
 */
public class HardwareSensorsSkyStone
{
    public GyroSensor sensorMrGyro = null;

    private DistanceSensor sensorRangeFrontLeft;
    private DistanceSensor sensorRangeFrontRight;
    private DistanceSensor sensorRangeRearLeft;
    private DistanceSensor sensorRangeRearRight;
    private DistanceSensor sensorRangeSideLeft;
    private DistanceSensor sensorRangeSideRight;
    private ColorSensor sensorColorLeft;
    private DistanceSensor sensorColorDistanceLeft;
    private ColorSensor sensorColorRight;
    private DistanceSensor sensorColorDistanceRight;

    /* local OpMode members. */
    HardwareMap hardwareMap     = null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public HardwareSensorsSkyStone(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hardwareMap = ahwMap;

        //sensorMrGyro = ahwMap.gyroSensor.get("mrgyro");
        // you can use this as a regular DistanceSensor.
        sensorRangeFrontLeft     = hardwareMap.get(DistanceSensor.class, "rangeFrontLeft");
        sensorRangeFrontRight    = hardwareMap.get(DistanceSensor.class, "rangeFrontRight");
        sensorRangeRearLeft      = hardwareMap.get(DistanceSensor.class, "rangeRearLeft");
        sensorRangeRearRight     = hardwareMap.get(DistanceSensor.class, "rangeRearRight");
        sensorRangeSideLeft      = hardwareMap.get(DistanceSensor.class, "rangeSideLeft");
        sensorRangeSideRight     = hardwareMap.get(DistanceSensor.class, "rangeSideRight");
        sensorColorLeft          = hardwareMap.get(ColorSensor .class, "colorSensorLeft");
        sensorColorDistanceLeft  = hardwareMap.get(DistanceSensor.class, "colorSensorLeft");
        sensorColorRight         = hardwareMap.get(ColorSensor .class, "colorSensorRight");
        sensorColorDistanceRight = hardwareMap.get(DistanceSensor.class, "colorSensorRight");
    }

    public double distanceFrontLeftCM(){
        return sensorRangeFrontLeft.getDistance(DistanceUnit.CM);
    }
    public double distanceFrontLeftIN(){
        return sensorRangeFrontLeft.getDistance(DistanceUnit.INCH);
    }
    public double distanceFrontRightCM(){
        return sensorRangeFrontRight.getDistance(DistanceUnit.CM);
    }
    public double distanceFrontRightIN(){
        return sensorRangeFrontRight.getDistance(DistanceUnit.INCH);
    }
    public double distanceRearLeftCM(){
        return sensorRangeRearLeft.getDistance(DistanceUnit.CM);
    }
    public double distanceRearLeftIN(){
        return sensorRangeRearLeft.getDistance(DistanceUnit.INCH);
    }
    public double distanceRearRightCM(){
        return sensorRangeRearRight.getDistance(DistanceUnit.CM);
    }
    public double distanceRearRightIN(){
        return sensorRangeRearRight.getDistance(DistanceUnit.INCH);
    }
    public double distanceSideLeftCM(){
        return sensorRangeSideLeft.getDistance(DistanceUnit.CM);
    }
    public double distanceSideLeftIN(){
        return sensorRangeSideLeft.getDistance(DistanceUnit.INCH);
    }
    public double distanceSideRightCM(){
        return sensorRangeSideRight.getDistance(DistanceUnit.CM);
    }
    public double distanceSideRightIN(){
        return sensorRangeSideRight.getDistance(DistanceUnit.INCH);
    }

    public double distanceColorSideRightCM(){
        return sensorColorDistanceRight.getDistance(DistanceUnit.CM);
    }
    public double distanceColorSideRightIN(){
        return sensorColorDistanceRight.getDistance(DistanceUnit.INCH);
    }

    public double distanceColorSideLeftCM(){
        return sensorColorDistanceLeft.getDistance(DistanceUnit.CM);
    }
    public double distanceColorSideLeftIN(){
        return sensorColorDistanceLeft.getDistance(DistanceUnit.INCH);
    }
    public ColorSensor distanceColorSideRight() {
        return sensorColorRight;
    }
    public ColorSensor distanceColorSideLeft() {
        return sensorColorLeft;
    }
}

