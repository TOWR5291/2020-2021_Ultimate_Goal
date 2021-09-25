package club.towr5291.opmodes;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.firstinspires.ftc.teamcode.R;

import club.towr5291.functions.Constants;
import club.towr5291.functions.FileLogger;
import club.towr5291.functions.TOWR5291Tick;
import club.towr5291.functions.TOWR5291Toggle;
import club.towr5291.libraries.LibraryMotorType;
import club.towr5291.libraries.TOWRDashBoard;
import club.towr5291.libraries.robotConfig;
import club.towr5291.libraries.robotConfigSettings;
import club.towr5291.robotconfig.HardwareArmMotorsSkyStone;
import club.towr5291.robotconfig.HardwareArmMotorsUltimateGoal;
import club.towr5291.robotconfig.HardwareDriveMotors;


/*
    made by Emma Beggs 11-15-20  ^^^^
*/
@TeleOp(name = "BaseDriveEmma", group = "5291")
public class BaseDriveEmma extends OpModeMasterLinear {
    private Constants.stepState stepState = Constants.stepState.STATE_COMPLETE;
    private boolean hold = false;
    private boolean clawIsOpen = false;
    private boolean armIsUp = true;

    /* Hardware Set Up */
    private HardwareDriveMotors robotDrive = new HardwareDriveMotors();
    private HardwareArmMotorsUltimateGoal robotArms = new HardwareArmMotorsUltimateGoal();

    //Settings from the sharepreferences
    private SharedPreferences sharedPreferences;

    private FileLogger fileLogger;
    final String TAG = "TeleOp";
    private ElapsedTime runtime = new ElapsedTime();
    private robotConfig ourRobotConfig;

    public double HOLDINGTILTMOTORPOWER = .75;
    private int debug;
    public int originalliftposition;
    private static TOWRDashBoard dashboard = null;

    public static TOWRDashBoard getDashboard() {
        return dashboard;
    }

    @Override
    public void runOpMode() throws InterruptedException {

        dashboard = TOWRDashBoard.createInstance(telemetry);
        FtcRobotControllerActivity act = (FtcRobotControllerActivity) (hardwareMap.appContext);

        dashboard.setTextView((TextView) act.findViewById(R.id.textOpMode));
        dashboard.displayPrintf(0, "Starting Menu System");

        ourRobotConfig = new robotConfig();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(hardwareMap.appContext);

        ourRobotConfig.setAllianceColor(sharedPreferences.getString("club.towr5291.Autonomous.Color", "Red"));// Using a Function to Store The Robot Specification
        ourRobotConfig.setTeamNumber(sharedPreferences.getString("club.towr5291.Autonomous.TeamNumber", "0000"));
        ourRobotConfig.setAllianceStartPosition(sharedPreferences.getString("club.towr5291.Autonomous.Position", "Left"));
        ourRobotConfig.setDelay(Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Delay", "0")));
        ourRobotConfig.setRobotMotorType(sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorType", "REV20ORBIT"));
        ourRobotConfig.setRobotConfigBase(sharedPreferences.getString("club.towr5291.Autonomous.RobotConfigBase", "TileRunner2x40"));
        debug = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "1"));

        //now we have loaded the config from sharedpreferences we can setup the robot
        ourRobotConfig.initConfig();
        dashboard.displayPrintf(0, "Robot Config Loaded");

        fileLogger = new FileLogger(runtime, Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "1")), true);// initializing FileLogger
        fileLogger.open();// Opening FileLogger
        fileLogger.writeEvent(TAG, "Log Started");// First Line Add To Log

        // All The Specification of the robot and controller
        fileLogger.writeEvent(1, "Alliance Color", ourRobotConfig.getAllianceColor());
        fileLogger.writeEvent(1, "Alliance Start Position", ourRobotConfig.getAllianceStartPosition());
        fileLogger.writeEvent(1, "Delay", String.valueOf(ourRobotConfig.getDelay()));
        fileLogger.writeEvent(1, "Robot Base Config", ourRobotConfig.getRobotConfigBase());
        fileLogger.writeEvent(1, "Robot Motor Type", ourRobotConfig.getRobotMotorType());
        fileLogger.writeEvent(1, "Team Number", ourRobotConfig.getTeamNumber());

        robotDrive.init(fileLogger, hardwareMap, robotConfigSettings.robotConfigChoice.valueOf(ourRobotConfig.getRobotConfigBase()), LibraryMotorType.MotorTypes.valueOf(ourRobotConfig.getRobotMotorType()));// Starting robot Hardware map
        dashboard.displayPrintf(0, "Robot Base Loaded");

        robotDrive.allMotorsStop();
        robotDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        robotArms.init(hardwareMap, dashboard);

        fileLogger.writeEvent(1, "", "Wait For Start ");

        dashboard.displayPrintf(1, "Waiting for Start");

        robotArms.liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //new stuff added for liftmotor
        robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //robotArms.flywheelMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robotArms.flywheelMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        originalliftposition = robotArms.liftMotor1.getCurrentPosition();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        dashboard.clearDisplay();
        fileLogger.writeEvent("Starting Loop");

        dashboard.clearDisplay();
                dashboard.displayPrintf(3, "Controller A Options");
        dashboard.displayPrintf(4, "--------------------");
        dashboard.displayPrintf(8, "Controller B Options");
        dashboard.displayPrintf(9, "--------------------");

        //the main loop.  this is where the action happens
        while (opModeIsActive()) {
            fileLogger.writeEvent(1, "In Main Loop");

            dashboard.displayPrintf(5, "Controller Mode -- ", "Mecanum Drive Relic Recovery (BAD)");
            fileLogger.writeEvent(debug, "Controller Mode", "Mecanum Drive Relic Recovery");

            robotDrive.baseMotor1.setPower(Range.clip(gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x, -1, 1));
            robotDrive.baseMotor2.setPower(Range.clip(gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x, -1, 1));
            robotDrive.baseMotor3.setPower(Range.clip(gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x, -1, 1));
            robotDrive.baseMotor4.setPower(Range.clip(gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x, -1, 1));

            liftMotorPower();

            // ejector
            if (gamepad2.a)
                robotArms.ejector.setPosition(.3);
            else
                robotArms.ejector.setPosition(0);

            // gripper claw
            if (gamepad2.left_stick_button){
                robotArms.foundationServo.setPosition(0);
                clawIsOpen = true;
                if (armIsUp){
                    hold = false;}
            }
            else if (gamepad2.right_stick_button){
                robotArms.foundationServo.setPosition(1);
                clawIsOpen = false;}

            //activate flywheel
            if (gamepad2.dpad_up) {
                //robotArms.flywheelMotor.setPower(-0.71);
                //robotArms.flywheelMotor.setPower(-0.64);
                robotArms.flywheelMotor.setVelocity(-1675);
                telemetry.clearAll();
                telemetry.update();
                double mitch = robotArms.flywheelMotor.getVelocity();
                telemetry.addLine("Velocity "  + String.valueOf(mitch));
                //telemetry.addLine("FLywheel " + robotArms.flywheelMotor.getCurrentPosition());

            } else if (gamepad2.dpad_right) {
                //robotArms.flywheelMotor.setPower(-0.70);
                //robotArms.flywheelMotor.setPower(-0.63);
                robotArms.flywheelMotor.setVelocity(-1650);
                double mitch = robotArms.flywheelMotor.getVelocity();
                telemetry.clearAll();
                telemetry.update();
               // telemetry.addLine("Did this Change? " + robotArms.flywheelMotor.getCurrentPosition());
                telemetry.addLine("Velocity "  + String.valueOf(mitch));
                //fileLogger.writeEvent(2, "Actual Velocity: " + String.valueOf(mitch));
            } else if (gamepad2.dpad_down) {
                //robotArms.flywheelMotor.setPower(-0.69);
                //robotArms.flywheelMotor.setPower(-0.62);
                robotArms.flywheelMotor.setVelocity(-1625);
                double mitch = robotArms.flywheelMotor.getVelocity();
                telemetry.addLine("Velocity "  + String.valueOf(mitch));
            } else if (gamepad2.dpad_left) {
                //robotArms.flywheelMotor.setPower(-0.68);
                //robotArms.flywheelMotor.setPower(-0.61);
                robotArms.flywheelMotor.setVelocity(-1475);
                telemetry.update();
                telemetry.clearAll();
                double mitch = robotArms.flywheelMotor.getVelocity();
                telemetry.addLine("Velocity "  + String.valueOf(mitch));
            } else {
                //robotArms.flywheelMotor.setPower(0);
                robotArms.flywheelMotor.setVelocity(0);
                double mitch = robotArms.flywheelMotor.getVelocity();
                telemetry.update();
                telemetry.clearAll();
                telemetry.addLine("Velocity "  + String.valueOf(mitch));

            }
            //Intake testing
            if (gamepad2.left_bumper)
                robotArms.intakeMotor.setPower(1.0);
            else if (gamepad2.right_bumper)
                robotArms.intakeMotor.setPower(-1.0);
            else
                robotArms.intakeMotor.setPower(0);


        }
        //stop the logging
        if (fileLogger != null) {
            fileLogger.writeEvent(1, "TeleOP FINISHED - FINISHED");
            fileLogger.writeEvent(1, "Stopped");
            fileLogger.close();
            fileLogger = null;
        }
    } //RunOpMode

    public void liftMotorPower() {

        if ((gamepad2.x)) {

            //          robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            //          robotArms.liftMotor1.setTargetPosition(originalliftposition - 80);
            //          robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robotArms.liftMotor1.setPower(-0.5);
            hold = false;
            armIsUp = false;
            //telemetry.addLine("Lift Motor X " + robotArms.liftMotor1.getCurrentPosition());
            //telemetry.update();
            fileLogger.writeEvent(8, "Setting the power to the tilt motor at " + robotArms.liftMotor1.getPower());
        } else if ((gamepad2.y)) {
            hold = true;
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robotArms.liftMotor1.setTargetPosition(originalliftposition);
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robotArms.liftMotor1.setPower(0.75);
            armIsUp = true;
            //telemetry.addLine("Lift Motor Y " + robotArms.liftMotor1.getCurrentPosition());
            //telemetry.update();
        }
        else if ((gamepad1.x)){
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robotArms.liftMotor1.setPower(1);
        }
        else if ((gamepad1.y)){
            originalliftposition = robotArms.liftMotor1.getCurrentPosition();
        }
        else if (!hold)
            robotArms.liftMotor1.setPower(0);
            //telemetry.addLine("Lift Motor default " + robotArms.liftMotor1.getCurrentPosition());

        //-gamepad2.left_stick_y);
    }


    //Original liftmotor method
 /*   public void liftMotorPower(){

        if ((gamepad2.left_stick_y < .1 && gamepad2.left_stick_y > -0.1)) {
            if ((hold == false)) {
                fileLogger.writeEvent(8, "Hold is Now true");
                fileLogger.writeEvent(8,"Run using encoders now in hold function in the movement of the arm");
                robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                fileLogger.writeEvent(8, "Setting the target position so that the arm does not move!");
                robotArms.liftMotor1.setTargetPosition(robotArms.liftMotor1.getCurrentPosition());
                hold = true;
            }
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            robotArms.liftMotor1.setPower(HOLDINGTILTMOTORPOWER);

            fileLogger.writeEvent(8, "Setting the power to the tilt motor at " + robotArms.liftMotor1.getPower());
        } else {
            robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            hold = false;
            robotArms.liftMotor1.setPower(Range.clip(-gamepad2.left_stick_y, -0.75, 0.75));

                    //-gamepad2.left_stick_y);
        }
    }  */

}