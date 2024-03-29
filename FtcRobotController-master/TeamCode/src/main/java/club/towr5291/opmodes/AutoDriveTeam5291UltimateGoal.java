/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package club.towr5291.opmodes;

//Android Imports

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl;
import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.HashMap;

import club.towr5291.functions.Constants;
import club.towr5291.functions.FileLogger;
import club.towr5291.functions.ReadStepFileXML;
import club.towr5291.functions.SkyStoneOCV;
import club.towr5291.functions.TOWR5291PID;
import club.towr5291.functions.TOWR5291TextToSpeech;
import club.towr5291.functions.TOWR5291Utils;
import club.towr5291.functions.UltimateGoalOCV;
import club.towr5291.libraries.ImageCaptureOCV;
import club.towr5291.libraries.LibraryMotorType;
import club.towr5291.libraries.LibraryStateSegAutoRoverRuckus;
import club.towr5291.libraries.LibraryVuforiaRoverRuckus;
import club.towr5291.libraries.LibraryVuforiaUltimateGoal;
import club.towr5291.libraries.TOWRDashBoard;
import club.towr5291.libraries.robotConfig;
import club.towr5291.libraries.robotConfigSettings;
import club.towr5291.robotconfig.HardwareArmMotorsSkyStone;
import club.towr5291.robotconfig.HardwareArmMotorsUltimateGoal;
import club.towr5291.robotconfig.HardwareDriveMotors;
import club.towr5291.robotconfig.HardwareSensorsSkyStone;

//Qualcomm Imports
//FTC Imports
//OpenCV Imports
//Java Imports
//Local Imports


/*
TOWR 5291 Autonomous
Copyright (c) 2016 TOWR5291
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

Written by Ian Haden/Wyatt Ashley October 2018
2018-10-27 - Ian Haden  - Converted to ROVER RUCKUS

*/

@Autonomous(name="5291 Autonomous Drive Ultimate Goal", group="5291")
//@Disabled
public class AutoDriveTeam5291UltimateGoal extends OpModeMasterLinear {

    private OpMode onStop = this;
    private OpModeManagerImpl opModeManager;
    private String TeleOpMode = "Base Drive 2019";

    final int LABEL_WIDTH = 200;

    //The autonomous menu settings from the sharepreferences
    private SharedPreferences sharedPreferences;
    private robotConfig ourRobotConfig;

    private ElapsedTime runtime = new ElapsedTime();

    //set up the variables for file logger and what level of debug we will log info at
    public FileLogger fileLogger;
    private int debug = 3;

    //adafruit IMU
    // The IMU sensor object
    private BNO055IMU imu;

    private double mdblTurnAbsoluteGyro;
    private double mdblGyrozAccumulated;
    private double mdblTankTurnGyroRequiredHeading;
    private int mintStableCount;
    private String mstrWiggleDir;
    private double mdblPowerBoost;
    private int mintPowerBoostCount;

    //Camera Webcam
    private WebcamName robotWebcam;

    //vuforia localisation variables
    private OpenGLMatrix lastLocation = null;
    private double localisedRobotX;
    private double localisedRobotY;
    private double localisedRobotBearing;
    private boolean localiseRobotPos;
    private static final int TARGET_WIDTH = 254;
    private static final int TARGET_HEIGHT = 184;

    //define each state for the step.  Each step should go through some of the states below
    // set up the variables for the state engine
    private int mintCurrentStep = 1;                                            // Current Step in State Machine.
    private Constants.stepState mintCurrentStateStep;                           // Current State Machine State.
    private Constants.stepState mintCurrentStateDrive;                          // Current State of Drive.
    private Constants.stepState mintCurrentStateDriveHeading;                   // Current State of Drive Heading.
    private Constants.stepState mintCurrentStateTankTurn;                       // Current State of Tank Turn.
    private Constants.stepState mintCurrentStatePivotTurn;                      // Current State of Pivot Turn.
    private Constants.stepState mintCurrentStateRadiusTurn;                     // Current State of Radius Turn.
    private Constants.stepState mintCurStVuforiaMove5291;                       // Current State of Vuforia Move
    private Constants.stepState mintCurStVuforiaTurn5291;                       // Current State of Vuforia Turn
    private Constants.stepState mintCurrentStateGyroTurnEncoder5291;            // Current State of the Turn function that take the Gyro as an initial heading
    private Constants.stepState mintCurrentStateEyes5291;                       // Current State of the Eyelids
    private Constants.stepState mintCurrentStateTankTurnGyroHeading;            // Current State of Tank Turn using Gyro
    private Constants.stepState mintCurrentStateMecanumStrafe;                  // Current State of mecanum strafe
    private Constants.stepState mintCurrentStepDelay;                           // Current State of Delay (robot doing nothing)
    private Constants.stepState mintCurrentStateMoveLift;                       // Current State of the Move lift
    private Constants.stepState mintCurrentStateInTake;                         // Current State of the Move lift
    private Constants.stepState mintCurrentStateNextStone;                       // Current State of Finding Gold
    private Constants.stepState mintCurrentStateWyattsGyroDrive;                // Wyatt Gyro Function
    private Constants.stepState mintCurrentStateTapeMeasure;                    // Control Tape Measure
    private Constants.stepState mintCurrentStateFlywheel;                       // Control flywheel
    private Constants.stepState mintCurrentStateClawMovement;                           // Control Servo to open close claw
    private Constants.stepState getMintCurrentStateEjector;                     // Control ejector
    private Constants.stepState mintCurrentStateGrabBlock;                      // Control arm to grab block
    private Constants.stepState mintCurrentStepFindGoldSS;                      // test

    private boolean mboolFoundSkyStone = false;

    private double mdblTeamMarkerDrop = .8;
    private double mdblTeamMarkerHome = 0;

    private HashMap<String, Integer> mintActiveSteps = new HashMap<>();
    private HashMap<String, Integer> mintActiveStepsCopy = new HashMap<>();

    //motors
    // load all the robot configurations for this season
    private HardwareDriveMotors robotDrive          = new HardwareDriveMotors();   // Use 5291's hardware
    private HardwareArmMotorsUltimateGoal robotArms     = new HardwareArmMotorsUltimateGoal();   // Use 5291's hardware
    private HardwareSensorsSkyStone sensors         = new HardwareSensorsSkyStone();

    PIDFCoefficients getMotorPIDFMotor1;
    PIDFCoefficients getMotorPIDFMotor2;
    PIDFCoefficients getMotorPIDFMotor3;
    PIDFCoefficients getMotorPIDFMotor4;
    PIDFCoefficients newMotorPIDFMotor1;
    PIDFCoefficients newMotorPIDFMotor2;
    PIDFCoefficients newMotorPIDFMotor3;
    PIDFCoefficients newMotorPIDFMotor4;
    //private HardwareSensorsRoverRuckus sensor       = new HardwareSensorsRoverRuckus();

    private boolean vuforiaWebcam = true;

    //variable for the state engine, declared here so they are accessible throughout the entire opmode with having to pass them through each function
    private double mdblStep;                                 //Step from the step file, probably not needed
    private double mdblStepTimeout;                          //Timeout value ofthe step, the step will abort if the timeout is reached
    private String mstrRobotCommand;                         //The command the robot will execute, such as move forward, turn right etc
    private double mdblStepDistance;                         //used when decoding the step, this will indicate how far the robot is to move in inches
    private double mdblStepSpeed;                            //When a move command is executed this is the speed the motors will run at
    private boolean mblnParallel;                            //used to determine if next step will run in parallel - at same time
    private boolean mblnRobotLastPos;                        //used to determine if next step will run from end of last step or from encoder position
    private double mdblRobotParm1;                           //First Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not
    private double mdblRobotParm2;                           //Second Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not
    private double mdblRobotParm3;                           //Third Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not
    private double mdblRobotParm4;                           //Fourth Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not
    private double mdblRobotParm5;                           //Fifth Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not
    private double mdblRobotParm6;                           //Sixth Parameter of the command, not all commands have paramters, A*Star has parameters, where moveing does not

    private int mintStartPositionLeft1;                      //Left Motor 1  - start position of the robot in inches, starts from 0 to the end
    private int mintStartPositionLeft2;                      //Left Motor 2  - start position of the robot in inches, starts from 0 to the end
    private int mintStartPositionRight1;                     //Right Motor 1 - start position of the robot in inches, starts from 0 to the end
    private int mintStartPositionRight2;                     //Right Motor 2 - start position of the robot in inches, starts from 0 to the end
    private int mintStepLeftTarget1;                         //Left Motor 1   - encoder target position
    private int mintStepLeftTarget2;                         //Left Motor 2   - encoder target position
    private int mintStepRightTarget1;                        //Right Motor 1  - encoder target position
    private int mintStepRightTarget2;                        //Right Motor 2  - encoder target position
    private double mdblDistanceToMoveTilt1;                  //Tilt Motor 1 - encoder target position
    private double mdblDistanceToMoveTilt2;                  //Tilt Motor 2 - encoder target position
    private double mdblTargetPositionTop1;                   //Main Lift Motor 1 - target Position
    private double mdblTargetPositionTop2;                   //Main Lift Motor 1 - target Position
    private double dblStepSpeedTempLeft;
    private double dblStepSpeedTempRight;
    private double mdblStepTurnL;                            //used when decoding the step, this will indicate if the robot is turning left
    private double mdblStepTurnR;                            //used when decoding the step, this will indicate if the robot is turning right
    private double mdblRobotTurnAngle;                       //used to determine angle the robot will turn
    private int mintLastEncoderDestinationLeft1;             //used to store the encoder destination from current Step
    private int mintLastEncoderDestinationLeft2;             //used to store the encoder destination from current Step
    private int mintLastEncoderDestinationRight1;            //used to store the encoder destination from current Step
    private int mintLastEncoderDestinationRight2;            //used to store the encoder destination from current Step
    private int mintLastPositionLeft1_1;
    private int mintLastPositionLeft2_1;
    private int mintLastPositionLeft1_2;
    private int mintLastPositionLeft2_2;
    private int mintLastPositionRight1_1;
    private int mintLastPositionRight2_1;
    private int mintLastPositionRight1_2;
    private int mintLastPositionRight2_2;
    private boolean blnMotor1Stall1 = false;
    private boolean blnStallTimerStarted = false;
    private boolean blnMotorStall1 = false;
    private boolean blnMotorStall2 = false;
    private boolean blnMotorStall3 = false;
    private boolean blnMotorStall4 = false;
    private Boolean stones[] = {true,true,true,true,true,true};

    private boolean mblnNextStepLastPos;                     //used to detect using encoders or previous calc'd position
    private int mintStepDelay;                               //used when decoding the step, this will indicate how long the delay is on ms.
    private boolean mblnDisableVisionProcessing = false;     //used when moving to disable vision to allow faster speed reading encoders.
    private int mintStepRetries = 0;                         //used to count retries on a step
    private ElapsedTime mStateTime = new ElapsedTime();      // Time into current state, used for the timeout
    private ElapsedTime mStateStalTimee = new ElapsedTime(); // Time into current state, used for the timeout
    private int mintStepNumber;
    private boolean flipit = false;
    private int quadrant;
    private int imuStartCorrectionVar = 0;
    private int imuMountCorrectionVar = 90;
    private boolean blnCrossZeroPositive = false;
    private boolean blnCrossZeroNegative = false;
    private boolean blnReverseDir = false;
    /**
     * Variables for the lift and remembering the current position
     */
    private int mintCurrentLiftCountMotor1          = 0;
    private int mintCurrentLiftCountMotor2          = 0;
    private int mintLiftStartCountMotor1            = 0;
    private int mintLiftStartCountMotor2            = 0;

    //hashmap for the steps to be stored in.  A Hashmap is like a fancy array
    //private HashMap<String, LibraryStateSegAutoRoverRuckus> autonomousSteps = new HashMap<String, LibraryStateSegAutoRoverRuckus>();
    private HashMap<String, String> powerTable = new HashMap<String, String>();
    private ReadStepFileXML autonomousStepsFile = new ReadStepFileXML();

    private UltimateGoalOCV elementColour = new UltimateGoalOCV();

    private ImageCaptureOCV imageCaptureOCV = new ImageCaptureOCV();
    //private LibraryTensorFlowRoverRuckus tensorFlowRoverRuckus = new LibraryTensorFlowRoverRuckus();

    private int mintNumberColourTries = 0;
    private Constants.ObjectColours mColour;

    private Constants.ObjectColours numberOfRings;
    private Constants.ObjectColours mLocation;

    private TOWR5291TextToSpeech towr5291TextToSpeech = new TOWR5291TextToSpeech(false);

    private TOWR5291PID PID1 = new TOWR5291PID();
    private TOWR5291PID PIDLEFT1 = new TOWR5291PID();
    private TOWR5291PID PIDLEFT2 = new TOWR5291PID();
    private TOWR5291PID PIDRIGHT1 = new TOWR5291PID();
    private TOWR5291PID PIDRIGHT2 = new TOWR5291PID();
    private int intdirection;
    private double dblStartVoltage = 0;

    private static TOWRDashBoard dashboard = null;

    public static TOWRDashBoard getDashboard() {
        return dashboard;
    }

    //each robot speeds up and slows down at different rates
    //helps reduce over runs and
    //table for the tilerunner from AndyMark.  These values are for the twin 20 motors which makes the robot fast
    private void loadPowerTableTileRunner() {
        powerTable.put(String.valueOf(0.5), ".1");
        powerTable.put(String.valueOf(1), ".2");
        powerTable.put(String.valueOf(2), ".3");
        powerTable.put(String.valueOf(4), ".4");
        powerTable.put(String.valueOf(6), ".5");
        powerTable.put(String.valueOf(8), ".6");
        powerTable.put(String.valueOf(10), ".7");
        powerTable.put(String.valueOf(12), ".8");
    }

    //table for the custom tanktread robot.  These values are for the twin 40 motors
    private void loadPowerTableTankTread() {
        powerTable.put(String.valueOf(0.5), ".3");
        powerTable.put(String.valueOf(1), ".3");
        powerTable.put(String.valueOf(2), ".4");
        powerTable.put(String.valueOf(4), ".5");
        powerTable.put(String.valueOf(6), ".5");
        powerTable.put(String.valueOf(8), ".6");
        powerTable.put(String.valueOf(10), ".6");
        powerTable.put(String.valueOf(12), ".6");
        powerTable.put(String.valueOf(15), ".8");
    }

    @Override
    public void runOpMode() throws InterruptedException {

        dashboard = TOWRDashBoard.createInstance(telemetry);
        dashboard = TOWRDashBoard.getInstance();

        FtcRobotControllerActivity activity = (FtcRobotControllerActivity) hardwareMap.appContext;

        dashboard.setTextView((TextView) activity.findViewById(R.id.textOpMode));
        dashboard.clearDisplay();
        dashboard.displayPrintf(0, LABEL_WIDTH, "Text: ", "*** Robot Data ***");
        //start the logging

        //create logging based on initial settings, sharepreferences will adjust levels
        fileLogger = new FileLogger(runtime, 1, true);
        fileLogger.open();
        fileLogger.write("Time,SysMS,Thread,Event,Desc");
        fileLogger.setEventTag("runOpMode()");
        fileLogger.writeEvent("Log Started");
        runtime.reset();
        dashboard.displayPrintf(1, "FileLogger: Started");

        //load menu settings and setup robot and debug level
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(hardwareMap.appContext);
        ourRobotConfig = new robotConfig(sharedPreferences);
        debug = ourRobotConfig.getDebug();

        switch (ourRobotConfig.getAllianceStartPosition()){
            case "Left":
                imuStartCorrectionVar = 0;
                break;
            default:
                imuStartCorrectionVar = 0;
                break;
        }

        //adjust debug level based on saved settings
        fileLogger.setDebugLevel(debug);

        fileLogger.writeEvent(1, "robotConfigTeam #       " + ourRobotConfig.getTeamNumber());
        fileLogger.writeEvent(1, "Alliance Colour         " + ourRobotConfig.getAllianceColor());
        fileLogger.writeEvent(1, "Alliance Start Pos      " + ourRobotConfig.getAllianceStartPosition());
        fileLogger.writeEvent(1, "Alliance Delay          " + ourRobotConfig.getDelay());
        fileLogger.writeEvent(1, "Robot Config Base       " + ourRobotConfig.getRobotConfigBase());
        fileLogger.writeEvent(1, "Robot Motor Type        " + ourRobotConfig.getRobotMotorType());
        fileLogger.writeEvent(1, "Robot Motor Ratio       " + ourRobotConfig.getRobotMotorRatio());
        fileLogger.writeEvent(1, "Robot Motor Direction   " + ourRobotConfig.getRobotMotorDirection());
        fileLogger.writeEvent(1, "Robot Motor Counts PR   " + ourRobotConfig.getCounts_Per_Rev());
        fileLogger.writeEvent(1, "Configuring Robot Parameters - Finished");
        fileLogger.writeEvent(1, "Loading Autonomous Steps - Start");

        dashboard.displayPrintf(1, "initRobot Loading Steps " + ourRobotConfig.getAllianceColor() + " Team " + ourRobotConfig.getTeamNumber());
        dashboard.displayPrintf(2, "initRobot SharePreferences!");
        dashboard.displayPrintf(3, "robotConfigTeam #     " + ourRobotConfig.getTeamNumber());
        dashboard.displayPrintf(4, "Alliance              " + ourRobotConfig.getAllianceColor());
        dashboard.displayPrintf(5, "Start Pos             " + ourRobotConfig.getAllianceStartPosition());
        dashboard.displayPrintf(6, "Start Del             " + ourRobotConfig.getDelay());
        dashboard.displayPrintf(7, "Robot Base            " + ourRobotConfig.getRobotConfigBase());
        dashboard.displayPrintf(8, "Robot Motor Type      " + ourRobotConfig.getRobotMotorType());
        dashboard.displayPrintf(9, "Robot Motor Ratio      " + ourRobotConfig.getRobotMotorRatio());
        dashboard.displayPrintf(10, "Robot Motor Direction " + ourRobotConfig.getRobotMotorDirection());
        dashboard.displayPrintf(11, "Robot Motor Counts PR " + ourRobotConfig.getCounts_Per_Rev());
        dashboard.displayPrintf(12, "Debug Level           " + debug);

        //load the sequence based on alliance colour and team
        autonomousStepsFile.ReadStepFile(ourRobotConfig);

        //need to load initial step of a delay based on user input
        autonomousStepsFile.insertSteps(ourRobotConfig.getDelay() + 1, "DELAY", 0, 0, false, false,ourRobotConfig.getDelay() * 1000, 0, 0, 0, 0, 0, 1);

        dashboard.displayPrintf(10, "initRobot STEPS LOADED");

        fileLogger.writeEvent(3, "Loading Autonomous Steps - Finished");
        fileLogger.writeEvent(3, "Configuring Adafruit IMU - Start");
        towr5291TextToSpeech.Speak("Step File Loaded!", debug);

        dashboard.displayPrintf(10, "initRobot IMU Loading");

        // Set up the parameters with which we will use our IMU. Note that integration
        // algorithm here just reports accelerations to the logcat log; it doesn't actually
        // provide positional information.
        towr5291TextToSpeech.Speak("Loading IMU Gyro", debug);
        BNO055IMU.Parameters parametersAdafruitImu = new BNO055IMU.Parameters();
        parametersAdafruitImu.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parametersAdafruitImu.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parametersAdafruitImu.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parametersAdafruitImu.loggingEnabled = true;
        parametersAdafruitImu.loggingTag = "IMU";
        parametersAdafruitImu.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parametersAdafruitImu);

        dashboard.displayPrintf(10, "initRobot IMU Configured");

        fileLogger.writeEvent(3, "Configuring Adafruit IMU - Finished");
        fileLogger.writeEvent(3, "Configuring Motors Base - Start");

        dashboard.displayPrintf(10, "initRobot BaseDrive Loading");

        dashboard.displayPrintf(10, "initRobot Sensors Loading");
        //init all the sensors
        sensors.init(hardwareMap);

        dashboard.displayPrintf(10, "initRobot BaseDrive Loading");

        robotDrive.init(fileLogger, hardwareMap, robotConfigSettings.robotConfigChoice.valueOf(ourRobotConfig.getRobotConfigBase()), LibraryMotorType.MotorTypes.valueOf(ourRobotConfig.getRobotMotorType()));
        robotDrive.setHardwareDriveResetEncoders();
        robotDrive.setHardwareDriveRunUsingEncoders();
        robotDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        getMotorPIDFMotor1 = robotDrive.getMotorPIDF(1);
        getMotorPIDFMotor2 = robotDrive.getMotorPIDF(2);
        getMotorPIDFMotor3 = robotDrive.getMotorPIDF(3);
        getMotorPIDFMotor4 = robotDrive.getMotorPIDF(4);
        newMotorPIDFMotor1 = getMotorPIDFMotor1;
        newMotorPIDFMotor2 = getMotorPIDFMotor2;
        newMotorPIDFMotor3 = getMotorPIDFMotor3;
        newMotorPIDFMotor4 = getMotorPIDFMotor4;
        fileLogger.writeEvent("(orig Motor 1) P " + getMotorPIDFMotor1.p + ", I " + getMotorPIDFMotor1.i + ", D " + getMotorPIDFMotor1.d + ", F " + getMotorPIDFMotor1.f);
        fileLogger.writeEvent("(orig Motor 2) P " + getMotorPIDFMotor2.p + ", I " + getMotorPIDFMotor2.i + ", D " + getMotorPIDFMotor2.d + ", F " + getMotorPIDFMotor2.f);
        fileLogger.writeEvent("(orig Motor 3) P " + getMotorPIDFMotor3.p + ", I " + getMotorPIDFMotor3.i + ", D " + getMotorPIDFMotor3.d + ", F " + getMotorPIDFMotor3.f);
        fileLogger.writeEvent("(orig Motor 4) P " + getMotorPIDFMotor4.p + ", I " + getMotorPIDFMotor4.i + ", D " + getMotorPIDFMotor4.d + ", F " + getMotorPIDFMotor4.f);
        newMotorPIDFMotor1.p = getMotorPIDFMotor1.p + 20.5;
        newMotorPIDFMotor2.p = getMotorPIDFMotor2.p + 20.5;
        newMotorPIDFMotor3.p = getMotorPIDFMotor3.p + 20.5;
        newMotorPIDFMotor4.p = getMotorPIDFMotor4.p + 20.5;
        newMotorPIDFMotor1.i = getMotorPIDFMotor1.i + 0.037;
        newMotorPIDFMotor2.i = getMotorPIDFMotor2.i + 0.037;
        newMotorPIDFMotor3.i = getMotorPIDFMotor3.i + 0.037;
        newMotorPIDFMotor4.i = getMotorPIDFMotor4.i + 0.037;
        newMotorPIDFMotor1.d = getMotorPIDFMotor1.d;
        newMotorPIDFMotor2.d = getMotorPIDFMotor2.d;
        newMotorPIDFMotor3.d = getMotorPIDFMotor3.d;
        newMotorPIDFMotor4.d = getMotorPIDFMotor4.d;
        newMotorPIDFMotor1.f = 0;//getMotorPIDFMotor1.f + 2;
        newMotorPIDFMotor2.f = 0;//getMotorPIDFMotor2.f + 2;
        newMotorPIDFMotor3.f = 0;//getMotorPIDFMotor3.f + 2;
        newMotorPIDFMotor4.f = 0;//getMotorPIDFMotor4.f + 2;

        //set new PIDF Values
        robotDrive.setMotorPIDF(newMotorPIDFMotor1,1);
        robotDrive.setMotorPIDF(newMotorPIDFMotor2,2);
        robotDrive.setMotorPIDF(newMotorPIDFMotor3,3);
        robotDrive.setMotorPIDF(newMotorPIDFMotor4,4);

        getMotorPIDFMotor1 = robotDrive.getMotorPIDF(1);
        getMotorPIDFMotor2 = robotDrive.getMotorPIDF(2);
        getMotorPIDFMotor3 = robotDrive.getMotorPIDF(3);
        getMotorPIDFMotor4 = robotDrive.getMotorPIDF(4);
        fileLogger.writeEvent("(new Motor 1) P " + getMotorPIDFMotor1.p + ", I " + getMotorPIDFMotor1.i + ", D " + getMotorPIDFMotor1.d + ", F " + getMotorPIDFMotor1.f);
        fileLogger.writeEvent("(new Motor 2) P " + getMotorPIDFMotor2.p + ", I " + getMotorPIDFMotor2.i + ", D " + getMotorPIDFMotor2.d + ", F " + getMotorPIDFMotor2.f);
        fileLogger.writeEvent("(new Motor 3) P " + getMotorPIDFMotor3.p + ", I " + getMotorPIDFMotor3.i + ", D " + getMotorPIDFMotor3.d + ", F " + getMotorPIDFMotor3.f);
        fileLogger.writeEvent("(new Motor 4) P " + getMotorPIDFMotor4.p + ", I " + getMotorPIDFMotor4.i + ", D " + getMotorPIDFMotor4.d + ", F " + getMotorPIDFMotor4.f);


        fileLogger.writeEvent(1, "robotConfigTeam #       " + ourRobotConfig.getTeamNumber());
        fileLogger.writeEvent(1, "Alliance Colour         " + ourRobotConfig.getAllianceColor());
        fileLogger.writeEvent(1, "Alliance Start Pos      " + ourRobotConfig.getAllianceStartPosition());

        dashboard.displayPrintf(10, "initRobot BaseDrive Loaded");
        fileLogger.writeEvent(3, "Configuring Motors Base - Finish");

        dashboard.displayPrintf(10, "Configuring Arm Motors - Start");
        fileLogger.writeEvent(3, "Configuring Arm Motors - Start");

        robotArms.init(hardwareMap , dashboard);
        robotArms.setHardwareLiftMotorResetEncoders();
        robotArms.setHardwareLiftMotorRunUsingEncoders();
        robotArms.setHardwareArmDirections();
        fileLogger.writeEvent(3, "Configuring Arm Motors - Finish");
        dashboard.displayPrintf(10, "Configuring Arm Motors - Finish");

        //sensor.init(hardwareMap);
        fileLogger.writeEvent(3, "Resetting State Engine - Start");

        initDefaultStates();

        mblnNextStepLastPos = false;

        towr5291TextToSpeech.Speak("Loading OpenCV & Vuforia", debug);
        //init openCV
        initOpenCv();
        dashboard.displayPrintf(1, "initRobot OpenCV!");
        fileLogger.writeEvent(3, "OpenCV Started");

        //load all the vuforia stuff
        LibraryVuforiaUltimateGoal UltimateGoalVuforia = new LibraryVuforiaUltimateGoal();
        VuforiaTrackables UltimateGoalTrackables;

        if (vuforiaWebcam) {
            robotWebcam = hardwareMap.get(WebcamName.class, "Webcam1");
            UltimateGoalTrackables = UltimateGoalVuforia.LibraryVuforiaUltimateGoal(hardwareMap, ourRobotConfig, robotWebcam, false);
        } else{
            UltimateGoalTrackables = UltimateGoalVuforia.LibraryVuforiaUltimateGoal(hardwareMap, ourRobotConfig, robotWebcam, false);
        }

        imageCaptureOCV.initImageCaptureOCV(UltimateGoalVuforia, dashboard, fileLogger);
        //tensorFlowRoverRuckus.initTensorFlow(RoverRuckusVuforia.getVuforiaLocalizer(), hardwareMap, fileLogger, "RoverRuckus.tflite", "GOLD", "SILVER", true);

        fileLogger.writeEvent(3,"MAIN","Configured Vuforia - About to Activate");
        dashboard.displayPrintf(10, "Configured Vuforia - About to Activate");

        //activate vuforia
        UltimateGoalTrackables.activate();

        fileLogger.writeEvent(3,"MAIN", "Activated Vuforia");

        towr5291TextToSpeech.Speak("Completed Loading, Waiting for Start", debug);
        dashboard.displayPrintf(10, "Init - Complete, Wait for Start");

        imu.stopAccelerationIntegration();

        dblStartVoltage = getBatteryVoltage();

        //move the right block arm to a stashed position
        //start position for within 18 inches
 //       robotArms.rightWristServo.setPosition(1);
 //       robotArms.rightArmServo.setPosition(0.0);
 //       robotArms.rightClampServo.setPosition(0.15);
 //       robotArms.leftWristServo.setPosition(0.05);
 //       robotArms.leftArmServo.setPosition(0.0);
 //       robotArms.leftClampServo.setPosition(0.15);
        robotArms.foundationServo.setPosition(1);
        robotArms.liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robotArms.liftMotor1.setPower(0);

 //       Experiment to keep lift from falling during initial drive step
        robotArms.liftMotor1.setTargetPosition(0);
        fileLogger.writeEvent("target position set");
        robotArms.setHardwareLiftPower(1.0);
        fileLogger.writeEvent("lift power set");
        robotArms.setHardwareLiftMotorRunToPosition();
        fileLogger.writeEvent("Run to position set");

        //used for SkyStone Detection
        mLocation = Constants.ObjectColours.OBJECT_NONE;

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        fileLogger.setEventTag("opModeIsActive()");
        dashboard.clearDisplay();

        imu.startAccelerationIntegration(new Position(), new Velocity(), 500);

        //the main loop.  this is where the action happens
        while (opModeIsActive()) {

            switch (mintCurrentStateStep) {
                case STATE_INIT:
                    fileLogger.writeEvent(1,"mintCurrentStateStep:- " + mintCurrentStateStep + " mintCurrentStateStep " + mintCurrentStateStep);
                    fileLogger.writeEvent(1,"About to check if step exists " + mintCurrentStep);

                    // get step from hashmap, send it to the initStep for decoding
                    if (autonomousStepsFile.activeSteps().containsKey(String.valueOf(mintCurrentStep))) {
                        fileLogger.writeEvent(1,"Step Exists TRUE " + mintCurrentStep + " about to get the values from the step");
                        initStep();
                        mintCurrentStateStep = Constants.stepState.STATE_RUNNING;
                    } else {
                        mintCurrentStateStep = Constants.stepState.STATE_FINISHED;
                    }
                    break;
                case STATE_START:
                    mintCurrentStateStep = Constants.stepState.STATE_RUNNING;
                    break;
                case STATE_RUNNING:

                    //load all the parallel steps so they can be evaluated for completeness
                    loadParallelSteps();

                    //Process all the parallel steps
                    for (String stKey : mintActiveStepsCopy.keySet()) {
                        fileLogger.writeEvent(1, "STATE_RUNNING", "Looping through Parallel steps, found " + stKey);
                        mintStepNumber = mintActiveStepsCopy.get(stKey);
                        loadActiveStep(mintStepNumber);
                        fileLogger.writeEvent(1, "STATE_RUNNING", "About to run " + mstrRobotCommand);
                        processSteps(mstrRobotCommand);
                    }

                    //Check the status of all the steps if all the states are complete we can move to the next state
                    if (checkAllStatesComplete()) {
                        mintCurrentStateStep = Constants.stepState.STATE_COMPLETE;
                    }

                    //make sure we load the current step to determine if parallel, if the steps are run out of order and a previous step was parallel
                    //things get all messed up and a step that isn't parallel can be assumed to be parallel
                    loadActiveStep(mintCurrentStep);
                    if (mblnParallel) {
                        // mark this step as complete and do next one, the current step should continue to run.  Not all steps are compatible with being run in parallel
                        // like drive steps, turns etc
                        // Drive forward and shoot
                        // Drive forward and detect beacon
                        // are examples of when parallel steps should be run
                        // errors will occur if other combinations are run
                        // only go to next step if current step equals the one being processed for parallelism.
                        for (String stKey : mintActiveStepsCopy.keySet()) {
                            mintStepNumber = mintActiveStepsCopy.get(stKey);
                            if (mintCurrentStep == mintStepNumber)
                                mintCurrentStateStep = Constants.stepState.STATE_COMPLETE;
                        }
                    }
                    break;
                case STATE_PAUSE:
                    break;
                case STATE_COMPLETE:
                    fileLogger.writeEvent(1,"Step Complete - Current Step:- " + mintCurrentStep);
                    //  Transition to a new state and next step.
                    mintCurrentStep++;
                    mintCurrentStateStep = Constants.stepState.STATE_INIT;
                    break;
                case STATE_TIMEOUT:
                    robotArms.intakeMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotArms.liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotArms.intakeMotor1.setPower(0);
                    robotArms.liftMotor1.setPower(0);
                    robotDrive.setHardwareDrivePower(0);
                    //  Transition to a new state.
                    mintCurrentStateStep = Constants.stepState.STATE_FINISHED;
                    break;
                case STATE_ERROR:
                    dashboard.displayPrintf(1, LABEL_WIDTH,"STATE", "ERROR WAITING TO FINISH " + mintCurrentStep);
                    break;
                case STATE_FINISHED:
                    robotArms.intakeMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotArms.liftMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotArms.intakeMotor1.setPower(0);
                    robotArms.liftMotor1.setPower(0);
                    robotDrive.setHardwareDrivePower(0);
                    robotDrive.setHardwareDriveRunWithoutEncoders();
                    robotDrive.setHardwareDrivePower(0);
                    imu.close();
                    //stop the logging
                    if (fileLogger != null) {
                        fileLogger.writeEvent(1, "Step FINISHED - FINISHED");
                        fileLogger.writeEvent(1, "Stopped");
                        Log.d("END:-", "FileLogger Stopped");
                        fileLogger.close();
                        fileLogger = null;
                    }
                    dashboard.displayPrintf(1, LABEL_WIDTH,"STATE", "FINISHED " + mintCurrentStep);
                    break;
            }
        }
        if (fileLogger != null) {
            fileLogger.writeEvent(1, "FINISHED AUTON - TIMED OUT");
            Log.d("END:-", "FINISHED AUTON - TIMED OUT - logger stopped");
            fileLogger.close();
            fileLogger = null;
        }

        //tensorFlowRoverRuckus.shutdown();

        //switch opmode to teleop
        //opModeManager = (OpModeManagerImpl) onStop.internalOpModeServices;
        //opModeManager.initActiveOpMode(TeleOpMode);
        //opmode not active anymore
    }

    private void loadActiveStep(int step) {
        fileLogger.setEventTag("loadActiveStep()");
        LibraryStateSegAutoRoverRuckus mStateSegAuto = autonomousStepsFile.activeSteps().get(String.valueOf(step));
        fileLogger.writeEvent(1,"Got the values for step " + step + " about to decode");

        mdblStep            = mStateSegAuto.getmStep();
        mdblStepTimeout     = mStateSegAuto.getmRobotTimeOut();
        mstrRobotCommand    = mStateSegAuto.getmRobotCommand();
        mdblStepDistance    = mStateSegAuto.getmRobotDistance();
        mdblStepSpeed       = mStateSegAuto.getmRobotSpeed();
        mblnParallel        = mStateSegAuto.getmRobotParallel();
        mblnRobotLastPos    = mStateSegAuto.getmRobotLastPos();
        mdblRobotParm1      = mStateSegAuto.getmRobotParm1();
        mdblRobotParm2      = mStateSegAuto.getmRobotParm2();
        mdblRobotParm3      = mStateSegAuto.getmRobotParm3();
        mdblRobotParm4      = mStateSegAuto.getmRobotParm4();
        mdblRobotParm5      = mStateSegAuto.getmRobotParm5();
        mdblRobotParm6      = mStateSegAuto.getmRobotParm6();
    }

    private void loadParallelSteps() {
        fileLogger.setEventTag("loadParallelSteps()");
        mintActiveStepsCopy.clear();
        for (String stKey : mintActiveSteps.keySet()) {
            fileLogger.writeEvent(2,"Loading Active Parallel Step " + stKey);
            mintActiveStepsCopy.put(stKey, mintActiveSteps.get(stKey));
        }
    }

    private void deleteParallelStep() {
        fileLogger.setEventTag("deleteParallelStep()");
        for (String stKey : mintActiveStepsCopy.keySet()) {
            int tempStep = mintActiveStepsCopy.get(stKey);
            if (mintStepNumber == tempStep) {
                fileLogger.writeEvent(2,"Removing Parallel Step " + tempStep);
                if (mintActiveSteps.containsKey(stKey))
                    mintActiveSteps.remove(stKey);
            }
        }
    }

    private void processSteps(String stepName) {
        fileLogger.setEventTag("processSteps()");
        fileLogger.writeEvent(2,"Processing Parallel Step " + stepName);
        switch (stepName.toUpperCase()) {
            case "DELAY":
                DelayStep();
                break;
            case "FINDGOLDSS":
                findGoldSS();
                break;
            case "TANKTURN":
                TankTurnStep();
                break;
            case "TANKTURNGYRO":
                TankTurnGyroHeading();
                break;
            case "TANKTURNGYROENCODER":
                TankTurnGyroHeadingEncoder();
                break;
            case "LPE":
            case "RPE":
                PivotTurnStep();
                break;
            case "STRAFE":
                MecanumStrafe();
                //MecanumStrafeTime();
                break;
            case "RADIUSTURN":
                RadiusTurnStep();
                break;
            case "DRIVE":  // Drive forward a distance in inches and power setting
                DriveStepHeading();
                break;
            case "VME":  // Move the robot using localisation from the targets
                VuforiaMove();
                break;
            case "VTE":  // Turn the Robot using information from Vuforia and Pythag
                VuforiaTurn();
                break;
            case "LIFT":    // Moves the lift up and down for the 2018-19 game
                moveLiftUpDown();
                break;
            case "INTAKE":
                SetIntake();
                break;
            case "NEXTSTONE":
                nextStone();
                break;
            case "WYATTGYRO":
                WyattsGyroDrive();
                break;
            case "CLAW":
                clawMovement();
                break;
            case "EJECTOR":
                ejector();
                break;
            case "FLYWHEEL":
                flywheel();
                break;
            case "STONEARM":
                grabBlock();
                break;
        }
    }

    //--------------------------------------------------------------------------
    //  Initialise the state.
    //--------------------------------------------------------------------------
    private void initStep() {
        fileLogger.setEventTag("initStep()");
        fileLogger.writeEvent(3,"Starting to Decode Step " + mintCurrentStep);

        if (!(mintActiveSteps.containsValue(mintCurrentStep))) {
            mintActiveSteps.put(String.valueOf(mintCurrentStep), mintCurrentStep);
            fileLogger.writeEvent(3,"Put step into hashmap mintActiveSteps " + mintCurrentStep);
        }

        loadActiveStep(mintCurrentStep);
        // Reset the state time, and then change to next state.
        mStateTime.reset();

        switch (mstrRobotCommand.toUpperCase()) {
            case "DELAY":
                mintCurrentStepDelay                = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Delay", debug);
                break;
            case "FINDGOLDSS":
                mintCurrentStepFindGoldSS                = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Find Gold SS", debug);
                break;
            case "TANKTURNGYRO":
                mintCurrentStateTankTurnGyroHeading = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Tank Turn Gyro Heading", debug);
                break;
            case "STRAFE":
                mintCurrentStateMecanumStrafe       = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Mecanum Strafe", debug);
                break;
            case "TANKTURN":
                mintCurrentStateTankTurn            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Tank Turn", debug);
                break;
            case "TANKTURNGYROENCODER":
                mintCurrentStateGyroTurnEncoder5291  = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Tank Turn Gyro", debug);
                break;
            case "LPE":
                mintCurrentStatePivotTurn           = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Pivot Turn", debug);
                break;
            case "RPE":
                mintCurrentStatePivotTurn           = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Pivot Turn", debug);
                break;
            case "LRE":  // Left turn with a Radius in Parm 1
                mintCurrentStateRadiusTurn          = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Radius Turn", debug);
                break;
            case "RRE":  // Right turn with a Radius in Parm 1
                mintCurrentStateRadiusTurn          = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Radius Turn", debug);
                break;
            case "DRIVE":  // Drive forward a distance in inches and power setting
                mintCurrentStateDriveHeading        = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Drive Heading", debug);
                break;
            case "VME":  // Move the robot using localisation from the targets
                mintCurStVuforiaMove5291            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Vuforia Move", debug);
                break;
            case "VTE":  // Turn the Robot using information from Vuforia and Pythag
                mintCurStVuforiaTurn5291            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Vuforia Turn", debug);
                break;
            case "GTE":  // Special Function, 5291 Move forward until line is found
                mintCurrentStateGyroTurnEncoder5291 = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Gyro Turn Encoder", debug);
                break;
            case "EYE":  // Special Function, 5291 Move forward until line is found
                mintCurrentStateEyes5291            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Move Eyes", debug);
                break;
            case "LIFT":
                mintCurrentStateMoveLift            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Move Lift", debug);
                break;
            case "INTAKE":
                mintCurrentStateInTake              = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Set Intake", debug);
                break;
            case "NEXTSTONE":
                mintCurrentStateNextStone            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Find Skystone", debug);
                break;
            case "WYATTGYRO":
                mintCurrentStateWyattsGyroDrive     = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Running Wyatt's Gyro Drive", debug);
                break;
            case "FNC":  //  Run a special Function with Parms
                break;
            case "CLAW":
                mintCurrentStateClawMovement       = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Moving Claw", debug);
                break;
            case "EJECTOR":
                getMintCurrentStateEjector           = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Ejector", debug);
                break;
            case "FLYWHEEL":
                mintCurrentStateFlywheel            = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Flywheel", debug);
                break;
            case "STONEARM":
                mintCurrentStateGrabBlock          = Constants.stepState.STATE_INIT;
                towr5291TextToSpeech.Speak("Grab Block", debug);
                break;
        }

        fileLogger.writeEvent(2,"Current Step          :- " + mintCurrentStep);
        fileLogger.writeEvent(2, "initStep()", "Current Step          :- " + mintCurrentStep);
        fileLogger.writeEvent(2, "initStep()", "mdblStepTimeout       :- " + mdblStepTimeout);
        fileLogger.writeEvent(2, "initStep()", "mdblStepSpeed         :- " + mdblStepSpeed);
        fileLogger.writeEvent(2, "initStep()", "mstrRobotCommand      :- " + mstrRobotCommand);
        fileLogger.writeEvent(2, "initStep()", "mblnParallel          :- " + mblnParallel);
        fileLogger.writeEvent(2, "initStep()", "mblnRobotLastPos      :- " + mblnRobotLastPos);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm1        :- " + mdblRobotParm1);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm2        :- " + mdblRobotParm2);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm3        :- " + mdblRobotParm3);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm4        :- " + mdblRobotParm4);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm5        :- " + mdblRobotParm5);
        fileLogger.writeEvent(2, "initStep()", "mdblRobotParm6        :- " + mdblRobotParm6);
        fileLogger.writeEvent(2, "initStep()", "mdblStepDistance      :- " + mdblStepDistance);
        fileLogger.writeEvent(2, "initStep()", "mdblStepTurnL         :- " + mdblStepTurnL);
        fileLogger.writeEvent(2, "initStep()", "mdblStepTurnR         :- " + mdblStepTurnR);
    }

    private void DriveStepHeading() {
        fileLogger.setEventTag("DriveStepHeading()");

        double dblDistanceToEndLeft1;
        double dblDistanceToEndLeft2;
        double dblDistanceToEndRight1;
        double dblDistanceToEndRight2;
        double dblDistanceToEnd;
        double dblDistanceFromStartLeft1;
        double dblDistanceFromStartLeft2;
        double dblDistanceFromStartRight1;
        double dblDistanceFromStartRight2;
        double dblDistanceFromStart;
        int intLeft1MotorEncoderPosition;
        int intLeft2MotorEncoderPosition;
        int intRight1MotorEncoderPosition;
        int intRight2MotorEncoderPosition;
        double dblMaxSpeed;
        double dblError;
        double dblSteer;
        double dblLeftSpeed;
        double dblRightSpeed;

        switch (mintCurrentStateDriveHeading) {
            case STATE_INIT:
                // set motor controller to mode
                robotDrive.setHardwareDriveRunUsingEncoders();
                mblnDisableVisionProcessing = true;  //disable vision processing
                fileLogger.writeEvent(2,"mdblStepDistance   :- " + mdblStepDistance);
                // Determine new target position
                if (mblnNextStepLastPos) {
                    mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                    mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                    mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                    mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                } else {
                    mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                    mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                    mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                    mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                }

                fileLogger.writeEvent(2,"mStepLeftStart1 :- " + mintStartPositionLeft1 + " mStepLeftStart2 :- " + mintStartPositionLeft2);
                fileLogger.writeEvent(2,"mStepRightStart1:- " + mintStartPositionRight1 + " mStepRightStart2:- " + mintStartPositionRight2);

                mblnNextStepLastPos = false;

                mintStepLeftTarget1 = mintStartPositionLeft1 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH());
                mintStepLeftTarget2 = mintStartPositionLeft2 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH());
                mintStepRightTarget1 = mintStartPositionRight1 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH());
                mintStepRightTarget2 = mintStartPositionRight2 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH());

                //store the encoder positions so next step can calculate destination
                mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                // pass target position to motor controller
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                fileLogger.writeEvent(2,"mStepLeftTarget1 :- " + mintStepLeftTarget1 + " mStepLeftTarget2 :- " + mintStepLeftTarget2);
                fileLogger.writeEvent(2,"mStepRightTarget1:- " + mintStepRightTarget1 + " mStepRightTarget2:- " + mintStepRightTarget2);

                // set motor controller to mode, Turn On RUN_TO_POSITION
                robotDrive.setHardwareDriveRunToPosition();
                robotDrive.setHardwareDrivePower(Math.abs(mdblStepSpeed));

                dblStepSpeedTempRight = mdblStepSpeed;
                dblStepSpeedTempLeft = mdblStepSpeed;

                mintCurrentStateDriveHeading = Constants.stepState.STATE_RUNNING;
                break;

            case STATE_START:
                if (robotDrive.getHardwareBaseDriveBusy()) {
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_RUNNING;
                    fileLogger.writeEvent(2,"Base Drive Not Running Yet ");
                }
                robotDrive.setHardwareDrivePower(Math.abs(mdblStepSpeed));
                //check timeout value
                if (mStateTime.seconds() > mdblStepTimeout) {// Stop all motion;
                    robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(1,"Timeout Drive didn't engage:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;

            case STATE_RUNNING:
                // pass target position to motor controller
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                robotDrive.setHardwareDrivePower(Math.abs(mdblStepSpeed));

                intLeft1MotorEncoderPosition = robotDrive.baseMotor1.getCurrentPosition();
                intLeft2MotorEncoderPosition = robotDrive.baseMotor2.getCurrentPosition();
                intRight1MotorEncoderPosition = robotDrive.baseMotor3.getCurrentPosition();
                intRight2MotorEncoderPosition = robotDrive.baseMotor4.getCurrentPosition();

                // ramp up speed - need to write function to ramp up speed
                dblDistanceFromStartLeft1 = Math.abs(mintStartPositionLeft1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceFromStartLeft2 = Math.abs(mintStartPositionLeft2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceFromStartRight1 = Math.abs(mintStartPositionRight1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceFromStartRight2 = Math.abs(mintStartPositionRight2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();

                //if moving ramp up
                dblDistanceFromStart = (dblDistanceFromStartLeft1 + dblDistanceFromStartRight1 + dblDistanceFromStartLeft2 + dblDistanceFromStartRight2) / 4;

                //determine how close to target we are
                //dblDistanceToEndLeft1 = (mintStepLeftTarget1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                //dblDistanceToEndLeft2 = (mintStepLeftTarget2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                //dblDistanceToEndRight1 = (mintStepRightTarget1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                //dblDistanceToEndRight2 = (mintStepRightTarget2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();

                //if getting close ramp down speed
                //dblDistanceToEnd = Math.max(Math.max(Math.max(Math.abs(dblDistanceToEndLeft1),Math.abs(dblDistanceToEndRight1)),Math.abs(dblDistanceToEndLeft2)),Math.abs(dblDistanceToEndRight2));
                dblDistanceToEnd = Math.abs(Math.abs(mdblStepDistance) - Math.abs(dblDistanceFromStart));

                //parameter 1 use gyro for direction,  setting either of these to 1 will get gyro correction
                // if parameter 1 is true
                // parameter 2 is the heading
                // parameter 3 is the gain coefficient
                if ((mdblRobotParm1 == 1)) {
                    //use Gyro to run heading
                    // adjust relative speed based on heading error.
                    dblError = getDriveError(mdblRobotParm2);
                    dblSteer = getDriveSteer(dblError, mdblRobotParm3);
                    fileLogger.writeEvent(3,"dblError " + dblError);
                    fileLogger.writeEvent(3,"dblSteer " + dblSteer);
                    fileLogger.writeEvent(3, "runningDriveHeadingStep", "Heading " + mdblRobotParm2);

                    // if driving in reverse, the motor correction also needs to be reversed
                    if (mdblStepDistance > 0)
                        dblSteer *= -1.0;

                    dblStepSpeedTempLeft = dblStepSpeedTempLeft + dblSteer;
                    dblStepSpeedTempRight = dblStepSpeedTempRight - dblSteer;

                    // Normalize speeds if any one exceeds +/- 1.0;
                    dblMaxSpeed = Math.max(Math.abs(dblStepSpeedTempLeft), Math.abs(dblStepSpeedTempRight));
                    if (dblMaxSpeed > 1.0) {
                        dblStepSpeedTempLeft /= dblMaxSpeed;
                        dblStepSpeedTempRight /= dblMaxSpeed;
                    }
                }
                if (mdblRobotParm4 == 1) {
                    //find the first skystone, and move to the middle of it
                    if (findSkystone()) {
                        //calculate where
                        double frontDistance = ((sensors.distanceFrontLeftCM() + sensors.distanceFrontLeftCM() ) / 2);
                        fileLogger.writeEvent(3,"Stone 0 " + stones[0]);
                        fileLogger.writeEvent(3,"Stone 1 " + stones[1]);
                        fileLogger.writeEvent(3,"Stone 2 " + stones[2]);
                        fileLogger.writeEvent(3,"Stone 3 " + stones[3]);
                        fileLogger.writeEvent(3,"Stone 4 " + stones[4]);
                        fileLogger.writeEvent(3,"Stone 5 " + stones[5]);
                        if ((frontDistance > 96) && (frontDistance < 104)) {
                            mLocation = Constants.ObjectColours.OBJECT_SKYSTONE_LEFT;
                            fileLogger.writeEvent(3,"SkyStone LEFT " + (frontDistance));
                            stones[0] = false;
                            mboolFoundSkyStone = true;
                        } else if ((frontDistance > 79) && (frontDistance < 87)) {
                            mLocation = Constants.ObjectColours.OBJECT_SKYSTONE_CENTER;
                            fileLogger.writeEvent(3,"SkyStone CENTER " + (frontDistance));
                            stones[1] = false;
                            mboolFoundSkyStone = true;
                        } else if ((frontDistance > 59) && (frontDistance < 67)) {
                            mLocation = Constants.ObjectColours.OBJECT_SKYSTONE_RIGHT;
                            fileLogger.writeEvent(3,"SkyStone RIGHT " + (frontDistance));
                            stones[2] = false;
                            mboolFoundSkyStone = true;
                        } else if ((frontDistance > 39) && (frontDistance < 49)) {
                            mLocation = Constants.ObjectColours.OBJECT_SKYSTONE_LEFT;
                            fileLogger.writeEvent(3,"SkyStone RIGHT " + (frontDistance));
                            stones[3] = false;
                            mboolFoundSkyStone = true;
                        } else {
                            mboolFoundSkyStone = false;
                        }
                        if (mboolFoundSkyStone) {
                            fileLogger.writeEvent(3,"Stone 0 " + stones[0]);
                            fileLogger.writeEvent(3,"Stone 1 " + stones[1]);
                            fileLogger.writeEvent(3,"Stone 2 " + stones[2]);
                            fileLogger.writeEvent(3,"Stone 3 " + stones[3]);
                            fileLogger.writeEvent(3,"Stone 4 " + stones[4]);
                            fileLogger.writeEvent(3,"Stone 5 " + stones[5]);
                            fileLogger.writeEvent(3,"FoundSkyStone Exiting ");
                            robotDrive.setHardwareDrivePower(0);
                            /*robotDrive.baseMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robotDrive.baseMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robotDrive.baseMotor3.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robotDrive.baseMotor4.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robotDrive.baseMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            robotDrive.baseMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            robotDrive.baseMotor3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            robotDrive.baseMotor4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
*/
                            mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                            deleteParallelStep();
                            break;
                        }
                    }
                }

                //Parm 5 is stop when distance from wall is close enough.  this parm is the distance as well
                //based on the direction of travel
                if (mdblRobotParm5 > 0 ) {
                    //Stop at the distance from the wall
                    double wallDistance;
                    if (mdblStepDistance > 0) {
                        wallDistance = ((sensors.distanceFrontLeftIN() + sensors.distanceFrontLeftIN()) / 2);
                    } else {
                        wallDistance = ((sensors.distanceRearLeftIN() + sensors.distanceRearLeftIN() ) / 2);
                    }
                    fileLogger.writeEvent(3,"Distance From Wall " + wallDistance);

                    if (wallDistance <= mdblRobotParm5) {
                        fileLogger.writeEvent(3,"Distance Met Exiting ");
                        robotDrive.setHardwareDrivePower(0);
                        mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                } else if (mdblRobotParm5 < 0 ) {
                    //Stop at the distance from the wall
                    double wallDistance;
                    if (mdblStepDistance < 0) {
                        wallDistance = ((sensors.distanceFrontLeftIN() + sensors.distanceFrontLeftIN()) / 2);
                    } else {
                        wallDistance = ((sensors.distanceRearLeftIN() + sensors.distanceRearLeftIN() ) / 2);
                    }
                    fileLogger.writeEvent(3,"Distance From Rev Wall " + wallDistance);

                    if (wallDistance <= mdblRobotParm5) {
                        fileLogger.writeEvent(3,"Distance Met Rev Exiting ");
                        robotDrive.setHardwareDrivePower(0);
                        mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                }

                fileLogger.writeEvent(3,"dblDistanceToEnd " + dblDistanceToEnd);

                if (mblnRobotLastPos) {
                    if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                        mblnNextStepLastPos = true;
                        fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END " + Math.abs(dblDistanceToEnd));
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                } else if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                    fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END " + Math.abs(dblDistanceToEnd));
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                //stop driving when within .5 inch, sometimes takes a long time to get that last bit and times out.
                //stop when drive motors stop
                if (Math.abs(dblDistanceToEnd) <= 1) {
                    fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END " + Math.abs(dblDistanceToEnd));
                    robotDrive.setHardwareDrivePower(0);
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                } else if (robotDrive.getHardwareBaseDriveBusy()) {
                    fileLogger.writeEvent(3,"Encoder counts per inch = " + ourRobotConfig.getCOUNTS_PER_INCH() + " dblDistanceFromStart " + dblDistanceFromStart + " dblDistanceToEnd " + dblDistanceToEnd + " Power Level Left " + dblStepSpeedTempLeft + " Power Level Right " + dblStepSpeedTempRight + " Running to target  L1, L2, R1, R2  " + mintStepLeftTarget1 + ", " + mintStepLeftTarget2 + ", " + mintStepRightTarget1 + ",  " + mintStepRightTarget2 + ", " + " Running at position L1 " + intLeft1MotorEncoderPosition + " L2 " + intLeft2MotorEncoderPosition + " R1 " + intRight1MotorEncoderPosition + " R2 " + intRight2MotorEncoderPosition);
                    dashboard.displayPrintf(3, LABEL_WIDTH,"Path1", "Running to " + mintStepLeftTarget1 + ":" + mintStepRightTarget1);
                    dashboard.displayPrintf(4, LABEL_WIDTH,"Path2", "Running at " +intLeft1MotorEncoderPosition + ":" + intRight1MotorEncoderPosition);
                    dashboard.displayPrintf(5, LABEL_WIDTH,"Path3", "Running at " + intLeft2MotorEncoderPosition + ":" + intRight2MotorEncoderPosition);
                    // set power on motor controller to update speeds
                    robotDrive.setHardwareDriveLeftMotorPower(dblStepSpeedTempLeft);
                    robotDrive.setHardwareDriveRightMotorPower(dblStepSpeedTempRight);
                } else {
                    //robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(2,"Complete         ");
                    mblnDisableVisionProcessing = false;  //enable vision processing
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                //check timeout value
                if (mStateTime.seconds() > mdblStepTimeout) {// Stop all motion;
                    robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateDriveHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
                break;
        }
    }

    //--------------------------------------------------------------------------
    //  Execute the state.
    //--------------------------------------------------------------------------

    private void PivotTurnStep()  //should be same as radius turn with radius of 1/2 robot width, so this function can be deleted once radius turn is completed
    {
        fileLogger.setEventTag("PivotTurnStep()");

        double dblDistanceToEndLeft1;
        double dblDistanceToEndLeft2;
        double dblDistanceToEndRight1;
        double dblDistanceToEndRight2;
        int intLeft1MotorEncoderPosition;
        int intLeft2MotorEncoderPosition;
        int intRight1MotorEncoderPosition;
        int intRight2MotorEncoderPosition;

        switch (mintCurrentStatePivotTurn) {
            case STATE_INIT: {
                // set motor controller to mode
                robotDrive.setHardwareDriveRunUsingEncoders();
                mblnDisableVisionProcessing = true;  //disable vision processing
                mdblStepTurnL = 0;
                mdblStepTurnR = 0;

                if (mdblStepDistance > 0) {
                    mdblStepTurnL = mdblStepDistance;
                    mdblStepTurnR = 0;
                } else {
                    mdblStepTurnL = 0;
                    mdblStepTurnR = mdblStepDistance;
                }

                fileLogger.writeEvent(2,"mdblStepTurnL      :- " + mdblStepTurnL);
                fileLogger.writeEvent(2,"mdblStepTurnR      :- " + mdblStepTurnR);

                // Turn On RUN_TO_POSITION
                if (mdblStepTurnR == 0) {
                    // Determine new target position
                    fileLogger.writeEvent(2,"Current LPosition:-" + robotDrive.baseMotor1.getCurrentPosition());

                    // Get Current Encoder positions
                    if (mblnNextStepLastPos) {
                        mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                        mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                        mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                        mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                    } else {
                        mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                        mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                        mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                        mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                    }
                    mblnNextStepLastPos = false;

                    // Determine new target position
                    mintStepLeftTarget1 = mintStartPositionLeft1 + (int) (mdblStepTurnL * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (mdblStepTurnL * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget1 = mintStartPositionRight1 + (int) (mdblStepTurnR * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget2 = mintStartPositionRight2 + (int) (mdblStepTurnR * ourRobotConfig.getCOUNTS_PER_DEGREE());

                    //store the encoder positions so next step can calculate destination
                    mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                    mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                    mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                    mintLastEncoderDestinationRight2 = mintStepRightTarget2;
                    fileLogger.writeEvent(2,"mintStepLeftTarget1:-  " + mintStepLeftTarget1 + " mintStepLeftTarget2:-  " + mintStepLeftTarget2);

                    // pass target position to motor controller
                    robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                    robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);

                    // set left motor controller to mode
                    robotDrive.setHardwareDriveLeftRunToPosition();

                    // set power on motor controller to start moving
                    robotDrive.setHardwareDriveLeftMotorPower(Math.abs(mdblStepSpeed));
                } else {
                    // Determine new target position
                    fileLogger.writeEvent(2,"Current RPosition:-" + robotDrive.baseMotor3.getCurrentPosition());

                    // Get Current Encoder positions
                    if (mblnNextStepLastPos) {
                        mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                        mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                        mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                        mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                    } else {
                        mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                        mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                        mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                        mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                    }
                    mblnNextStepLastPos = false;

                    // Determine new target position
                    mintStepLeftTarget1 = mintStartPositionLeft1 + (int) (mdblStepTurnL * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (mdblStepTurnL * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget1 = mintStartPositionRight1 + (int) (mdblStepTurnR * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget2 = mintStartPositionRight2 + (int) (mdblStepTurnR * ourRobotConfig.getCOUNTS_PER_DEGREE());

                    //store the encoder positions so next step can calculate destination
                    mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                    mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                    mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                    mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                    fileLogger.writeEvent(3,"mintStepRightTarget1:- " + mintStepRightTarget1 + " mintStepRightTarget2:- " + mintStepRightTarget2);

                    // pass target position to motor controller
                    robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                    robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                    // set right motor controller to mode, Turn On RUN_TO_POSITION
                    robotDrive.setHardwareDriveRightRunToPosition();

                    // set power on motor controller to start moving
                    robotDrive.setHardwareDriveRightMotorPower(Math.abs(mdblStepSpeed));
                }

                //store the encoder positions so next step can calculate destination
                mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                fileLogger.writeEvent(3,"gblStepLeftTarget:- " + mintStepLeftTarget1 + " mintStepLeftTarget2 :- " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"gblStepRightTarget:- " + mintStepRightTarget1 + " mintStepRightTarget2:- " + mintStepRightTarget2);
                mintCurrentStatePivotTurn = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {

                intLeft1MotorEncoderPosition = robotDrive.baseMotor1.getCurrentPosition();
                intLeft2MotorEncoderPosition = robotDrive.baseMotor2.getCurrentPosition();
                intRight1MotorEncoderPosition = robotDrive.baseMotor3.getCurrentPosition();
                intRight2MotorEncoderPosition = robotDrive.baseMotor4.getCurrentPosition();

                //determine how close to target we are
                dblDistanceToEndLeft1 = (mintStepLeftTarget1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndLeft2 = (mintStepLeftTarget2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight1 = (mintStepRightTarget1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight2 = (mintStepRightTarget2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();

                fileLogger.writeEvent(3,"Current LPosition1:-" + robotDrive.baseMotor1.getCurrentPosition() + " LTarget:- " + mintStepLeftTarget1 + " LPosition2:-" + robotDrive.baseMotor2.getCurrentPosition() + " LTarget2:- " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"Current RPosition1:-" + robotDrive.baseMotor3.getCurrentPosition() + " RTarget:- " + mintStepRightTarget1 + " RPosition2:-" + robotDrive.baseMotor4.getCurrentPosition() + " RTarget2:- " + mintStepRightTarget2);

                if (mdblStepTurnR == 0) {
                    fileLogger.writeEvent(3,"Running.......");
                    dashboard.displayPrintf(3, LABEL_WIDTH, "Target", "Running to %7d :%7d", mintStepLeftTarget1, mintStepRightTarget1);
                    dashboard.displayPrintf(4, LABEL_WIDTH,"Actual_Left", "Running at %7d :%7d", intLeft1MotorEncoderPosition, intLeft2MotorEncoderPosition);
                    dashboard.displayPrintf(5, LABEL_WIDTH,"ActualRight", "Running at %7d :%7d", intRight1MotorEncoderPosition, intRight2MotorEncoderPosition);

                    if (mblnRobotLastPos) {
                        if (((dblDistanceToEndLeft1 + dblDistanceToEndLeft2) / 2) < 2.0) {
                            mblnNextStepLastPos = true;
                            mblnDisableVisionProcessing = false;  //enable vision processing
                            mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                            deleteParallelStep();
                        }
                    }
                    //if (!robotDrive.leftMotor1.isBusy()) {
                    //get motor busy state bitmap is right2, right1, left2, left1
                    if (((robotDrive.getHardwareDriveIsBusy() & robotConfig.motors.leftMotor1.toInt()) == robotConfig.motors.leftMotor1.toInt())) {
                        fileLogger.writeEvent(1,"Complete........");
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                    }
                } else if (mdblStepTurnL == 0) {
                    fileLogger.writeEvent(3,"Running.......");
                    dashboard.displayPrintf(3, LABEL_WIDTH,"Target", "Running to %7d :%7d", mintStepLeftTarget1, mintStepRightTarget1);
                    dashboard.displayPrintf(4, LABEL_WIDTH,"Actual_Left", "Running at %7d :%7d", intLeft1MotorEncoderPosition, intLeft2MotorEncoderPosition);
                    dashboard.displayPrintf(5, LABEL_WIDTH,"ActualRight", "Running at %7d :%7d", intRight1MotorEncoderPosition, intRight2MotorEncoderPosition);

                    if (mblnRobotLastPos) {
                        if (((dblDistanceToEndRight1 + dblDistanceToEndRight2) / 2) < 2.2) {
                            mblnNextStepLastPos = true;
                            mblnDisableVisionProcessing = false;  //enable vision processing
                            mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                            deleteParallelStep();
                            break;
                        }
                    }

                    //get motor busy state bitmap is right2, right1, left2, left1
                    if (!robotDrive.getHardwareBaseDriveBusy()) {
                        fileLogger.writeEvent(1,"Complete.......");
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                } else {
                    // Stop all motion by setting power to 0
                    //robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(1,"Complete.......");
                    mblnDisableVisionProcessing = false;  //enable vision processing
                    mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStatePivotTurn = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private void TankTurnStep() {
        fileLogger.setEventTag("TankTurnStep()");
        double dblDistanceToEndLeft1;
        double dblDistanceToEndLeft2;
        double dblDistanceToEndRight1;
        double dblDistanceToEndRight2;
        int intLeft1MotorEncoderPosition;
        int intLeft2MotorEncoderPosition;
        int intRight1MotorEncoderPosition;
        int intRight2MotorEncoderPosition;

        switch (mintCurrentStateTankTurn) {
            case STATE_INIT: {
                // set motor controller to mode
                robotDrive.setHardwareDriveRunUsingEncoders();

                mblnDisableVisionProcessing = true;  //disable vision processing

                mdblStepTurnL = 0;
                mdblStepTurnR = 0;

                // Get Current Encoder positions
                if (mblnNextStepLastPos) {
                    mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                    mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                    mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                    mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                } else {
                    mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                    mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                    mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                    mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                }
                mblnNextStepLastPos = false;

                // Determine new target position
                if (mdblStepDistance > 0) {
                    mintStepLeftTarget1 = mintStartPositionLeft1 + (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget1 = mintStartPositionRight1 - (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget2 = mintStartPositionRight2 - (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                } else {
                    mintStepLeftTarget1 = mintStartPositionLeft1 - (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepLeftTarget2 = mintStartPositionLeft2 - (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget1 = mintStartPositionRight1 + (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                    mintStepRightTarget2 = mintStartPositionRight2 + (int) (ourRobotConfig.getMecanumTurnOffset() * 0.5 * Math.abs(mdblStepDistance) * ourRobotConfig.getCOUNTS_PER_DEGREE());
                }

                //store the encoder positions so next step can calculate destination
                mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                fileLogger.writeEvent(3,"Current LPosition1:- " + robotDrive.baseMotor1.getCurrentPosition() + " mintStepLeftTarget1:-   " + mintStepLeftTarget1);
                fileLogger.writeEvent(3,"Current LPosition2:- " + robotDrive.baseMotor2.getCurrentPosition() + " mintStepLeftTarget2:-   " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"Current RPosition1:- " + robotDrive.baseMotor3.getCurrentPosition() + " mintStepRightTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(3,"Current RPosition2:- " + robotDrive.baseMotor4.getCurrentPosition() + " mintStepRightTarget2:- " + mintStepRightTarget2);

                // pass target position to motor controller
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);
                // set motor controller to mode
                robotDrive.setHardwareDriveRunToPosition();
                // set power on motor controller to start moving
                robotDrive.setHardwareDrivePower(Math.abs(mdblStepSpeed));
                fileLogger.writeEvent(2,"mintStepLeftTarget1 :- " + mintStepLeftTarget1);
                fileLogger.writeEvent(2,"mintStepLeftTarget2 :- " + mintStepLeftTarget2);
                fileLogger.writeEvent(2,"mintStepRightTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(2,"mintStepRightTarget2:- " + mintStepRightTarget2);

                mintCurrentStateTankTurn = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                // set power on motor controller to start moving
                robotDrive.setHardwareDrivePower(Math.abs(mdblStepSpeed));

                intLeft1MotorEncoderPosition = robotDrive.baseMotor1.getCurrentPosition();
                intLeft2MotorEncoderPosition = robotDrive.baseMotor2.getCurrentPosition();
                intRight1MotorEncoderPosition = robotDrive.baseMotor3.getCurrentPosition();
                intRight2MotorEncoderPosition = robotDrive.baseMotor4.getCurrentPosition();

                //determine how close to target we are
                dblDistanceToEndLeft1 = (mintStepLeftTarget1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndLeft2 = (mintStepLeftTarget2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight1 = (mintStepRightTarget1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight2 = (mintStepRightTarget2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();

                double dblDistanceToEnd = Math.max(Math.max(Math.max(Math.abs(dblDistanceToEndLeft1),Math.abs(dblDistanceToEndRight1)),Math.abs(dblDistanceToEndLeft2)),Math.abs(dblDistanceToEndRight2));

                fileLogger.writeEvent(3,"Current LPosition1:- " + intLeft1MotorEncoderPosition + " LTarget1:- " + mintStepLeftTarget1);
                fileLogger.writeEvent(3,"Current LPosition2:- " + intLeft2MotorEncoderPosition + " LTarget2:- " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"Current RPosition1:- " + intRight1MotorEncoderPosition + " RTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(3,"Current RPosition2:- " + intRight2MotorEncoderPosition + " RTarget2:- " + mintStepRightTarget2);
                fileLogger.writeEvent(3,"Distance to end:- " + dblDistanceToEnd);

                dashboard.displayPrintf(4, LABEL_WIDTH, "Left  Target: ", "Running to %7d :%7d", mintStepLeftTarget1, mintStepLeftTarget2);
                dashboard.displayPrintf(5, LABEL_WIDTH, "Left  Actual: ", "Running at %7d :%7d", intLeft1MotorEncoderPosition, intLeft2MotorEncoderPosition);
                dashboard.displayPrintf(6, LABEL_WIDTH, "Right Target: ", "Running to %7d :%7d", mintStepRightTarget1, mintStepRightTarget2);
                dashboard.displayPrintf(7, LABEL_WIDTH, "Right Actual: ", "Running at %7d :%7d", intRight1MotorEncoderPosition, intRight2MotorEncoderPosition);

                if (mblnRobotLastPos) {
                    if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                        fileLogger.writeEvent(3,"Complete NextStepLasp......." + dblDistanceToEnd);
                        mblnNextStepLastPos = true;
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateTankTurn = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                } else if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                    fileLogger.writeEvent(3,"Complete Early ......." + dblDistanceToEnd);
                    fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END ");
                    mintCurrentStateTankTurn = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                //stop driving when within .25 inch, sometimes takes a long time to get that last bit and times out.
                //stop when drive motors stop
                if (Math.abs(dblDistanceToEnd) <= 0.25) {
                    fileLogger.writeEvent(3,"dblDistanceToEnd Complete Near END " + Math.abs(dblDistanceToEnd));
                    mintCurrentStateTankTurn = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                } else if (!robotDrive.getHardwareBaseDriveBusy()) {
                    fileLogger.writeEvent(3,"Complete.......");
                    //robotDrive.setHardwareDrivePower(0);
                    mblnDisableVisionProcessing = false;  //enable vision processing
                    mintCurrentStateTankTurn = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStateTankTurn = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    //this has not been programmed, do not use
    private void RadiusTurnStep() {
        fileLogger.setEventTag("RadiusTurnStep()");
        double dblDistanceToEndLeft1;
        double dblDistanceToEndLeft2;
        double dblDistanceToEndRight1;
        double dblDistanceToEndRight2;
        int intLeft1MotorEncoderPosition;
        int intLeft2MotorEncoderPosition;
        int intRight1MotorEncoderPosition;
        int intRight2MotorEncoderPosition;
        double dblArcLengthRadiusTurnInner;             //used to calculate the arc length when doing a radius turn
        double rdblArcLengthRadiusTurnOuter;             //used to calculate the arc length when doing a radius turn
        double rdblSpeedOuter;                           //used to calculate the speed of the outer wheels during the turn
        double rdblSpeedInner;                           //used to calculate the speed of the inner wheels during the turn

        switch (mintCurrentStateRadiusTurn) {
            case STATE_INIT: {
                robotDrive.setHardwareDriveRunUsingEncoders();

                mblnDisableVisionProcessing = true;  //disable vision processing

                mdblRobotTurnAngle = mdblStepDistance;
                fileLogger.writeEvent(3,"mdblRobotTurnAngle" + mdblRobotTurnAngle);

                //calculate the distance to travel based on the angle we are turning
                // length = radius x angle (in radians)
                rdblArcLengthRadiusTurnOuter = ((mdblStepDistance / 180) * Math.PI) * mdblRobotParm1;
                dblArcLengthRadiusTurnInner = ((mdblStepDistance / 180) * Math.PI) * (mdblRobotParm1 - (ourRobotConfig.getROBOT_TRACK()));
                //rdblArcLengthRadiusTurnOuter = (mdblStepDistance / 180) *  Math.PI) * (mdblRobotParm1 + (0.5 * ROBOT_TRACK));

                rdblSpeedOuter = mdblStepSpeed;

                if (rdblSpeedOuter >= 0.58) {
                    rdblSpeedOuter = 0.58;  //This is the maximum speed, anything above 0.6 is the same as a speed of 1 for drive to position
                }
                rdblSpeedInner = dblArcLengthRadiusTurnInner / rdblArcLengthRadiusTurnOuter * rdblSpeedOuter * 0.96;
                fileLogger.writeEvent(3,"dblArcLengthRadiusTurnInner " + dblArcLengthRadiusTurnInner);
                fileLogger.writeEvent(3,"rdblArcLengthRadiusTurnOuter " + rdblArcLengthRadiusTurnOuter);
                fileLogger.writeEvent(3,"rdblSpeedOuter " + rdblSpeedOuter);
                fileLogger.writeEvent(3,"rdblSpeedInner " + rdblSpeedInner);

                // Get Current Encoder positions
                if (mblnNextStepLastPos) {
                    mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                    mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                    mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                    mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                } else {
                    mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                    mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                    mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                    mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                }
                mblnNextStepLastPos = false;

                // Determine new target position
                if (mdblStepDistance > 0) {
                    mintStepLeftTarget1 = mintStartPositionLeft1 + (int) (dblArcLengthRadiusTurnInner * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (dblArcLengthRadiusTurnInner * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepRightTarget1 = mintStartPositionRight1 + (int) (rdblArcLengthRadiusTurnOuter * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepRightTarget2 = mintStartPositionRight2 + (int) (rdblArcLengthRadiusTurnOuter * ourRobotConfig.getCOUNTS_PER_INCH());

                    //store the encoder positions so next step can calculate destination
                    mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                    mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                    mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                    mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                    // pass target position to motor controller
                    robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                    robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                    robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                    robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                    // set motor controller to mode
                    robotDrive.setHardwareDriveRunToPosition();

                    // set power on motor controller to start moving
                    robotDrive.setHardwareDriveLeftMotorPower(rdblSpeedInner);  //left side is inner when turning left
                    robotDrive.setHardwareDriveRightMotorPower(rdblSpeedOuter);  //right side is outer when turning left
                } else {
                    mintStepLeftTarget1 = mintStartPositionLeft1 + (int) (rdblArcLengthRadiusTurnOuter * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (rdblArcLengthRadiusTurnOuter * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepRightTarget1 = mintStartPositionRight1 + (int) (dblArcLengthRadiusTurnInner * ourRobotConfig.getCOUNTS_PER_INCH());
                    mintStepRightTarget2 = mintStartPositionRight2 + (int) (dblArcLengthRadiusTurnInner * ourRobotConfig.getCOUNTS_PER_INCH());

                    //store the encoder positions so next step can calculate destination
                    mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                    mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                    mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                    mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                    // pass target position to motor controller
                    robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                    robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                    robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                    robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                    // set motor controller to mode
                    robotDrive.setHardwareDriveRunToPosition();

                    // set power on motor controller to start moving
                    robotDrive.setHardwareDriveLeftMotorPower(rdblSpeedOuter);  //left side is outer when turning left
                    robotDrive.setHardwareDriveRightMotorPower(rdblSpeedInner);  //right side is inner when turning left
                }

                fileLogger.writeEvent(3,"Current LPosition1:- " + robotDrive.baseMotor1.getCurrentPosition() + " mintStepLeftTarget1:-   " + mintStepLeftTarget1);
                fileLogger.writeEvent(3,"Current LPosition2:- " + robotDrive.baseMotor2.getCurrentPosition() + " mintStepLeftTarget2:-   " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"Current RPosition1:- " + robotDrive.baseMotor3.getCurrentPosition() + " mintStepRightTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(3,"Current RPosition2:- " + robotDrive.baseMotor4.getCurrentPosition() + " mintStepRightTarget2:- " + mintStepRightTarget2);
                mintCurrentStateRadiusTurn = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {
                intLeft1MotorEncoderPosition = robotDrive.baseMotor1.getCurrentPosition();
                intLeft2MotorEncoderPosition = robotDrive.baseMotor2.getCurrentPosition();
                intRight1MotorEncoderPosition = robotDrive.baseMotor3.getCurrentPosition();
                intRight2MotorEncoderPosition = robotDrive.baseMotor4.getCurrentPosition();

                //determine how close to target we are
                dblDistanceToEndLeft1 = (mintStepLeftTarget1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndLeft2 = (mintStepLeftTarget2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight1 = (mintStepRightTarget1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight2 = (mintStepRightTarget2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                fileLogger.writeEvent(3,"Current LPosition1:- " + intLeft1MotorEncoderPosition + " LTarget1:- " + mintStepLeftTarget1);
                fileLogger.writeEvent(3,"Current LPosition2:- " + intLeft2MotorEncoderPosition + " LTarget2:- " + mintStepLeftTarget2);
                fileLogger.writeEvent(3,"Current RPosition1:- " + intRight1MotorEncoderPosition + " RTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(3,"Current RPosition2:- " + intRight2MotorEncoderPosition + " RTarget2:- " + mintStepRightTarget2);
                dashboard.displayPrintf(3, LABEL_WIDTH, "Target: ", "Running to %7d :%7d", mintStepLeftTarget1, mintStepRightTarget1);
                dashboard.displayPrintf(4, LABEL_WIDTH, "Actual_Left: ", "Running at %7d :%7d", intLeft1MotorEncoderPosition, intLeft2MotorEncoderPosition);
                dashboard.displayPrintf(5, LABEL_WIDTH, "ActualRight: ", "Running at %7d :%7d", intRight1MotorEncoderPosition, intRight2MotorEncoderPosition);

                if (mblnRobotLastPos) {
                    if ((((dblDistanceToEndRight1 + dblDistanceToEndRight2) / 2) < 2) && (((dblDistanceToEndLeft1 + dblDistanceToEndLeft2) / 2) < 2)) {
                        mblnNextStepLastPos = true;
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateRadiusTurn = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                    }
                }

                if (!robotDrive.baseMotor1.isBusy() || (!robotDrive.baseMotor3.isBusy())) {
                    robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(1,"Complete         ");
                    mblnDisableVisionProcessing = false;  //enable vision processing
                    mintCurrentStateRadiusTurn = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStateRadiusTurn = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private void MecanumStrafe() {
        fileLogger.setEventTag("MecanumStrafe()");

        double dblDistanceToEndLeft1;
        double dblDistanceToEndLeft2;
        double dblDistanceToEndRight1;
        double dblDistanceToEndRight2;
        int intLeft1MotorEncoderPosition;
        int intLeft2MotorEncoderPosition;
        int intRight1MotorEncoderPosition;
        int intRight2MotorEncoderPosition;
        double rdblSpeed;

        switch (mintCurrentStateMecanumStrafe) {
            case STATE_INIT: {
                double adafruitIMUHeading;
                double currentHeading;

                robotDrive.setHardwareDriveRunUsingEncoders();
                mblnDisableVisionProcessing = true;  //disable vision processing

                adafruitIMUHeading = getAdafruitHeading();
                currentHeading = adafruitIMUHeading;
                //mdblRobotTurnAngle = Double.parseDouble(mdblStepDistance);
                //fileLogger.writeEvent(3, "MecanumStrafe()", "mdblRobotTurnAngle " + mdblRobotTurnAngle + " currentHeading " + currentHeading);
                //mdblTurnAbsoluteGyro = Double.parseDouble(newAngleDirection((int) currentHeading, (int) mdblRobotTurnAngle).substring(3));

                robotDrive.setHardwareDriveRunToPosition();

                // Get Current Encoder positions
                if (mblnNextStepLastPos) {
                    mintStartPositionLeft1 = mintLastEncoderDestinationLeft1;
                    mintStartPositionLeft2 = mintLastEncoderDestinationLeft2;
                    mintStartPositionRight1 = mintLastEncoderDestinationRight1;
                    mintStartPositionRight2 = mintLastEncoderDestinationRight2;
                } else {
                    mintStartPositionLeft1 = robotDrive.baseMotor1.getCurrentPosition();
                    mintStartPositionLeft2 = robotDrive.baseMotor2.getCurrentPosition();
                    mintStartPositionRight1 = robotDrive.baseMotor3.getCurrentPosition();
                    mintStartPositionRight2 = robotDrive.baseMotor4.getCurrentPosition();
                }
                mblnNextStepLastPos = false;

                mintStepLeftTarget1 = mintStartPositionLeft1 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_LEFT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_REAR_OFFSET());
                mintStepLeftTarget2 = mintStartPositionLeft2 + (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_LEFT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_FRONT_OFFSET());
                mintStepRightTarget1 = mintStartPositionRight1 + (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_RIGHT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_REAR_OFFSET());
                mintStepRightTarget2 = mintStartPositionRight2 - (int) (mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_RIGHT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_FRONT_OFFSET());

                //store the encoder positions so next step can calculate destination
                mintLastEncoderDestinationLeft1 = mintStepLeftTarget1;
                mintLastEncoderDestinationLeft2 = mintStepLeftTarget2;
                mintLastEncoderDestinationRight1 = mintStepRightTarget1;
                mintLastEncoderDestinationRight2 = mintStepRightTarget2;

                // pass target position to motor controller
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);

                // set motor controller to mode
                robotDrive.setHardwareDriveRunToPosition();
                rdblSpeed = mdblStepSpeed;

                // set power on motor controller to start moving
                //robotDrive.setHardwareDrivePower(rdblSpeed);  //set motor power
                robotDrive.setHardwareDriveLeft1MotorPower(rdblSpeed);
                robotDrive.setHardwareDriveLeft2MotorPower(rdblSpeed);
                robotDrive.setHardwareDriveRight1MotorPower(rdblSpeed);
                robotDrive.setHardwareDriveRight2MotorPower(rdblSpeed);
                mintCurrentStateMecanumStrafe = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {
                rdblSpeed = mdblStepSpeed;

                // pass target position to motor controller
                robotDrive.baseMotor1.setTargetPosition(mintStepLeftTarget1);
                robotDrive.baseMotor2.setTargetPosition(mintStepLeftTarget2);
                robotDrive.baseMotor3.setTargetPosition(mintStepRightTarget1);
                robotDrive.baseMotor4.setTargetPosition(mintStepRightTarget2);
                robotDrive.setHardwareDriveLeft1MotorPower(rdblSpeed);
                robotDrive.setHardwareDriveLeft2MotorPower(rdblSpeed);
                //robotDrive.setHardwareDriveLeft2MotorPower(rdblSpeed * 1.12);
                robotDrive.setHardwareDriveRight1MotorPower(rdblSpeed);
                //robotDrive.setHardwareDriveRight2MotorPower(rdblSpeed * 1.12);
                robotDrive.setHardwareDriveRight2MotorPower(rdblSpeed);

                double adafruitIMUHeading;

                adafruitIMUHeading = getAdafruitHeading();
                mdblGyrozAccumulated = adafruitIMUHeading;
                mdblGyrozAccumulated = teamAngleAdjust(mdblGyrozAccumulated);//Set variables to MRgyro readings
                //mdblTurnAbsoluteGyro = Double.parseDouble(newAngleDirectionGyro((int) mdblGyrozAccumulated, (int) mdblRobotTurnAngle).substring(3));
                //String mstrDirection = (newAngleDirectionGyro((int) mdblGyrozAccumulated, (int) mdblRobotTurnAngle).substring(0, 3));
                fileLogger.writeEvent(3, "Running, mdblGyrozAccumulated = " + mdblGyrozAccumulated);
                fileLogger.writeEvent(3, "Running, mdblTurnAbsoluteGyro = " + mdblTurnAbsoluteGyro);
                //fileLogger.writeEvent(3, "Running, mstrDirection        = " + mstrDirection);
                fileLogger.writeEvent(3, "Running, adafruitIMUHeading   = " + adafruitIMUHeading);

                intLeft1MotorEncoderPosition = robotDrive.baseMotor1.getCurrentPosition();
                intLeft2MotorEncoderPosition = robotDrive.baseMotor2.getCurrentPosition();
                intRight1MotorEncoderPosition = robotDrive.baseMotor3.getCurrentPosition();
                intRight2MotorEncoderPosition = robotDrive.baseMotor4.getCurrentPosition();

                //determine how close to target we are
                dblDistanceToEndLeft1 = (mintStepLeftTarget1 - intLeft1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndLeft2 = (mintStepLeftTarget2 - intLeft2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight1 = (mintStepRightTarget1 - intRight1MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                dblDistanceToEndRight2 = (mintStepRightTarget2 - intRight2MotorEncoderPosition) / ourRobotConfig.getCOUNTS_PER_INCH();
                fileLogger.writeEvent(3, "Current LPosition1:- " + intLeft1MotorEncoderPosition + " LTarget1:- " + mintStepLeftTarget1);
                fileLogger.writeEvent(3, "Current LPosition2:- " + intLeft2MotorEncoderPosition + " LTarget2:- " + mintStepLeftTarget2);
                fileLogger.writeEvent(3, "Current RPosition1:- " + intRight1MotorEncoderPosition + " RTarget1:- " + mintStepRightTarget1);
                fileLogger.writeEvent(3, "Current RPosition2:- " + intRight2MotorEncoderPosition + " RTarget2:- " + mintStepRightTarget2);

                dashboard.displayPrintf(4,  "Mecanum Strafe Positions moving " + mdblStepDistance);
                dashboard.displayPrintf(5, LABEL_WIDTH, "Left  Target: ", "Running to %7d :%7d", mintStepLeftTarget1, mintStepLeftTarget2);
                dashboard.displayPrintf(6, LABEL_WIDTH, "Left  Actual: ", "Running at %7d :%7d", intLeft1MotorEncoderPosition, intLeft2MotorEncoderPosition);
                dashboard.displayPrintf(7, LABEL_WIDTH, "Right Target: ", "Running to %7d :%7d", mintStepRightTarget1, mintStepRightTarget2);
                dashboard.displayPrintf(8, LABEL_WIDTH, "Right Actual: ", "Running at %7d :%7d", intRight1MotorEncoderPosition, intRight2MotorEncoderPosition);

                //if moving ramp up
                // ramp up speed - need to write function to ramp up speed
                double dblDistanceFromStartLeft1;
                double dblDistanceFromStartLeft2;
                double dblDistanceFromStartRight1;
                double dblDistanceFromStartRight2;
                double dblDistanceFromStart;
                dblDistanceFromStartLeft1 = Math.abs(mintStartPositionLeft1 - intLeft1MotorEncoderPosition) / (ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_LEFT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_FRONT_OFFSET());
                dblDistanceFromStartLeft2 = Math.abs(mintStartPositionLeft2 - intLeft2MotorEncoderPosition) / (ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_LEFT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_REAR_OFFSET());
                dblDistanceFromStartRight1 = Math.abs(mintStartPositionRight1 - intRight1MotorEncoderPosition) / (ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_RIGHT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_FRONT_OFFSET());
                dblDistanceFromStartRight2 = Math.abs(mintStartPositionRight2 - intRight2MotorEncoderPosition) / (ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_RIGHT_OFFSET() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE() * ourRobotConfig.getCOUNTS_PER_INCH_STRAFE_REAR_OFFSET());

                dblDistanceFromStart = (dblDistanceFromStartLeft1 + dblDistanceFromStartRight1 + dblDistanceFromStartLeft2 + dblDistanceFromStartRight2) / 4;

                //double dblDistanceToEnd = Math.max(Math.max(Math.max(Math.abs(dblDistanceToEndLeft1),Math.abs(dblDistanceToEndRight1)),Math.abs(dblDistanceToEndLeft2)),Math.abs(dblDistanceToEndRight2));
                double dblDistanceToEnd = Math.abs(Math.abs(mdblStepDistance) - Math.abs(dblDistanceFromStart));
                fileLogger.writeEvent(3,"How close??? ......." + dblDistanceToEnd);
                //parm 1 is strafe until we are close to a wall of this distance using distance sensors
                if ((mdblRobotParm1 > 0) || (mdblRobotParm2 > 0)) {
                    //check which way we are strafing
                    double distanceToTarget = 0;
                    double distanceFromWall = 0;
                    double distanceToTargetC = 0;
                    double distanceFromWallC = 0;
                    if (mdblStepDistance < 0) {
                        //less than is left
                        distanceToTarget  = sensors.distanceSideLeftIN();
                        distanceFromWall  = sensors.distanceSideRightIN();
                        distanceToTargetC = sensors.distanceColorSideLeftIN();
                        distanceFromWallC = sensors.distanceColorSideRightIN();
                    } else {
                        //greater than is right
                        distanceToTarget  = sensors.distanceSideRightIN();
                        distanceFromWall  = sensors.distanceSideLeftIN();
                        distanceToTargetC = sensors.distanceColorSideRightIN();
                        distanceFromWallC = sensors.distanceColorSideLeftIN();
                    }
                    fileLogger.writeEvent(3,"Measuring Distance To ObjectR...." + (distanceToTarget));
                    fileLogger.writeEvent(3,"Measuring Distance From WallR...." + (distanceFromWall));
                    fileLogger.writeEvent(3,"Measuring Distance To ObjectC...." + (distanceToTargetC));
                    fileLogger.writeEvent(3,"Measuring Distance From WallC...." + (distanceFromWallC));

                    if ((Math.abs(mdblRobotParm1) >= distanceToTarget) && (mdblRobotParm1 > 0)) {
                        robotDrive.setHardwareDrivePower(0);
                        fileLogger.writeEvent(3,"Complete closeEnough......." + (distanceToTarget));
                        mblnNextStepLastPos = false;
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    } else if ((Math.abs(mdblRobotParm2) <= distanceFromWall) && (mdblRobotParm2 > 0)) {
                        robotDrive.setHardwareDrivePower(0);
                        fileLogger.writeEvent(3,"Complete farEnough......." + (distanceFromWall));
                        mblnNextStepLastPos = false;
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                }

                if (mblnRobotLastPos) {
                    if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                        fileLogger.writeEvent(3,"Complete NextStepLasp......." + dblDistanceToEnd);
                        mblnNextStepLastPos = true;
                        mblnDisableVisionProcessing = false;  //enable vision processing
                        mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    }
                } else if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                    fileLogger.writeEvent(3,"Complete Early ......." + dblDistanceToEnd);
                    fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END ");
                    mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                    robotDrive.setHardwareDrivePower(0);
                    deleteParallelStep();
                    break;
                }
/*
                if ((mintLastPositionLeft1_1 < (mintLastPositionLeft2_1 + 3)) && (mintLastPositionLeft1_1 > (mintLastPositionLeft2_1 - 3)))
                    blnMotor1Stall1 = true;
                if ((mintLastPositionLeft1_2 < (mintLastPositionLeft2_2 + 3)) && (mintLastPositionLeft1_2 > (mintLastPositionLeft2_2 - 3)))
                    blnMotorStall2 = true;
                if ((mintLastPositionRight1_1 < (mintLastPositionRight2_1 + 3)) && (mintLastPositionRight1_1 > (mintLastPositionRight2_1 + 3)))
                    blnMotorStall3 = true;
                if ((mintLastPositionRight1_2 < (mintLastPositionRight2_2 + 3)) && (mintLastPositionRight1_2 < (mintLastPositionRight2_2 + 3)))
                    blnMotorStall4 = true;

                mintLastPositionLeft2_1 = robotDrive.baseMotor1.getCurrentPosition();
                mintLastPositionLeft2_2 = robotDrive.baseMotor2.getCurrentPosition();
                mintLastPositionRight2_1 = robotDrive.baseMotor3.getCurrentPosition();
                mintLastPositionRight2_2 = robotDrive.baseMotor4.getCurrentPosition();

                if (!blnStallTimerStarted) {
                    if (blnMotorStall1 && blnMotorStall2 && blnMotorStall3 && blnMotorStall4) {
                        mStateStalTimee.reset();
                        blnStallTimerStarted = true;
                    }
                } else if (mStateStalTimee.milliseconds() > 500) {
                    mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                if (!blnMotor1Stall1 || !blnMotorStall2 || !blnMotorStall3 || !blnMotorStall4)
                    blnStallTimerStarted = false;
*/

                //stop driving when within .5 inch, sometimes takes a long time to get that last bit and times out.
                //stop when drive motors stop
                if (Math.abs(dblDistanceToEnd) <= 0.5) {
                    fileLogger.writeEvent(3,"mblnRobotLastPos Complete Near END " + Math.abs(dblDistanceToEnd));
                    mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                    robotDrive.setHardwareDrivePower(0);
                    /*robotDrive.baseMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor3.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor4.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);*/
                    deleteParallelStep();
                    break;
                } else if (!robotDrive.getHardwareBaseDriveBusy()) {
                    robotDrive.setHardwareDrivePower(0);
                    /*robotDrive.baseMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor3.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor4.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    robotDrive.baseMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);*/
                    fileLogger.writeEvent(1, "Complete         ");
                    mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

            } //end Case Running
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                robotDrive.setHardwareDrivePower(0);
                fileLogger.writeEvent(1, "Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStateMecanumStrafe = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
                break;
            }
            break;
        }
    }

    double mdblWyattsGyroDriveStartAngle = 0;

    private void WyattsGyroDrive(){
        switch(mintCurrentStateWyattsGyroDrive){
            case STATE_INIT:
                fileLogger.setEventTag("WyattsGyroDrive");

                mdblWyattsGyroDriveStartAngle = getAdafruitHeading();
                fileLogger.writeEvent(5,"Starting Gyro Value is: " + mdblWyattsGyroDriveStartAngle);

                // Determine new target position, and pass to motor controller
                int mintWyattsGyroDriveMoveCounts = (int)(mdblStepDistance * ourRobotConfig.getCOUNTS_PER_INCH());
                fileLogger.writeEvent(3, "move counts in Wyatt Gyro Drive is: " + mintWyattsGyroDriveMoveCounts);

                robotDrive.baseMotor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                robotDrive.baseMotor2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                robotDrive.baseMotor3.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                robotDrive.baseMotor4.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                // Set Target and Turn On RUN_TO_POSITION
                robotDrive.baseMotor1.setTargetPosition(robotDrive.baseMotor1.getCurrentPosition() + mintWyattsGyroDriveMoveCounts);
                robotDrive.baseMotor2.setTargetPosition(robotDrive.baseMotor2.getCurrentPosition() + mintWyattsGyroDriveMoveCounts);
                robotDrive.baseMotor3.setTargetPosition(robotDrive.baseMotor3.getCurrentPosition() + mintWyattsGyroDriveMoveCounts);
                robotDrive.baseMotor4.setTargetPosition(robotDrive.baseMotor4.getCurrentPosition() + mintWyattsGyroDriveMoveCounts);

                robotDrive.setHardwareDrivePower(mdblStepSpeed);
                break;

            case STATE_RUNNING:
                // adjust relative speed based on heading error.
                double driveError = getDriveError(mdblWyattsGyroDriveStartAngle);
                fileLogger.writeEvent(3, "Drive Error in Wyatt Gryo Drive is: " + driveError);
                fileLogger.writeEvent(3, "Current Gyro Value: " + getAdafruitHeading());
                double steer = getDriveSteer(driveError, mdblRobotParm1);
                fileLogger.writeEvent(3, "Drive Steer in Wyatt Gyro Drive is" + steer);

                // if driving in reverse, the motor correction also needs to be reversed
                if (mdblStepDistance < 0)
                    steer *= -1.0;

                double leftSpeed = mdblStepSpeed - steer;
                fileLogger.writeEvent(3, "Left Motor Speed in Wyatts Gyro Drive is: " + leftSpeed);
                double rightSpeed = mdblStepSpeed + steer;
                fileLogger.writeEvent(3, "Right Motor Speed in Wyatts Gyro Drive is: " + rightSpeed);

                // Normalize speeds if either one exceeds +/- 1.0;
                double max = Math.max(Math.abs(leftSpeed), Math.abs(rightSpeed));
                if (max > 1.0)
                {
                    leftSpeed /= max;
                    rightSpeed /= max;
                }

                robotDrive.setHardwareDriveLeftMotorPower(leftSpeed);
                robotDrive.setHardwareDriveRightMotorPower(rightSpeed);

                if (!robotDrive.baseMotor1.isBusy() && !robotDrive.baseMotor2.isBusy() &&
                        !robotDrive.baseMotor3.isBusy() && !robotDrive.baseMotor4.isBusy()){

                    mintCurrentStateWyattsGyroDrive = Constants.stepState.STATE_COMPLETE;

                    robotDrive.setHardwareDrivePower(0);

                    // Turn off RUN_TO_POSITION
                    robotDrive.baseMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor3.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    robotDrive.baseMotor4.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    fileLogger.writeEvent(2, "Basemotor turned off Run To Position in Wyatts Gyro Drive");
                }

                if (mStateTime.seconds() > mdblStepTimeout) {
                    robotDrive.setHardwareDrivePower(0);
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateMoveLift = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;
        }
    }

    private void TankTurnGyroHeading() {
        fileLogger.setEventTag("TankTurnGyroHeading()");
        switch (mintCurrentStateTankTurnGyroHeading) {
            case STATE_INIT: {
                double gain = 0.09;
                if (dblStartVoltage > 13.1){
                    gain = gain;
                } else if (dblStartVoltage > 12.8) {
                    gain = gain + 0.001;
                } else {
                    gain = gain + 0.002;
                }
                PID1 = new TOWR5291PID(runtime,0,0,gain,0,0);
                double adafruitIMUHeading;
                adafruitIMUHeading = getAdafruitHeading();
                mdblPowerBoost = 0;
                mintStableCount = 0;
                mstrWiggleDir = "";
                mdblRobotTurnAngle = mdblStepDistance;
                mdblTankTurnGyroRequiredHeading = mdblRobotTurnAngle + adafruitIMUHeading;
                fileLogger.writeEvent(3," When calculated mdblTankTurnGyroRequiredHeading " + mdblTankTurnGyroRequiredHeading);
                blnCrossZeroPositive = false;
                blnCrossZeroNegative = false;
                if (mdblTankTurnGyroRequiredHeading > 360) {
                    blnCrossZeroPositive = true;
                    while (mdblTankTurnGyroRequiredHeading > 360)
                        mdblTankTurnGyroRequiredHeading = mdblTankTurnGyroRequiredHeading - 360;
                } else if (mdblTankTurnGyroRequiredHeading < 0) {
                    blnCrossZeroNegative = true;
                    while (mdblTankTurnGyroRequiredHeading < 0)
                        mdblTankTurnGyroRequiredHeading = 360 + mdblTankTurnGyroRequiredHeading;
                } else if ((mdblTankTurnGyroRequiredHeading - adafruitIMUHeading) > 180 ) {
                    blnCrossZeroNegative = true;
                }

                if (mdblRobotParm1 > 0) {
                    if ((adafruitIMUHeading - mdblRobotTurnAngle) > 180)
                        blnCrossZeroNegative = true;
                    if (mdblRobotTurnAngle < 0)
                        mdblRobotTurnAngle = mdblRobotTurnAngle + 360;
                    mdblTankTurnGyroRequiredHeading = mdblRobotTurnAngle;
                }
                if (Math.abs(adafruitIMUHeading - mdblRobotTurnAngle) > 180){
                    blnReverseDir = true;
                }
                fileLogger.writeEvent(3,"blnReverseDir " + blnReverseDir);
                fileLogger.writeEvent(3,"mdblRobotTurnAngle " + mdblRobotTurnAngle + " adafruitIMUHeading " + adafruitIMUHeading);
                fileLogger.writeEvent(3,"mdblTankTurnGyroRequiredHeading " + mdblTankTurnGyroRequiredHeading);
                mdblTurnAbsoluteGyro = TOWR5291Utils.getNewHeading((int) adafruitIMUHeading, (int) mdblRobotTurnAngle);
                robotDrive.setHardwareDriveRunWithoutEncoders();
                mintCurrentStateTankTurnGyroHeading = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {
                double adafruitIMUHeading = getAdafruitHeading();
                fileLogger.writeEvent(3,"Running, before adafruitIMUHeading   = " + adafruitIMUHeading);

                //no longer near the 0 crossing point
                if ((adafruitIMUHeading > 90 ) && (adafruitIMUHeading < 270 )) {
                    blnCrossZeroPositive = false;
                    blnCrossZeroNegative = false;
                } else {
                    if (mdblRobotParm1 > 0) {
                        if (Math.abs(adafruitIMUHeading - mdblRobotTurnAngle) > 180)
                            blnCrossZeroNegative = true;
                        else
                            blnCrossZeroNegative = false;
                    }
                }
                fileLogger.writeEvent(3,"Running, Zero Neg Crossing Setting = " + blnCrossZeroNegative);
                fileLogger.writeEvent(3,"Running, Zero Pos Crossing Setting = " + blnCrossZeroPositive);

                //if (blnCrossZeroPositive && (adafruitIMUHeading >= 270) )
                //    adafruitIMUHeading = adafruitIMUHeading - 360;
                //else if (blnCrossZeroNegative && (adafruitIMUHeading >= 270))
                //    adafruitIMUHeading = adafruitIMUHeading - 360;

                fileLogger.writeEvent(3,"Running, after adafruitIMUHeading   = " + adafruitIMUHeading);
                double dblError = mdblRobotParm6;
                if (dblError == 0) {
                    dblError = 1;
                }
                if (Math.abs(mdblTankTurnGyroRequiredHeading - adafruitIMUHeading) > dblError) {  //Continue while the robot direction is further than param1 degrees from the target
                    fileLogger.writeEvent(3,"adafruitIMUHeading....." + adafruitIMUHeading);
                    //fileLogger.writeEvent(3,"Math.sin(adafruitIMUHeading * (Math.PI / 180.0)....." + Math.sin(adafruitIMUHeading * (Math.PI / 180.0)));
                    fileLogger.writeEvent(3,"mdblTankTurnGyroRequiredHeading....." + mdblTankTurnGyroRequiredHeading);
                    //fileLogger.writeEvent(3,"Correction....." + Math.sin(mdblTankTurnGyroRequiredHeading * (Math.PI / 180.0)));
                    //double correction = PID1.PIDCorrection(runtime,Math.sin(adafruitIMUHeading * (Math.PI / 180.0)), Math.sin(mdblTankTurnGyroRequiredHeading * (Math.PI / 180.0)));
                    double correction = 0;
                    if (blnCrossZeroNegative) {
                        correction = PID1.PIDCorrection(runtime, Math.abs(adafruitIMUHeading - 360), mdblTankTurnGyroRequiredHeading);
                    } else {
                        correction = PID1.PIDCorrection(runtime, adafruitIMUHeading, mdblTankTurnGyroRequiredHeading);
                    }
                    fileLogger.writeEvent(3,"Correction....." + correction);
                    //if (Math.abs(adafruitIMUHeading - mdblRobotTurnAngle) > 180){
                    if ((Math.abs(mdblTankTurnGyroRequiredHeading - Math.abs(mdblRobotTurnAngle)) > 180) || (blnCrossZeroNegative)){
                        blnReverseDir = true;
                    } else {
                        blnReverseDir = false;
                    }

                    if (blnReverseDir)
                        correction = -correction;
                    robotDrive.setHardwareDriveLeftMotorPower( -mdblStepSpeed * correction);
                    robotDrive.setHardwareDriveRightMotorPower( mdblStepSpeed * correction);
                } else {
                    robotDrive.setHardwareDriveLeftMotorPower(0);
                    robotDrive.setHardwareDriveRightMotorPower(0);
                    robotDrive.setHardwareDriveRunUsingEncoders();
                    fileLogger.writeEvent(1, "TankTurnGyro()", "Complete Near Enough:- " + Math.abs(mdblTankTurnGyroRequiredHeading - adafruitIMUHeading) + " Range:- " + mdblRobotParm6);
                    //  Transition to a new state.
                    mintCurrentStateTankTurnGyroHeading = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
            } //end Case Running
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                fileLogger.writeEvent(1, "TankTurnGyro()", "Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStateTankTurnGyroHeading = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private void TankTurnGyroHeadingEncoder()
    {
        fileLogger.setEventTag("TankTurnGyroHeadingEncoder()");
        switch (mintCurrentStateGyroTurnEncoder5291){
            case STATE_INIT:
            {
                double adafruitIMUHeading;
                adafruitIMUHeading = getAdafruitHeading();
                mdblPowerBoost = 0;
                mintStableCount = 0;
                mstrWiggleDir = "";
                mdblRobotTurnAngle = mdblStepDistance;
                if (mdblRobotParm2 > 0)
                    mdblTankTurnGyroRequiredHeading = mdblRobotTurnAngle;
                else
                    mdblTankTurnGyroRequiredHeading = mdblRobotTurnAngle + adafruitIMUHeading;
                fileLogger.writeEvent(3,",mdblRobotTurnAngle " + mdblRobotTurnAngle + " adafruitIMUHeading " + adafruitIMUHeading + " Target Heading " + mdblTankTurnGyroRequiredHeading);
                mdblTurnAbsoluteGyro = TOWR5291Utils.getNewHeading((int) adafruitIMUHeading, (int) mdblRobotTurnAngle);
                mintCurrentStateGyroTurnEncoder5291 = Constants.stepState.STATE_RUNNING;
            }
            break;
            case STATE_RUNNING: {
                double adafruitIMUHeading;
                adafruitIMUHeading = getAdafruitHeading();
                mdblGyrozAccumulated = teamAngleAdjust(mdblGyrozAccumulated); //Set variables to MRgyro readings
                fileLogger.writeEvent(3,"Running, mdblGyrozAccumulated = " + mdblGyrozAccumulated);
                fileLogger.writeEvent(3,"Running, adafruitIMUHeading   = " + adafruitIMUHeading);
                fileLogger.writeEvent(3,"Running, new angle   = " + (newAngleDirectionGyroOffset ((int)adafruitIMUHeading, (int)mdblTankTurnGyroRequiredHeading)));

                //only keep aiming for target if the angle is greater than the error specified in parm1
                if (Math.abs((newAngleDirectionGyroOffset ((int)adafruitIMUHeading, (int)mdblTankTurnGyroRequiredHeading))) > mdblRobotParm1) {
                    autonomousStepsFile.insertSteps(3, "TANKTURNGYROENCODER", (int) mdblTankTurnGyroRequiredHeading, .6, false, false, mdblRobotParm1, 1, 0, 0, 0, 0, mintCurrentStep + 1);
                    autonomousStepsFile.insertSteps(3, "TANKTURN", (newAngleDirectionGyroOffset((int) adafruitIMUHeading, (int) mdblTankTurnGyroRequiredHeading)), .6, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                }
                mintCurrentStateGyroTurnEncoder5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout) {
                fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStateGyroTurnEncoder5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }
    private void moveLiftUpDown(){

        fileLogger.setEventTag("moveLiftUpDown()");

        switch (mintCurrentStateMoveLift) {
            case STATE_INIT:
                fileLogger.writeEvent(2, "Initialised");


                robotArms.liftMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                fileLogger.writeEvent(5, "Using Encoders");
                robotArms.liftMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                fileLogger.writeEvent(5, "Resetting Encoders");

                //check timeout value
                if (mStateTime.milliseconds() > mdblRobotParm1) {
                    if (mdblRobotParm2 == 0) {
                        mdblTargetPositionTop1 = robotArms.getLiftMotor1Encoder() + (mdblStepDistance); //* ourRobotConfig.getLIFTMAIN_COUNTS_PER_INCH());
                        fileLogger.writeEvent(1, "Lift Target mode 1 " + mdblTargetPositionTop1);
                    } else if (mdblRobotParm2 == 1) {
                        double distaceInCounts = (mdblStepDistance * ourRobotConfig.getLIFTMAIN_COUNTS_PER_INCH());
                        mdblTargetPositionTop1 = (robotArms.getLiftMotor1Encoder() - mintLiftStartCountMotor1) + distaceInCounts;
                        fileLogger.writeEvent(1, "Lift Target mode 2 " + mdblTargetPositionTop1);
                    } else {
                        mdblTargetPositionTop1 = 0;
                        mdblTargetPositionTop2 = 0;
                        fileLogger.writeEvent("ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
                        fileLogger.writeEvent("ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
                        fileLogger.writeEvent("ERROR MOVING LIFT PARM 1 IS THE MODE CAN BE 0 OR 1");
                        fileLogger.writeEvent("Mode 1 is to move a certain distance so like move out 1 more inch");
                        fileLogger.writeEvent("Mode 2 is to move to a spot so move to 5 inches on the lift");
                    }
                    robotArms.liftMotor1.setTargetPosition((int) mdblTargetPositionTop1);
                    fileLogger.writeEvent("target position set");
                    robotArms.setHardwareLiftPower(mdblStepSpeed);
                    fileLogger.writeEvent("lift power set");
                    robotArms.setHardwareLiftMotorRunToPosition();
                    fileLogger.writeEvent("Run to position set");

                    mintCurrentStateMoveLift = Constants.stepState.STATE_RUNNING;
                }
                break;
            case STATE_RUNNING: {
                fileLogger.writeEvent(2, "Running");

                robotArms.setHardwareLiftPower(mdblStepSpeed);
                fileLogger.writeEvent(2, "Motor Speed Set: Busy 1 " + robotArms.liftMotor1.isBusy());

                //determine how close to target we are
                double dblDistanceToEndLift1 = (mdblTargetPositionTop1 - robotArms.getLiftMotor1Encoder());  //  / ourRobotConfig.getLIFTMAIN_COUNTS_PER_INCH();

                //if getting close ramp down speed
                double dblDistanceToEnd = Math.abs(dblDistanceToEndLift1);

                fileLogger.writeEvent(3, "Distance to END " + Math.abs(dblDistanceToEnd));
                fileLogger.writeEvent(5, "Lift Motor 1 Current Encoder Count: " + String.valueOf(robotArms.liftMotor1.getCurrentPosition()));

                if (Math.abs(dblDistanceToEnd) <= mdblRobotParm6) {
                    fileLogger.writeEvent(3, "mblnRobotLastPos Complete Near END " + Math.abs(dblDistanceToEnd));
                    mintCurrentStateMoveLift = Constants.stepState.STATE_COMPLETE;
                    robotArms.setHardwareLiftPower(0);
                    deleteParallelStep();
                    break;
                }

                //we are close enough.. don't waste time
                if (Math.abs(dblDistanceToEnd) <= .25) {
                    fileLogger.writeEvent(3, "mblnRobotLastPos Complete Close enough " + Math.abs(dblDistanceToEnd));
                    mintCurrentStateMoveLift = Constants.stepState.STATE_COMPLETE;
                    robotArms.setHardwareLiftPower(0);
                    deleteParallelStep();
                    break;
                }

                if ((!(robotArms.liftMotor1.isBusy()))) {
                    //robotArms.setHardwareLiftPower(0);
                    //fileLogger.writeEvent(5, "LIFT MOTOR POWER IS 0");
                    fileLogger.writeEvent(2, "Finished");
                    mintCurrentStateMoveLift = Constants.stepState.STATE_COMPLETE;
                    robotArms.setHardwareLiftPower(0);
                    deleteParallelStep();
                    break;
                }
        }
                //check timeout value
                if (mStateTime.seconds() > mdblStepTimeout) {
                    robotArms.setHardwareLiftPower(0);
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateMoveLift = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
                break;
        }
    }
    private void SetIntake(){
        fileLogger.setEventTag("SetIntake()");

        switch (mintCurrentStateInTake){
            case STATE_INIT:
                fileLogger.writeEvent(2,"Initialised");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                robotArms.intakeMotor1.setPower(mdblStepSpeed);
                mintCurrentStateInTake = Constants.stepState.STATE_RUNNING;
                break;
            case STATE_RUNNING:
                robotArms.intakeMotor1.setPower(mdblStepSpeed);
                fileLogger.writeEvent(2,"Running");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                if (mdblRobotParm1 == 0) {
                    fileLogger.writeEvent(1,"Complete.......");
                    mintCurrentStateInTake = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                if (mStateTime.milliseconds() >= mdblRobotParm1)
                {
                    robotArms.intakeMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                    robotArms.intakeMotor1.setPower(0);
                    fileLogger.writeEvent(1,"Timer Complete.......");
                    mintCurrentStateInTake = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }//check timeout value

                if (mStateTime.seconds() > mdblStepTimeout) {
                    robotArms.intakeMotor1.setPower(0);
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateInTake = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;
        }
    }

    private void clawMovement(){
        fileLogger.setEventTag("clawMovement()");

        switch (mintCurrentStateClawMovement){
            case STATE_INIT:
                fileLogger.writeEvent(2,"Initialised");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                robotArms.foundationServo.setPosition(mdblStepSpeed);
                mintCurrentStateClawMovement = Constants.stepState.STATE_RUNNING;
                break;
            case STATE_RUNNING:
                robotArms.foundationServo.setPosition(mdblStepSpeed);
                fileLogger.writeEvent(2,"Running");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                fileLogger.writeEvent(1,"Complete.......");
                mintCurrentStateClawMovement = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
                break;
        }
    }
    private void ejector(){
        fileLogger.setEventTag("ejector()");

        switch (getMintCurrentStateEjector){
            case STATE_INIT:
                fileLogger.writeEvent(2,"Initialised");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                robotArms.ejector.setPosition(mdblStepSpeed);
                getMintCurrentStateEjector = Constants.stepState.STATE_RUNNING;
                break;
            case STATE_RUNNING:
                robotArms.ejector.setPosition(mdblStepSpeed);
                fileLogger.writeEvent(2,"Running");
                fileLogger.writeEvent(2,"Power: " + String.valueOf(mdblStepSpeed));
                fileLogger.writeEvent(1,"Complete.......");
                getMintCurrentStateEjector = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
                break;
        }
    }

    private void grabBlock(){
        fileLogger.setEventTag("GrabFoundation()");

        switch (mintCurrentStateGrabBlock){
            case STATE_INIT:
                fileLogger.writeEvent(2,"Initialised");
                fileLogger.writeEvent(2,"State: " + String.valueOf(mdblRobotParm1));
                fileLogger.writeEvent(2,"Timer: " + String.valueOf(mStateTime.milliseconds()));
                mintCurrentStateGrabBlock = Constants.stepState.STATE_RUNNING;
                break;
            case STATE_RUNNING:
                fileLogger.writeEvent(2,"Timer: " + String.valueOf(mStateTime.milliseconds()));
                switch ((int)mdblRobotParm1) {
                    case 0:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightWristServo.setPosition(1);
                            robotArms.rightArmServo.setPosition(0.0);
                            robotArms.rightClampServo.setPosition(0.15);
                        } else {
                            //blue alliance
                            robotArms.leftWristServo.setPosition(0.05);
                            robotArms.leftArmServo.setPosition(0);
                            robotArms.leftClampServo.setPosition(0.15);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 1:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightArmServo.setPosition(1);
                            robotArms.rightClampServo.setPosition(1);
                        } else {
                            //blue alliance
                            robotArms.leftArmServo.setPosition(1);
                            robotArms.leftClampServo.setPosition(1);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 2:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightClampServo.setPosition(0);
                        } else {
                            //blue alliance
                            robotArms.leftClampServo.setPosition(0);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 3:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightArmServo.setPosition(0.55);
                        } else {
                            //blue alliance
                            robotArms.leftArmServo.setPosition(0.55);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 4:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightWristServo.setPosition(0.25);
                        } else {
                            //blue alliance
                            robotArms.leftWristServo.setPosition(0.85);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 5:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightWristServo.setPosition(1);
                        } else {
                            //blue alliance
                            robotArms.leftWristServo.setPosition(0.05);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 8:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightArmServo.setPosition(1);
                        } else {
                            //blue alliance
                            robotArms.leftArmServo.setPosition(1);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                    case 9:
                        if (ourRobotConfig.getAllianceColor().equals("Red")) {
                            //red alliance
                            robotArms.rightClampServo.setPosition(0.6);
                        } else {
                            //blue alliance
                            robotArms.leftClampServo.setPosition(0.6);
                        }
                        mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                        deleteParallelStep();
                        break;
                }
                if (mStateTime.seconds() > mdblStepTimeout) {
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateGrabBlock = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;
        }
    }

    private void flywheel(){
        fileLogger.setEventTag("flywheel()");

        switch (mintCurrentStateFlywheel){
            case STATE_INIT:
                fileLogger.writeEvent(2, "Initialised");
                //fileLogger.writeEvent(2, "Power: " + String.valueOf(mdblStepSpeed));
                fileLogger.writeEvent(2, "Velocity: " + String.valueOf(mdblRobotParm2));
                robotArms.flywheelMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
                //robotArms.flywheelMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                //robotArms.flywheelMotor.setPower(mdblStepSpeed);
                robotArms.flywheelMotor.setVelocity(mdblRobotParm2);
                //robotArms.flywheelMotor.setPower(mdblStepSpeed);
                mintCurrentStateFlywheel = Constants.stepState.STATE_RUNNING;
                break;

            case STATE_RUNNING: {
                //robotArms.flywheelMotor.setPower(mdblStepSpeed);
                robotArms.flywheelMotor.setVelocity(mdblRobotParm2);
                fileLogger.writeEvent(2, "Running");
                double flywheelVelocity = robotArms.flywheelMotor.getVelocity();
                //fileLogger.writeEvent(2, "Power: " + String.valueOf(mdblStepSpeed));
                fileLogger.writeEvent(2, "Target Velocity: " + String.valueOf(mdblRobotParm2));
                fileLogger.writeEvent(2, "Actual Velocity: " + String.valueOf(flywheelVelocity));
                if (mdblRobotParm1 == 0) {
                    fileLogger.writeEvent(1, "Complete.......");
                    mintCurrentStateFlywheel = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;

                }

                if (mStateTime.milliseconds() >= mdblRobotParm1) {
                    robotArms.flywheelMotor.setPower(0);
                    fileLogger.writeEvent(1, "Timer Complete.......");
                    mintCurrentStateFlywheel = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
            }//check timeout value

                if (mStateTime.seconds() > mdblStepTimeout) {
                    robotArms.flywheelMotor.setPower(0);
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateFlywheel = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
                deleteParallelStep();
                break;
        }
    }








    private boolean findSkystone() {
        String tag = fileLogger.getEventTag();
        fileLogger.setEventTag("findSkystone()");
        fileLogger.writeEvent(3, "Initialised");
        boolean black = false;
        if (ourRobotConfig.getAllianceColor().equals("Red")) {
            //red alliance
            black = (sensors.distanceColorSideRight().alpha() / sensors.distanceColorSideRight().red()) > 2.9 ? true : false;
            fileLogger.writeEvent(3, "In Alliance RED, FOUND BLACK " + black);
        } else {
            //blue alliance
            black = (sensors.distanceColorSideLeft().alpha() / sensors.distanceColorSideLeft().red()) > 2.9 ? true : false;
            fileLogger.writeEvent(3, "In Alliance BLUE, FOUND BLACK " + black);
        }
        fileLogger.setEventTag(tag);
        return black;
    }

    private void pickStone (int stone) {
        String tag = fileLogger.getEventTag();
        fileLogger.setEventTag("pickStone()");
        fileLogger.writeEvent(3, "Initialised");
        switch (stone) {
            case 0:
                autonomousStepsFile.insertSteps(4, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 41, 2, mintCurrentStep + 1);
                break;
            case 1:
                autonomousStepsFile.insertSteps(4, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 33, 2, mintCurrentStep + 1);
                break;
            case 2:
                autonomousStepsFile.insertSteps(5, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 25, 2, mintCurrentStep + 1);
                break;
            case 3:
                autonomousStepsFile.insertSteps(5, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 17, 2, mintCurrentStep + 1);
                break;
            case 4:
                autonomousStepsFile.insertSteps(6, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 9, 2, mintCurrentStep + 1);
                break;
            case 5:
                autonomousStepsFile.insertSteps(6, "DRIVE", 70, 0.55, false, false, 0, 0, 0, 0, 1, 2, mintCurrentStep + 1);
                break;
            case 99:
                if (stones[0]) {
                    pickStone(0);
                } else if (stones[1]) {
                    pickStone(1);
                } else if (stones[2]) {
                    pickStone(2);
                } else if (stones[3]) {
                    pickStone(3);
                } else if (stones[4]){
                    pickStone(4);
                } else {
                    pickStone(5);
                }
                break;
        }
        stones[stone] = false;
        fileLogger.setEventTag(tag);
    }

    private void nextStone() {
        fileLogger.setEventTag("nextStone()");

        switch (mintCurrentStateNextStone) {
            case STATE_INIT:
                fileLogger.writeEvent(3,"Initialised");
                fileLogger.writeEvent(3,"Stone 0 " + stones[0]);
                fileLogger.writeEvent(3,"Stone 1 " + stones[1]);
                fileLogger.writeEvent(3,"Stone 2 " + stones[2]);
                fileLogger.writeEvent(3,"Stone 3 " + stones[3]);
                fileLogger.writeEvent(3,"Stone 4 " + stones[4]);
                fileLogger.writeEvent(3,"Stone 5 " + stones[5]);

                switch (mLocation) {
                    case OBJECT_SKYSTONE_LEFT:
                        //need to check which left was take, the first left or the second left
                        if (stones[0]) {
                            fileLogger.writeEvent(3,"LEFT Picking Stone 0");
                            pickStone(0);
                        } else if (stones[3]) {
                            fileLogger.writeEvent(3,"LEFT Picking Stone 3");
                            pickStone(3);
                        } else {
                            fileLogger.writeEvent(3,"LEFT Picking Next Closest ");
                            pickStone(99);
                        }
                        break;
                    case OBJECT_SKYSTONE_CENTER:
                        if (stones[1]) {
                            fileLogger.writeEvent(3,"CENTER Picking Stone 1");
                            pickStone(1);
                        } else if (stones[4]) {
                            fileLogger.writeEvent(3,"CENTER Picking Stone 4");
                            pickStone(4);
                        } else {
                            fileLogger.writeEvent(3,"CENTER Picking Next Closest ");
                            pickStone(99);
                        }
                        break;
                    case OBJECT_SKYSTONE_RIGHT:
                        if (stones[2]) {
                            fileLogger.writeEvent(3,"RIGHT Picking Stone 2");
                            pickStone(2);
                        } else if (stones[5]) {
                            fileLogger.writeEvent(3,"RIGHT Picking Stone 5");
                            pickStone(5);
                        } else {
                            fileLogger.writeEvent(3,"RIGHT Picking Next Closest ");
                            pickStone(99);
                        }
                        break;
                }
                mintCurrentStateNextStone = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
                //check timeout value
                if (mStateTime.seconds() > mdblStepTimeout) {
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStateNextStone = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;
        }
    }

    private void findGoldSS() {
        fileLogger.setEventTag("findGold()");
        switch (mintCurrentStepFindGoldSS){
            case STATE_INIT:
                if (mStateTime.milliseconds() > mdblRobotParm1) {
                    fileLogger.writeEvent(3, "Initialised");
 //                   mboolFoundSkyStone = false;
                    mColour = Constants.ObjectColours.OBJECT_NONE;
                    numberOfRings = Constants.ObjectColours.OBJECT_NONE;
 //                   mLocation = Constants.ObjectColours.OBJECT_NONE;
                    imageCaptureOCV.takeImage(new ImageCaptureOCV.OnImageCapture() {
                        @Override
                        public void OnImageCaptureVoid(Mat mat) {
                            //find Skystone Position
                            if (numberOfRings == Constants.ObjectColours.OBJECT_NONE)  //was quad 3
                                if (elementColour.UltimateGoalOCV(fileLogger, dashboard, mat, 0, false, 9, false) == Constants.ObjectColours.OBJECT_RED_FOUR_RING) {
       //                             mColour = Constants.ObjectColours.OBJECT_RED;
         //                           mLocation = Constants.ObjectColours.OBJECT_RED_CENTER;
                                    numberOfRings = Constants.ObjectColours.OBJECT_RED_FOUR_RING;
                                }
                                else if (elementColour.UltimateGoalOCV(fileLogger, dashboard, mat, 0, false, 9, false) == Constants.ObjectColours.OBJECT_RED_ONE_RING) {
                                numberOfRings = Constants.ObjectColours.OBJECT_RED_ONE_RING;
                            }
                     //       if (mColour == Constants.ObjectColours.OBJECT_NONE) { //was quad 4
                      //          if (elementColour.UltimateGoalOCV(fileLogger, dashboard, mat, 0, false, 8, false) == Constants.ObjectColours.OBJECT_RED) {
                      //              mColour = Constants.ObjectColours.OBJECT_RED;
                     //               mLocation = Constants.ObjectColours.OBJECT_RED_LEFT;
                      //          }
                      //     }
                        }
                    });
                    mintNumberColourTries = 0;
                    mintCurrentStepFindGoldSS = Constants.stepState.STATE_RUNNING;
                }
                break;
            case STATE_RUNNING:
                fileLogger.writeEvent(3, "Running");

                //check to see
                // if we found
                if ((numberOfRings == Constants.ObjectColours.OBJECT_RED_FOUR_RING) || (numberOfRings == Constants.ObjectColours.OBJECT_RED_ONE_RING) || (numberOfRings == Constants.ObjectColours.OBJECT_NONE)) {
                    fileLogger.writeEvent(1,"Image Processed:- " + numberOfRings.toString());

                    switch (numberOfRings){
                        case OBJECT_RED_FOUR_RING:
                            switch (ourRobotConfig.getAllianceStartPosition()) {
                                case "Left":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -80, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 28, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 14, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 60, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -35, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", 16, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -36, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Blue 4 Ring Left");
                                    } else {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -80, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 28, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -14, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 60, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -35, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", -16, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -36, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Red 4 Ring Left");
                                    }
                                    break;
                                case "Right":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -80, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //corner align
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 23, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 6, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);

                                        autonomousStepsFile.insertSteps(6, "DRIVE", 20, 0.75, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                       // autonomousStepsFile.insertSteps(6, "DRIVE", 28, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 14, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 43, 0.75, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        //autonomousStepsFile.insertSteps(6, "DRIVE", 60, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.6, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -35, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", 16, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -36, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                                        //Below is the experimental part 1
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);

                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 8, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);



                                        /*
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);

                                        */
                                        fileLogger.writeEvent(3, "Blue 4 Ring Right");

                                    }
                                    else {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -80, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 28, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -14, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 60, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -35, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", -16, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -36, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Red 4 Ring Right");
                                    }
                                        break;
                            }
                            break;
                        case OBJECT_RED_ONE_RING:
                            switch (ourRobotConfig.getAllianceStartPosition()) {
                                case "Left":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 8, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -19, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -62, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 47, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //was 0.5 drive
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        //was 0.5 drive
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -18, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        //was 0.5 drive, chg to 1.0, then back to 0.5
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Blue Left One Ring");
                                    } else {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 8, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 19, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -62, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 47, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //was 0.5 drive
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 25, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        //was 0.5 drive
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -18, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        //was 0.5 drive, chg to 1.0, then back to 0.5
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Red Left One Ring");
                                    }
                                    break;
                                case "Right":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(6, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 8, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        //maybe then drive rest of way
                                        //maybe shoot
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -19, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //maybe drive less
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -62, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //Added 2 lines below to line up in corner
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 23, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 6, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);

                                        //0.5   was 44 distance, reduced to add alignment to corner
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 22, 0.75, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                                        //maybe turn off intake here
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 21, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //0.5
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 26, 0.75, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                                        //maybe turn on intake here
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.6, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -21, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                                        //Below is the experimental part 1
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);

                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 8, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);



                                        //Below is the real part one
                                        /*autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "DELAY", 0, 0, false, false, 3000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1625, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -45, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        */
                                        fileLogger.writeEvent(3, "Blue Right One Ring");
                                    }
                                    else {
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 8, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 19, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -62, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 47, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 28, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -21, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Red Right One Ring");
                                    }
                                    break;
                            }
                            break;
                        default:  //right side, unfortunately we don't sample the right side so if its not left or center we assume right
                            switch (ourRobotConfig.getAllianceStartPosition()) {
                                case "Left":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -20, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -12, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 10, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -41, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 46, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", 20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Default Blue Left");
                                    }
                                    else {
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -20, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 12, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 10, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -41, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 6, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -23, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 22, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 46, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", -20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -13, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Default Red Left");
                                    }
                                    break;
                                case "Right":
                                    if (ourRobotConfig.getAllianceColor().equals("Blue")) {
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -20, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -12, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 10, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -39, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        //Adjust drive back to corner from 20 to 23
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 26, 0.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 6, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 24, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.6, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", 20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);

                                        //Below is the experimental part 1
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);

                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 8, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);


                                        /*
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "DELAY", 0, 0, false, false, 3000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1625, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -45, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        */
                                        fileLogger.writeEvent(3, "Default Blue Right");
                                    }
                                    else  {
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -20, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 12, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 80, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 10, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -39, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", -10, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 750, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", -60, -1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", 46, 1.0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "CLAW", 0, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "LIFT", 40, 1, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "CLAW", 0, 0.7, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "LIFT", -55, -0.5, false, false, 0, 0, 0, 0, 0, 5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(3, "STRAFE", -20, 0.5, false, false, 0, 0, 0, 0, 0, 1.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 1000, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "EJECTOR", 0, 0.3, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(2, "DELAY", 0, 0, false, false, 1500, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "FLYWHEEL", 0, 0, false, false, 5000, -1600, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(1, "TANKTURNGYRO", 0, 0.5, false, false, 1, 0, 0, 0, 0, 1, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(4, "STRAFE", 9, 0.5, false, false, 0, 0, 0, 0, 0, 0.5, mintCurrentStep + 1);
                                        autonomousStepsFile.insertSteps(6, "DRIVE", -46, 0.5, false, false, 1, 0, 0.3, 0, 0, 0.5, mintCurrentStep + 1);
                                        fileLogger.writeEvent(3, "Default Red Right");
                                        }
                                    break;
                            }
                            break;
                    }

                    //  Transition to a new state.
                    mintCurrentStepFindGoldSS = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                //check timeout value
                if (mStateTime.seconds() > mdblStepTimeout) {
                    fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                    //  Transition to a new state.
                    mintCurrentStepFindGoldSS = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
                break;
        }
    }

    private void VuforiaMove ()
    {
        int intCorrectionAngle = 0;
        String strCorrectionCommand = "DELAY";

        fileLogger.setEventTag("VuforiaMove()");
        switch (mintCurStVuforiaMove5291) {
            case STATE_INIT: {
                //ensure vision processing is enable
                mblnDisableVisionProcessing = false;  //enable vision processing

                mintCurStVuforiaMove5291 = Constants.stepState.STATE_RUNNING;
                fileLogger.writeEvent(2, "Initialised");
            }
            break;
            case STATE_RUNNING:
            {
                fileLogger.writeEvent(2, "Running" );
                fileLogger.writeEvent(2, "localiseRobotPos " + localiseRobotPos );

                if (!localiseRobotPos)
                {
                    //need to do something gere to try and get localise
                    mintCurStVuforiaMove5291 = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }

                int currentX = (int)localisedRobotX;
                int currentY = (int)localisedRobotY;
                int intLocalisedRobotBearing = (int)localisedRobotBearing;
                double requiredMoveX = (currentX - (int)mdblRobotParm4);
                double requiredMoveY = (currentY - (int)mdblRobotParm5);

                double requiredMoveDistance = ((Math.sqrt(requiredMoveX * requiredMoveX + requiredMoveY * requiredMoveY)) / 25.4);
                double requiredMoveAngletemp1 = requiredMoveX/requiredMoveY;
                double requiredMoveAngletemp2 = Math.atan(requiredMoveAngletemp1);
                double requiredMoveAngletemp3 = Math.toDegrees(requiredMoveAngletemp2);
                int requiredMoveAngle = (int)Math.abs(requiredMoveAngletemp3);

                fileLogger.writeEvent(2,"Temp Values requiredMoveAngletemp1 " + requiredMoveAngletemp1 + " requiredMoveAngletemp2 " + requiredMoveAngletemp2 + " requiredMoveAngletemp3 " + requiredMoveAngletemp3);
                fileLogger.writeEvent(2,"Temp Values currentX " + currentX + " currentY " + currentY);
                fileLogger.writeEvent(2,"Localised, determining angles....Alliancecolour= " + ourRobotConfig.getAllianceColor() + " intLocalisedRobotBearing= " + intLocalisedRobotBearing + " CurrentX= " + currentX + " CurrentY= " + currentY);
                fileLogger.writeEvent(2,"Localised, determining angles....requiredMoveX " + requiredMoveX + " requiredMoveY " + requiredMoveY);
                fileLogger.writeEvent(2,"Localised, determining angles....requiredMoveDistance " + requiredMoveDistance + " requiredMoveAngle " + requiredMoveAngle);

                if ((((int) mdblRobotParm5) > currentY) && ((int) mdblRobotParm4 > currentX)) {
                    requiredMoveAngle = 90 - requiredMoveAngle;
                } else if ((((int) mdblRobotParm5) > currentY) && ((int) mdblRobotParm4 < currentX)) {
                    requiredMoveAngle =  90 + requiredMoveAngle;
                } else if ((((int) mdblRobotParm5) < currentY) && ((int) mdblRobotParm4 > currentX)) {
                    requiredMoveAngle = 270 + requiredMoveAngle;
                } else if ((((int) mdblRobotParm5) < currentY) && ((int) mdblRobotParm4 < currentX)) {
                    requiredMoveAngle = 270 - requiredMoveAngle;
                }

                intCorrectionAngle = TOWR5291Utils.getNewHeading((int)localisedRobotBearing, (int)mdblRobotParm1);

                autonomousStepsFile.insertSteps(3, "DRIVE", requiredMoveDistance,0.6, false, false, 0, 0, 0, 0, 0, 0,  mintCurrentStep + 1);
                if (intCorrectionAngle < 0) {
                    autonomousStepsFile.insertSteps(3, "LEFTTURN",  intCorrectionAngle,.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                } else if (intCorrectionAngle > 0) {
                    autonomousStepsFile.insertSteps(3, "RIGHTTURN", intCorrectionAngle,.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                }
                mintCurStVuforiaMove5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout)
            {
                fileLogger.writeEvent(1,"Timeout:- "  + mStateTime.seconds());
                //  Transition to a new state.
                mintCurStVuforiaMove5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private void VuforiaTurn ()
    {
        fileLogger.setEventTag("VuforiaTurn()");
        int intCorrectionAngle;
        switch (mintCurStVuforiaTurn5291) {
            case STATE_INIT: {
                //ensure vision processing is enabled
                mblnDisableVisionProcessing     = false;  //enable vision processing
                mintCurStVuforiaTurn5291        = Constants.stepState.STATE_RUNNING;
                fileLogger.writeEvent(2,"Initialised");
            }
            break;
            case STATE_RUNNING:
            {
                fileLogger.writeEvent(2,"Running" );
                fileLogger.writeEvent(2,"localiseRobotPos " + localiseRobotPos );

                if (!localiseRobotPos)
                {
                    //need to do something here to try and get localised
                    mintCurStVuforiaTurn5291    = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                    break;
                }
                intCorrectionAngle = TOWR5291Utils.getNewHeading((int)localisedRobotBearing, (int)mdblRobotParm1);
                if (intCorrectionAngle < 0) {
                    autonomousStepsFile.insertSteps(3, "LEFTTURN", intCorrectionAngle,.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);

                } else if (intCorrectionAngle > 0) {
                    autonomousStepsFile.insertSteps(3, "RIGHTTURN", intCorrectionAngle,.5, false, false, 0, 0, 0, 0, 0, 0, mintCurrentStep + 1);
                }
                fileLogger.writeEvent(2,"Localised, determining angles....Alliancecolour= " + ourRobotConfig.getAllianceColor() + " localisedRobotBearing= " + localisedRobotBearing  + " requiredMoveAngle " + mdblRobotParm1);
                mintCurStVuforiaTurn5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout)
            {
                fileLogger.writeEvent(1, "Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurStVuforiaTurn5291 = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private void DelayStep ()
    {
        fileLogger.setEventTag("DelayStep()");
        switch (mintCurrentStepDelay) {
            case STATE_INIT: {
                mintStepDelay = (int)(mdblRobotParm1);
                mintCurrentStepDelay = Constants.stepState.STATE_RUNNING;
                fileLogger.writeEvent(3,"Init Delay Time......." + mintStepDelay);
            }
            break;
            case STATE_RUNNING:
            {
                if (mStateTime.milliseconds() >= mintStepDelay)
                {
                    fileLogger.writeEvent(1,"Complete.......");
                    mintCurrentStepDelay = Constants.stepState.STATE_COMPLETE;
                    deleteParallelStep();
                }
            }
            //check timeout value
            if (mStateTime.seconds() > mdblStepTimeout)
            {
                fileLogger.writeEvent(1,"Timeout:- " + mStateTime.seconds());
                //  Transition to a new state.
                mintCurrentStepDelay = Constants.stepState.STATE_COMPLETE;
                deleteParallelStep();
            }
            break;
        }
    }

    private double teamAngleAdjust ( double angle ) {
        fileLogger.setEventTag("teamAngleAdjust()");
        fileLogger.writeEvent(2,"teamAngleAdjust - angle " + angle + " allianceColor " + ourRobotConfig.getAllianceColor());

        if (ourRobotConfig.getAllianceColor().equals("Red")) {
            //angle = angle + 90;  if starting against the wall
            //angle = angle + 225; if starting at 45 to the wall facing the beacon
            angle = angle + 0;
            if (angle > 360) {
                angle = angle - 360;
            //angle = angle +0;
        }
            fileLogger.writeEvent(2,"In RED Angle " + angle);

        } else
        if (ourRobotConfig.getAllianceColor().equals("Blue")) {
            //angle = angle - 180;;  if starting against the wall
            //angle = angle - 135;
            if (angle < 0) {
                angle = angle + 360;
            //angle = angle +0;
            }
        }
        return angle;
    }

    private double newAngleDirectionGyroOffset (int currentDirection, int newDirection) {
        fileLogger.setEventTag("newAngleDirectionGyroOffset()");
        int intAngle1;
        //calculate the smallest angle
        //if (currentDirection < newDirection) {
        intAngle1 = (newDirection - currentDirection);
        if (intAngle1 > 180)
        {
            intAngle1 = (currentDirection + 360 - newDirection);
            return intAngle1;
        }
        return intAngle1;
        //}
        //else
        //{
        //    intAngle1 = (currentDirection - newDirection);
        //    if (intAngle1 > 180)
        //    {
        //        intAngle1 = (newDirection + 360 - currentDirection);
        //        return -intAngle1;
        //    }
        //    return intAngle1;
        //}
    }

    private String newAngleDirectionGyro (int currentDirection, int newDirection)
    {
        fileLogger.setEventTag("newAngleDirectionGyro()");
        int intAngle1;

        //calculate the smallest angle
        if (currentDirection < newDirection) {
            intAngle1 = (newDirection - currentDirection);
            if (intAngle1 > 180)
            {
                intAngle1 = (currentDirection + 360 - newDirection);
                return "-" + intAngle1;
            }
            return "+" + intAngle1;
        }
        else
        {
            intAngle1 = (currentDirection - newDirection);
            if (intAngle1 > 180)
            {
                intAngle1 = (newDirection + 360 - currentDirection);
                return "+" + intAngle1;
            }
            return "-" + intAngle1;
        }
    }

    private String format(OpenGLMatrix transformationMatrix) {
        return transformationMatrix.formatAsTransform();
    }

    /**
     * Converts a reading of the optical sensor into centimeters. This computation
     * could be adjusted by altering the numeric parameters, or by providing an alternate
     * calculation in a subclass.
     */
    private double cmFromOptical(int opticalReading)
    {
        double pParam = -1.02001;
        double qParam = 0.00311326;
        double rParam = -8.39366;
        int    sParam = 10;

        if (opticalReading < sParam)
            return 0;
        else
            return pParam * Math.log(qParam * (rParam + opticalReading));
    }

    private int cmUltrasonic(int rawUS)
    {
        return rawUS;
    }

    private double cmOptical(int rawOptical)
    {
        return cmFromOptical(rawOptical);
    }

    public double getDistance(int rawUS, int rawOptical, DistanceUnit unit)
    {
        double cmOptical = cmOptical(rawOptical);
        double cm        = cmOptical > 0 ? cmOptical : cmUltrasonic(rawUS);
        return unit.fromUnit(DistanceUnit.CM, cm);
    }

    /**
     * getError determines the error between the target angle and the robot's current heading
     * @param   targetAngle  Desired angle (relative to global reference established at last Gyro Reset).
     * @return  error angle: Degrees in the range +/- 180. Centered on the robot's frame of reference
     *          +ve error means the robot should turn LEFT (CCW) to reduce error.
     */
    private double getDriveError(double targetAngle) {
        fileLogger.setEventTag("getDriveError()");
        double robotError;
        double robotErrorIMU;
        double adafruitIMUHeading;

        adafruitIMUHeading = getAdafruitHeading();

        fileLogger.writeEvent(2,"targetAngle " + targetAngle);
        fileLogger.writeEvent(2,"Adafruit IMU Reading " + adafruitIMUHeading);
        // calculate error in -179 to +180 range  (
        robotErrorIMU = targetAngle - teamAngleAdjust(adafruitIMUHeading);
        robotError = robotErrorIMU;
        fileLogger.writeEvent(2,"robotErrorIMU " + robotError + ", getAdafruitHeading() " + adafruitIMUHeading + " teamAngleAdjust(adafruitIMUHeading) "  + teamAngleAdjust(adafruitIMUHeading));

        if (robotError > 180)
            robotError -= 360;
        if (robotError <= -180)
            robotError += 360;

        fileLogger.writeEvent(2,"robotError2 " + robotError);
        return robotError;
    }

    /**
     * returns desired steering force.  +/- 1 range.  +ve = steer left
     * @param error   Error angle in robot relative degrees
     * @param PCoeff  Proportional Gain Coefficient
     */
    private double getDriveSteer(double error, double PCoeff) {
        return Range.clip(error * PCoeff, -1, 1);
    }

    private Double getAdafruitPitch ()
    {
        Orientation angles;
        angles = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZYX);

        if (imuMountCorrectionVar == 90) {
            return formatAngle(angles.angleUnit, angles.thirdAngle);
        } else {
            return formatAngle(angles.angleUnit, angles.secondAngle);
        }
    }

    private Double getAdafruitHeading()
    {
        Orientation angles;
        angles = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZYX);
        return angleToHeading(formatAngle(angles.angleUnit, angles.firstAngle));
    }

    //for adafruit IMU
    private Double formatAngle(AngleUnit angleUnit, double angle) {
        return AngleUnit.DEGREES.fromUnit(angleUnit, angle);
    }

    //for adafruit IMU as it returns z angle only
    private double angleToHeading(double z) {
        double angle = -z;// + imuStartCorrectionVar + imuMountCorrectionVar;
        if (angle < 0)
            return angle + 360;
        else if (angle > 360)
            return angle - 360;
        else
            return angle;
    }

    // Computes the current battery voltage
    double getBatteryVoltage() {
        double result = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double voltage = sensor.getVoltage();
            if (voltage > 0) {
                result = Math.min(result, voltage);
            }
        }
        return result;
    }

    private boolean checkAllStatesComplete () {
        if ((mintCurrentStepDelay                   == Constants.stepState.STATE_COMPLETE) &&
                (mintCurStVuforiaTurn5291               == Constants.stepState.STATE_COMPLETE) &&
                (mintCurStVuforiaMove5291               == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateDrive                  == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateDriveHeading           == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStatePivotTurn              == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateTankTurn               == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateEyes5291               == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateGyroTurnEncoder5291    == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateTankTurnGyroHeading    == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateMecanumStrafe          == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateMoveLift               == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateInTake                 == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateNextStone              == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateRadiusTurn             == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateWyattsGyroDrive        == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateClawMovement           == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateTapeMeasure            == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStepFindGoldSS              == Constants.stepState.STATE_COMPLETE) &&
                (mintCurrentStateGrabBlock              == Constants.stepState.STATE_COMPLETE)) {
            return true;
        }
        return false;
    }

    private void initDefaultStates() {
        mintCurrentStateStep                = Constants.stepState.STATE_INIT;
        mintCurrentStepDelay                = Constants.stepState.STATE_COMPLETE;
        mintCurStVuforiaMove5291            = Constants.stepState.STATE_COMPLETE;
        mintCurStVuforiaTurn5291            = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateDrive               = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateDriveHeading        = Constants.stepState.STATE_COMPLETE;
        mintCurrentStatePivotTurn           = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateTankTurn            = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateEyes5291            = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateGyroTurnEncoder5291 = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateTankTurnGyroHeading = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateMecanumStrafe       = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateMoveLift            = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateInTake              = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateNextStone            = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateRadiusTurn          = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateWyattsGyroDrive     = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateClawMovement        = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateTapeMeasure         = Constants.stepState.STATE_COMPLETE;
        mintCurrentStepFindGoldSS           = Constants.stepState.STATE_COMPLETE;
        mintCurrentStateGrabBlock           = Constants.stepState.STATE_COMPLETE;
    }
}
