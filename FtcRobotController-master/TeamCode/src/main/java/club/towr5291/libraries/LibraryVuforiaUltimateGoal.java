package club.towr5291.libraries;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.vuforia.HINT;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

public class LibraryVuforiaUltimateGoal {

    private VuforiaLocalizer vuforia;
    private VuforiaTrackables targetsUltimateGoal;
    private VuforiaTrackable blueTowerGoalTarget;
    private VuforiaTrackable redTowerGoalTarget;
    private VuforiaTrackable redAllianceTarget;
    private VuforiaTrackable blueAllianceTarget;
    private VuforiaTrackable frontWallTarget;
    private List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();

    /**
     * We use units of mm here because that's the recommended units of measurement for the
     * size values specified in the XML for the ImageTarget trackables in data sets. E.g.:
     * You don't *have to* use mm here, but the units here and the units used in the XML
     * target configuration files *must* correspond for the math to work out correctly.
     */
    private static final float mmPerInch = 25.4f;
    private float FTCFieldWidth = (12 * 12 - 3);
    private float mmFTCFieldWidth = FTCFieldWidth * mmPerInch;   // the FTC field is ~11'10" center-to-center of the glass panels
    private static final float mmTargetHeight = (6) * mmPerInch;          // the height of the center of the target image above the floor
    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField = 36 * mmPerInch;

    private boolean targetVisible = false;
    private float phoneXRotate = 0;
    private float phoneYRotate = 0;
    private float phoneZRotate = 0;
    private static final boolean PHONE_IS_PORTRAIT = false;

    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;

    public VuforiaTrackables gettargetsUltimateGoal() {
        return this.targetsUltimateGoal;
    }

    public VuforiaTrackable getblueTowerGoalTarget() {
        return this.blueTowerGoalTarget;
    }

    public VuforiaTrackable getredTowerGoalTarget() {
        return redTowerGoalTarget;
    }

    public VuforiaTrackable getredAllianceTarget() {
        return redAllianceTarget;
    }

    public VuforiaTrackable getblueAllianceTarget() {
        return blueAllianceTarget;
    }

    public VuforiaTrackable getfrontWallTarget() {
        return frontWallTarget;
    }

    public VuforiaLocalizer getVuforia() {
        return this.vuforia;
    }

    public List<VuforiaTrackable> getAllTrackables() {
        return this.allTrackables;
    }


    public VuforiaTrackables LibraryVuforiaRoverRuckus(HardwareMap hardwareMap, robotConfig robotConfiguration, boolean ShowView) {
        //load all the vuforia stuff

        /*
         * To start up Vuforia, tell it the view that we wish to use for camera monitor (on the RC phone);
         * If no camera monitor is desired, use the parameterless constructor instead (commented out below).
         */
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters;
        if (ShowView) {
            parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        } else {
            parameters = new VuforiaLocalizer.Parameters();
        }

        // OR...  Do Not Activate the Camera Monitor View, to save power
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        switch (robotConfiguration.getTeamNumber()) {
            case "5291":
                parameters.vuforiaLicenseKey = "AVnlHKP/////AAABmU5kpwBUw0KGkmPKLAjP2fthurq7h6D9ULkmkt8zlkqRmNOdUk3BsiLm+o93UF/GumwUEhMEUD2R5SCnKb2GeULtLWeSCLjIRYEGSAfOAnt4vVHboCAvwrOlUykc1WESQrw2sbO+jhb/rw6RVR8v3416VgUUO0AHKPN1M47o0PZO17pIYXVcUYByKSc7fqmm/Lld/XdYbCNBwRJnTFgautU/GsLx193RQSN4GAAtW4yOIyLRC8Ezy6zRIqm2RQdxFh9puI0cB/tDc0oZVtSPBg69MEVmEpP0HloGstMtIgFLpp56eH4rmO/ngHsmIVZ0XbSSvAp68QLdKc6IYYJYQTthftLcP6N4z/avQfOwp1wU";
                break;
            case "11230":
                parameters.vuforiaLicenseKey = "Not Provided";
                break;
            case "11231":
                parameters.vuforiaLicenseKey = "Aai2GEX/////AAAAGaIIK9GK/E5ZsiRZ/jrJzdg7wYZCIFQ7uzKqQrMx/0Hh212zumzIy4raGwDY6Mf6jABMShH2etZC/BcjIowIHeAG5ShG5lvZIZEplTO+1zK1nFSiGFTPV59iGVqH8KjLbQdgUbsCBqp4f3tI8BWYqAS27wYIPfTK697SuxdQnpEZAOhHpgz+S2VoShgGr+EElzYMBFEaj6kdA/Lq5OwQp31JPet7NWYph6nN+TNHJAxnQBkthYmQg687WlRZhYrvNJepnoEwsDO3NSyeGlFquwuQwgdoGjzq2qn527I9tvM/XVZt7KR1KyWCn3PIS/LFvADSuyoQ2lsiOFtM9C+KCuNWiqQmj7dPPlpvVeUycoDH";
                break;
            default:
                parameters.vuforiaLicenseKey = "AVnlHKP/////AAABmU5kpwBUw0KGkmPKLAjP2fthurq7h6D9ULkmkt8zlkqRmNOdUk3BsiLm+o93UF/GumwUEhMEUD2R5SCnKb2GeULtLWeSCLjIRYEGSAfOAnt4vVHboCAvwrOlUykc1WESQrw2sbO+jhb/rw6RVR8v3416VgUUO0AHKPN1M47o0PZO17pIYXVcUYByKSc7fqmm/Lld/XdYbCNBwRJnTFgautU/GsLx193RQSN4GAAtW4yOIyLRC8Ezy6zRIqm2RQdxFh9puI0cB/tDc0oZVtSPBg69MEVmEpP0HloGstMtIgFLpp56eH4rmO/ngHsmIVZ0XbSSvAp68QLdKc6IYYJYQTthftLcP6N4z/avQfOwp1wU";
                break;
        }

        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        this.vuforia = ClassFactory.getInstance().createVuforia(parameters);

        vuforia.enableConvertFrameToBitmap();

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);                                          //enables RGB565 format for the image
        vuforia.setFrameQueueCapacity(5);                                                           //tells VuforiaLocalizer to only store one frame at a time
        Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 1);

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables targetsUltimateGoal = this.vuforia.loadTrackablesFromAsset("UltimateGoal");
        VuforiaTrackable blueTowerGoalTarget = targetsUltimateGoal.get(0);
        blueTowerGoalTarget.setName("Blue Tower Goal Target");
        VuforiaTrackable redTowerGoalTarget = targetsUltimateGoal.get(1);
        redTowerGoalTarget.setName("Red Tower Goal Target");
        VuforiaTrackable redAllianceTarget = targetsUltimateGoal.get(2);
        redAllianceTarget.setName("Red Alliance Target");
        VuforiaTrackable blueAllianceTarget = targetsUltimateGoal.get(3);
        blueAllianceTarget.setName("Blue Alliance Target");
        VuforiaTrackable frontWallTarget = targetsUltimateGoal.get(4);
        frontWallTarget.setName("Front Wall Target");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsUltimateGoal);

        /**
         * In order for localization to work, we need to tell the system where each target is on the field, and
         * where the phone resides on the robot.  These specifications are in the form of <em>transformation matrices.</em>
         * Transformation matrices are a central, important concept in the math here involved in localization.
         * See <a href="https://en.wikipedia.org/wiki/Transformation_matrix">Transformation Matrix</a>
         * for detailed information. Commonly, you'll encounter transformation matrices as instances
         * of the {@link OpenGLMatrix} class.
         *
         * If you are standing in the Red Alliance Station looking towards the center of the field,
         *     - The X axis runs from your left to the right. (positive from the center to the right)
         *     - The Y axis runs from the Red Alliance Station towards the other side of the field
         *       where the Blue Alliance Station is. (Positive is from the center, towards the BlueAlliance station)
         *     - The Z axis runs from the floor, upwards towards the ceiling.  (Positive is above the floor)
         *
         * Before being transformed, each target image is conceptually located at the origin of the field's
         *  coordinate system (the center of the field), facing up.
         */

        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90;
        }


        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, parameters.cameraDirection);
        }

        return targetsUltimateGoal;


    }
    public VuforiaTrackables LibraryVuforiaUltimateGoal(HardwareMap hardwareMap, robotConfig robotConfiguration, WebcamName robotWebcam, boolean ShowView) {

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters;
        if (ShowView) {
            parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        } else {
            parameters = new VuforiaLocalizer.Parameters();
        }
        switch (robotConfiguration.getTeamNumber()) {
            case "5291":
                parameters.vuforiaLicenseKey = "AVnlHKP/////AAABmU5kpwBUw0KGkmPKLAjP2fthurq7h6D9ULkmkt8zlkqRmNOdUk3BsiLm+o93UF/GumwUEhMEUD2R5SCnKb2GeULtLWeSCLjIRYEGSAfOAnt4vVHboCAvwrOlUykc1WESQrw2sbO+jhb/rw6RVR8v3416VgUUO0AHKPN1M47o0PZO17pIYXVcUYByKSc7fqmm/Lld/XdYbCNBwRJnTFgautU/GsLx193RQSN4GAAtW4yOIyLRC8Ezy6zRIqm2RQdxFh9puI0cB/tDc0oZVtSPBg69MEVmEpP0HloGstMtIgFLpp56eH4rmO/ngHsmIVZ0XbSSvAp68QLdKc6IYYJYQTthftLcP6N4z/avQfOwp1wU";
                break;
            case "11230":
                parameters.vuforiaLicenseKey = "Not Provided";
                break;
            case "11231":
                parameters.vuforiaLicenseKey = "Aai2GEX/////AAAAGaIIK9GK/E5ZsiRZ/jrJzdg7wYZCIFQ7uzKqQrMx/0Hh212zumzIy4raGwDY6Mf6jABMShH2etZC/BcjIowIHeAG5ShG5lvZIZEplTO+1zK1nFSiGFTPV59iGVqH8KjLbQdgUbsCBqp4f3tI8BWYqAS27wYIPfTK697SuxdQnpEZAOhHpgz+S2VoShgGr+EElzYMBFEaj6kdA/Lq5OwQp31JPet7NWYph6nN+TNHJAxnQBkthYmQg687WlRZhYrvNJepnoEwsDO3NSyeGlFquwuQwgdoGjzq2qn527I9tvM/XVZt7KR1KyWCn3PIS/LFvADSuyoQ2lsiOFtM9C+KCuNWiqQmj7dPPlpvVeUycoDH";
                break;
        }
        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */
        parameters.cameraName = robotWebcam;

        this.vuforia = ClassFactory.getInstance().createVuforia(parameters);
        vuforia.enableConvertFrameToBitmap();
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);                                          //enables RGB565 format for the image
        vuforia.setFrameQueueCapacity(5);

        this.targetsUltimateGoal = this.vuforia.loadTrackablesFromAsset("UltimateGoal");
        this.blueTowerGoalTarget = this.targetsUltimateGoal.get(0);
        this.blueTowerGoalTarget.setName("Blue Tower Goal Target"); // can help in debugging; otherwise not necessary
        this.redTowerGoalTarget = this.targetsUltimateGoal.get(1);
        this.redTowerGoalTarget.setName("Red Tower Goal Target");
        this.redAllianceTarget = this.targetsUltimateGoal.get(2);
        this.redAllianceTarget.setName("Red Alliance Target");
        this.blueAllianceTarget = this.targetsUltimateGoal.get(3);
        this.blueAllianceTarget.setName("Blue Alliance Target");
        this.frontWallTarget = this.targetsUltimateGoal.get(4);
        this.frontWallTarget.setName("Front Wall Target");

        allTrackables.addAll(this.targetsUltimateGoal);

        float mmPerInch = 25.4f;
        float FTCFieldWidth = (12 * 12 - 3);
        float mmFTCFieldWidth = FTCFieldWidth * mmPerInch;   // the FTC field is ~11'10" center-to-center of the glass panels
        float mmTargetHeight = (6) * mmPerInch;          // the height of the center of the target image above the floor
        // Constants for perimeter targets
        float halfField = 72 * mmPerInch;
        float quadField = 36 * mmPerInch;

        boolean targetVisible = false;
        float phoneXRotate = 0;
        float phoneYRotate = 0;
        float phoneZRotate = 0;
        boolean PHONE_IS_PORTRAIT = false;

        VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;

        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90;
        }


        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, parameters.cameraDirection);
        }

        return targetsUltimateGoal;
    }
    public VuforiaLocalizer getVuforiaLocalizer(){
        return vuforia;
    }
    }