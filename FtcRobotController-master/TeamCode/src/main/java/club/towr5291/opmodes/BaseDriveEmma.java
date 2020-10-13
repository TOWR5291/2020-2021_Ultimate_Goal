package club.towr5291.opmodes;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
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
import club.towr5291.robotconfig.HardwareDriveMotors;


/*
    made by Wyatt Ashley on 8/2/2018
    Xavier was here:>
*/
@TeleOp(name = "BaseDriveEmma", group = "5291")
public class BaseDriveEmma extends OpModeMasterLinear {
    private Constants.stepState stepState = Constants.stepState.STATE_COMPLETE;
    boolean hold = false;

    /* Hardware Set Up */
    private HardwareDriveMotors robotDrive               = new HardwareDriveMotors();
    private HardwareArmMotorsSkyStone robotArms          = new HardwareArmMotorsSkyStone();

    //Settings from the sharepreferences
    private SharedPreferences sharedPreferences;

    private FileLogger fileLogger;
    final String TAG = "TeleOp";
    private ElapsedTime runtime                     = new ElapsedTime();
    private robotConfig ourRobotConfig;

    public double HOLDINGTILTMOTORPOWER = .5;
    private int debug;

    private static TOWRDashBoard dashboard = null;
    public static TOWRDashBoard getDashboard()
    {
        return dashboard;
    }

    @Override
    public void runOpMode() throws InterruptedException {

        dashboard = TOWRDashBoard.createInstance(telemetry);
        FtcRobotControllerActivity act = (FtcRobotControllerActivity)(hardwareMap.appContext);

        dashboard.setTextView((TextView)act.findViewById(R.id.textOpMode));
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
        fileLogger.writeEvent(1,"Alliance Color", ourRobotConfig.getAllianceColor());
        fileLogger.writeEvent(1,"Alliance Start Position", ourRobotConfig.getAllianceStartPosition());
        fileLogger.writeEvent(1,"Delay", String.valueOf(ourRobotConfig.getDelay()));
        fileLogger.writeEvent(1,"Robot Base Config", ourRobotConfig.getRobotConfigBase());
        fileLogger.writeEvent(1,"Robot Motor Type", ourRobotConfig.getRobotMotorType());
        fileLogger.writeEvent(1,"Team Number", ourRobotConfig.getTeamNumber());

        robotDrive.init(fileLogger, hardwareMap, robotConfigSettings.robotConfigChoice.valueOf(ourRobotConfig.getRobotConfigBase()), LibraryMotorType.MotorTypes.valueOf(ourRobotConfig.getRobotMotorType()));// Starting robot Hardware map
        dashboard.displayPrintf(0, "Robot Base Loaded");

        robotDrive.allMotorsStop();
        robotDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        robotArms.init(hardwareMap, dashboard);

        //run positions Teleop
        //start position for within 18 inches
        robotArms.rightWristServo.setPosition(1);
        robotArms.rightArmServo.setPosition(0.0);
        robotArms.rightClampServo.setPosition(0.15);
        robotArms.leftWristServo.setPosition(0.05);
        robotArms.leftArmServo.setPosition(0.0);
        robotArms.leftClampServo.setPosition(0.15);

        fileLogger.writeEvent(1,"","Wait For Start ");

        dashboard.displayPrintf(1, "Waiting for Start");

        robotArms.liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        dashboard.clearDisplay();
        fileLogger.writeEvent("Starting Loop");

        robotArms.leftWristServo.setPosition(1);
        robotArms.rightWristServo.setPosition(.60);
        robotArms.leftArmServo.setPosition(.7);
        robotArms.rightArmServo.setPosition(0);
        robotArms.leftClampServo.setPosition(0.1);
        robotArms.rightClampServo.setPosition(0.3);

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
            robotArms.intakeMotor1.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            // grab the block
            if (gamepad2.a)
                robotArms.grabServo.setPosition(0);
            else if (gamepad2.y)
                robotArms.grabServo.setPosition(.5);

            //rotate the arm out so we can stack the block or bring it back in
            if (gamepad2.b)
                robotArms.wristServo.setPosition(0);
            else if (gamepad2.x)
                robotArms.wristServo.setPosition(1);

            //send the tape measure out
            if (gamepad2.dpad_up)
            {robotArms.tapeMotor.setPower(1);
                robotArms.tapeMotor2.setPower(-1);}
            else if (gamepad2.dpad_right)
            {robotArms.tapeMotor.setPower(0.75);
                robotArms.tapeMotor2.setPower(-0.75);}
            else if (gamepad2.dpad_down)
            {robotArms.tapeMotor.setPower(0.5);
                robotArms.tapeMotor2.setPower(-0.5);}
            else if (gamepad2.dpad_left)
            {robotArms.tapeMotor.setPower(0.25);
                robotArms.tapeMotor2.setPower(-0.25);}
            else
            { robotArms.tapeMotor.setPower(0);
                robotArms.tapeMotor2.setPower(0);}
            //Foundation Arm

            if (gamepad2.left_bumper)
                robotArms.foundationServo.setPosition(0);
            else if (gamepad2.right_bumper)
                robotArms.foundationServo.setPosition(1);

        }
        //stop the logging
        if (fileLogger != null) {
            fileLogger.writeEvent(1, "TeleOP FINISHED - FINISHED");
            fileLogger.writeEvent(1, "Stopped");
            fileLogger.close();
            fileLogger = null;
        }
    } //RunOpMode

    public void liftMotorPower(){

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
            robotArms.liftMotor1.setPower(-gamepad2.left_stick_y);
        }
    }
}
