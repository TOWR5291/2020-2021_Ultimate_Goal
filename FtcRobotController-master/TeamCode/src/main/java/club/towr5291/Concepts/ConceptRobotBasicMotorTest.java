package club.towr5291.Concepts;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import club.towr5291.functions.FileLogger;
import club.towr5291.libraries.LibraryMotorType;
import club.towr5291.libraries.robotConfigSettings;
import club.towr5291.robotconfig.HardwareArmMotors;
import club.towr5291.robotconfig.HardwareArmMotorsSkyStone;
import club.towr5291.robotconfig.HardwareDriveMotors;

/**
 * Created by lztdd0 on 11/5/17.
 */


/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@TeleOp(name = "Concept: Robot Base Motors Test", group = "Concepts")
//@Disabled
public class ConceptRobotBasicMotorTest extends LinearOpMode {
     /*
     * The REV Robotics Touch Sensor
     * is treated as a digital channel.  It is HIGH if the button is unpressed.
     * It pulls LOW if the button is pressed.
     *
     * Also, when you connect a REV Robotics Touch Sensor to the digital I/O port on the
     * Expansion Hub using a 4-wire JST cable, the second pin gets connected to the Touch Sensor.
     * The lower (first) pin stays unconnected.*
     */

    //motors
    // Declare OpMode members.
    private HardwareDriveMotors robotDrive      = new HardwareDriveMotors();   // Use a Pushbot's hardware
    private HardwareArmMotorsSkyStone robotArms          = new HardwareArmMotorsSkyStone();

    //mode selection stuff
    public int mode = 0;

    //all modes variables
    public double dblLeftMotor1;
    public double dblLeftMotor2;
    public double dblRightMotor1;
    public double dblRightMotor2;

    //The autonomous menu settings from the sharepreferences
    private SharedPreferences sharedPreferences;
    private String teamNumber;
    private String allianceColor;
    private String allianceStartPosition;
    private int delay;
    private String robotConfig;
    private ElapsedTime runtime = new ElapsedTime();
    private String motorType;

    //set up the variables for file logger and what level of debug we will log info at
    private FileLogger fileLogger;
    private int debug = 3;

    @Override
    public void runOpMode() {

        //load menu settings and setup robot and debug level
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(hardwareMap.appContext);
        teamNumber = sharedPreferences.getString("club.towr5291.Autonomous.TeamNumber", "0000");
        allianceColor = sharedPreferences.getString("club.towr5291.Autonomous.Color", "Red");
        allianceStartPosition = sharedPreferences.getString("club.towr5291.Autonomous.Position", "Left");
        delay = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Delay", "0"));
        robotConfig = sharedPreferences.getString("club.towr5291.Autonomous.RobotConfigBase", "TileRunner2x40");
        motorType = sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorChoice", "REV20ORBIT");
        debug = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "3"));

        fileLogger = new FileLogger(runtime, Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "1")), true);// initializing FileLogger
        fileLogger.open();// Opening FileLogger
        fileLogger.writeEvent("TEST", "Log Started");// First Line Add To Log

        robotDrive.init(fileLogger,hardwareMap, robotConfigSettings.robotConfigChoice.valueOf(robotConfig), LibraryMotorType.MotorTypes.valueOf(motorType));
        robotArms.init(hardwareMap, null);

        robotDrive.setHardwareDriveRunWithoutEncoders();

        PIDFCoefficients getMotorPIDFMotor1;
        PIDFCoefficients getMotorPIDFMotor2;
        PIDFCoefficients getMotorPIDFMotor3;
        PIDFCoefficients getMotorPIDFMotor4;
        // wait for the start button to be pressed.
        waitForStart();

        getMotorPIDFMotor1 = robotDrive.getMotorPIDF(1);
        getMotorPIDFMotor2 = robotDrive.getMotorPIDF(2);
        getMotorPIDFMotor3 = robotDrive.getMotorPIDF(3);
        getMotorPIDFMotor4 = robotDrive.getMotorPIDF(4);

        // while the op mode is active, loop and read the light levels.
        // Note we use opModeIsActive() as our loop condition because it is an interruptible method.
        while (opModeIsActive()) {

            dblLeftMotor1 = Range.clip(gamepad1.left_stick_y, -1.0, 1.0);
            dblLeftMotor2 = Range.clip(gamepad1.left_stick_x , -1.0, 1.0);
            dblRightMotor1 = Range.clip(gamepad1.right_stick_y , -1.0, 1.0);
            dblRightMotor2 = Range.clip(gamepad1.right_stick_x , -1.0, 1.0);

            robotDrive.setHardwareDrivePower(dblLeftMotor1, dblLeftMotor2, dblRightMotor1, dblRightMotor2);
            telemetry.clearAll();
            robotArms.leftWristServo.setPosition(1);
            robotArms.rightWristServo.setPosition(.60);


            robotArms.leftArmServo.setPosition(.7);
            robotArms.rightArmServo.setPosition(0);

            robotArms.leftClampServo.setPosition(0.1);
            robotArms.rightClampServo.setPosition(0.3);

            telemetry.addLine("Motor1 " + robotDrive.baseMotor1.getCurrentPosition());
            telemetry.addLine("Motor2 " + robotDrive.baseMotor2.getCurrentPosition());
            telemetry.addLine("Motor3 " + robotDrive.baseMotor3.getCurrentPosition());
            telemetry.addLine("Motor4 " + robotDrive.baseMotor4.getCurrentPosition());
            telemetry.addData("P,I,D,F (orig Motor 1)", "%.04f, %.04f, %.0f, %.0f", getMotorPIDFMotor1.p, getMotorPIDFMotor1.i, getMotorPIDFMotor1.d, getMotorPIDFMotor1.f);
            telemetry.addData("P,I,D,F (orig Motor 2)", "%.04f, %.04f, %.0f, %.0f", getMotorPIDFMotor2.p, getMotorPIDFMotor2.i, getMotorPIDFMotor2.d, getMotorPIDFMotor2.f);
            telemetry.addData("P,I,D,F (orig Motor 3)", "%.04f, %.04f, %.0f, %.0f", getMotorPIDFMotor3.p, getMotorPIDFMotor3.i, getMotorPIDFMotor3.d, getMotorPIDFMotor3.f);
            telemetry.addData("P,I,D,F (orig Motor 4)", "%.04f, %.04f, %.0f, %.0f", getMotorPIDFMotor4.p, getMotorPIDFMotor4.i, getMotorPIDFMotor4.d, getMotorPIDFMotor4.f);
            telemetry.addLine("Left Wrist " + robotArms.leftWristServo.getPosition());
            telemetry.addLine("Left Arm   " + robotArms.leftArmServo.getPosition());
            telemetry.addLine("Left Grab  " + robotArms.leftClampServo.getPosition());
            telemetry.addLine("Left Wrist " + robotArms.rightWristServo.getPosition());
            telemetry.addLine("Left Arm   " + robotArms.rightArmServo.getPosition());
            telemetry.addLine("Left Grab  " + robotArms.rightClampServo.getPosition());

            telemetry.update();
        }
    }
}


