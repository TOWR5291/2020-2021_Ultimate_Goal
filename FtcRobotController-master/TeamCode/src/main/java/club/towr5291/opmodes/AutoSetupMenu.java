package club.towr5291.opmodes;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.firstinspires.ftc.teamcode.R;

import club.towr5291.functions.FileLogger;
import club.towr5291.libraries.LibraryMotorType;
import club.towr5291.libraries.robotConfigSettings;

import club.towr5291.libraries.FTCChoiceMenu;
import club.towr5291.libraries.FTCMenu;
import club.towr5291.libraries.FTCValueMenu;
import club.towr5291.libraries.TOWRDashBoard;

/**
 * Created by Ian Haden on 11/7/2016.
 */

@TeleOp(name = "**Configure Robot**", group = "0")
//@Disabled
public class AutoSetupMenu extends OpModeMasterLinear implements FTCMenu.MenuButtons {

    //set up the variables for the logger
    final String TAG = "Auton Menu";
    private ElapsedTime runtime = new ElapsedTime();
    private FileLogger fileLogger;
    private int debug = 3;

    private int loop = 0;

    private static TOWRDashBoard dashboard = null;

    public static TOWRDashBoard getDashboard()
    {
        return dashboard;
    }

    //The autonomous menu settings using sharedpreferences
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String teamNumber;
    private String allianceColor;
    private String allianceStartPosition;
    private String allianceParkPosition;
    private int delay;
    private String robotConfigBase;
    private String robotMotorType;
    private int robotMotorRatio;
    private String robotMotorDirection;

    private static AutoSetupMenu instance = null;

    public AutoSetupMenu()
    {
        super();
        instance = this;
    }

    //
    // Implements FtcMenu.MenuButtons interface.
    //

    @Override
    public boolean isMenuUpButton() { return gamepad1.dpad_up;} //isMenuUpButton

    @Override
    public boolean isMenuDownButton()
    {
        return gamepad1.dpad_down;
    } //isMenuDownButton

    @Override
    public boolean isMenuEnterButton()
    {
        return gamepad1.a;
    } //isMenuEnterButton

    @Override
    public boolean isMenuBackButton()
    {
        return gamepad1.dpad_left;
    }  //isMenuBackButton

    private void doMenus()
    {

        //load variables
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(hardwareMap.appContext);
        editor = sharedPreferences.edit();
        teamNumber = sharedPreferences.getString("club.towr5291.Autonomous.TeamNumber", "0000");
        allianceColor = sharedPreferences.getString("club.towr5291.Autonomous.Color", "Red");
        allianceStartPosition = sharedPreferences.getString("club.towr5291.Autonomous.Position", "Left");
        delay = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Delay", "0"));
        robotConfigBase = sharedPreferences.getString("club.towr5291.Autonomous.RobotConfigBase", "Custom_Mecanum");
        robotMotorType = sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorType", "Rev");
        robotMotorRatio = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorRatio", "15"));
        robotMotorDirection = sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorDirection", "Forward");
        debug = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "1"));

        //
        // Create the menus.
        //
        FTCChoiceMenu teamMenu                  = new FTCChoiceMenu("robotConfigTeam:", null, this);
        FTCChoiceMenu allianceMenu              = new FTCChoiceMenu("Alliance:", teamMenu, this);
        FTCChoiceMenu startPosMenu              = new FTCChoiceMenu("Start:", allianceMenu, this);
        FTCValueMenu delayMenu                  = new FTCValueMenu("Delay:", startPosMenu, this, 0.0, 20.0, 1.0, delay, "%1f");
        FTCChoiceMenu robotConfigMenu           = new FTCChoiceMenu("Robot Base:", delayMenu, this);
        FTCChoiceMenu robotMotorMenu            = new FTCChoiceMenu("Robot Motor:", robotConfigMenu, this);
        FTCValueMenu robotMotorRatioMenu        = new FTCValueMenu("Robot Ratio:", robotMotorMenu, this, 0.0, 100.0, 1.0, robotMotorRatio, "%1f");
        FTCChoiceMenu robotMotorDirectionMenu   = new FTCChoiceMenu("Robot Ratio:", robotMotorRatioMenu, this);
        //FTCChoiceMenu debugConfigMenu           = new FTCChoiceMenu("Debug:", robotMotorDirectionMenu, this);
        FTCValueMenu debugConfigMenu            = new FTCValueMenu("Debug:", robotMotorDirectionMenu, this, 0.0, 20.0, 1.0, debug, "%1f");

        //
        // remember last saved settings and reorder the menu with last run settings as the defaults
        //
        if (teamNumber.equals(robotConfigSettings.robotConfigTeam.TOWR.toString())) {
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.TOWR.toString(), robotConfigSettings.robotConfigTeam.TOWR, true, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.CYBORGCATZ.toString(), robotConfigSettings.robotConfigTeam.CYBORGCATZ, false, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.ELECTCATZ.toString(), robotConfigSettings.robotConfigTeam.ELECTCATZ, false, allianceMenu);
        } else if (teamNumber.equals(robotConfigSettings.robotConfigTeam.CYBORGCATZ.toString())) {
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.CYBORGCATZ.toString(), robotConfigSettings.robotConfigTeam.CYBORGCATZ, true, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.TOWR.toString(), robotConfigSettings.robotConfigTeam.TOWR, false, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.ELECTCATZ.toString(), robotConfigSettings.robotConfigTeam.ELECTCATZ, false, allianceMenu);
        } else {
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.ELECTCATZ.toString(), robotConfigSettings.robotConfigTeam.ELECTCATZ, true, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.CYBORGCATZ.toString(), robotConfigSettings.robotConfigTeam.CYBORGCATZ, false, allianceMenu);
            teamMenu.addChoice(robotConfigSettings.robotConfigTeam.TOWR.toString(), robotConfigSettings.robotConfigTeam.TOWR, false, allianceMenu);
        }

        if (allianceColor.equals(robotConfigSettings.Alliance.RED.toString())) {
            allianceMenu.addChoice(robotConfigSettings.Alliance.RED.toString(),  robotConfigSettings.Alliance.RED, true, startPosMenu);
            allianceMenu.addChoice(robotConfigSettings.Alliance.BLUE.toString(), robotConfigSettings.Alliance.BLUE, false, startPosMenu);
        } else  {
            allianceMenu.addChoice(robotConfigSettings.Alliance.BLUE.toString(), robotConfigSettings.Alliance.BLUE, true, startPosMenu);
            allianceMenu.addChoice(robotConfigSettings.Alliance.RED.toString(),  robotConfigSettings.Alliance.RED, false, startPosMenu);
        }

        if (allianceStartPosition.equals(robotConfigSettings.robotConfigStartPos.START_LEFT.toString())) {
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_LEFT.toString(), robotConfigSettings.robotConfigStartPos.START_LEFT, true, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_RIGHT.toString(), robotConfigSettings.robotConfigStartPos.START_RIGHT, false, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_TEST.toString(), robotConfigSettings.robotConfigStartPos.START_TEST, false, delayMenu);
        } else if (allianceStartPosition.equals(robotConfigSettings.robotConfigStartPos.START_RIGHT.toString())) {
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_RIGHT.toString(), robotConfigSettings.robotConfigStartPos.START_RIGHT, true, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_LEFT.toString(), robotConfigSettings.robotConfigStartPos.START_LEFT, false, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_TEST.toString(), robotConfigSettings.robotConfigStartPos.START_TEST, false, delayMenu);
        } else {
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_TEST.toString(), robotConfigSettings.robotConfigStartPos.START_TEST, true, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_LEFT.toString(), robotConfigSettings.robotConfigStartPos.START_LEFT, false, delayMenu);
            startPosMenu.addChoice(robotConfigSettings.robotConfigStartPos.START_RIGHT.toString(), robotConfigSettings.robotConfigStartPos.START_RIGHT, false, delayMenu);
        }

        delayMenu.setChildMenu(robotConfigMenu);

        if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital,false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        } else if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital,false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        } else if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        } else if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        } else if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        } else if (robotConfigBase.equals(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString())) {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital, false, robotMotorMenu);
        } else {
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegular.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegular, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.Custom_Mecanum.toString(), robotConfigSettings.robotConfigChoice.Custom_Mecanum, true, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerRegularOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanum.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanum, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital.toString(), robotConfigSettings.robotConfigChoice.TileRunnerMecanumOrbital, false, robotMotorMenu);
            robotConfigMenu.addChoice(robotConfigSettings.robotConfigChoice.TankTread2x40Custom.toString(), robotConfigSettings.robotConfigChoice.TankTread2x40Custom, false, robotMotorMenu);
        }

        if (robotMotorType.equals(LibraryMotorType.MotorTypes.REV01ORBIT.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.REV20ORBIT.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.ANDY20SPUR.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.ANDY40SPUR.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.ANDY60SPUR.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.REV20SPUR.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
        } else if (robotMotorType.equals(LibraryMotorType.MotorTypes.REV40SPUR.toString())){
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
        } else {
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20SPUR.toString(), LibraryMotorType.MotorTypes.ANDY20SPUR, true, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY40SPUR.toString(), LibraryMotorType.MotorTypes.ANDY40SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY60SPUR.toString(), LibraryMotorType.MotorTypes.ANDY60SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY3_7ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY3_7ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.ANDY20ORBIT.toString(), LibraryMotorType.MotorTypes.ANDY20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV01ORBIT.toString(), LibraryMotorType.MotorTypes.REV01ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20ORBIT.toString(), LibraryMotorType.MotorTypes.REV20ORBIT, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV20SPUR.toString(), LibraryMotorType.MotorTypes.REV20SPUR, false, robotMotorRatioMenu);
            robotMotorMenu.addChoice(LibraryMotorType.MotorTypes.REV40SPUR.toString(), LibraryMotorType.MotorTypes.REV40SPUR, false, robotMotorRatioMenu);
        }

        robotMotorRatioMenu.setChildMenu(robotMotorDirectionMenu);

        if (robotMotorDirection.equals("Forward")) {
            robotMotorDirectionMenu.addChoice("Forward", "Forward" , true, debugConfigMenu);
            robotMotorDirectionMenu.addChoice("Reverse", "Reverse", false, debugConfigMenu);
        } else  {
            robotMotorDirectionMenu.addChoice("Reverse", "Reverse", true, debugConfigMenu);
            robotMotorDirectionMenu.addChoice("Forward",  "Forward", false, debugConfigMenu);
        }

/*
        switch (debug) {
            case 1:
                debugConfigMenu.addChoice("1", 1, true);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 2:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, true);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 3:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, true);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 4:
                debugConfigMenu.addChoice("1", 1, true);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, true);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 5:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, true);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 6:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, true);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 7:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, true);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 8:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, true);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 9:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, true);
                debugConfigMenu.addChoice("10", 10, false);
                break;
            case 10:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, false);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, true);
                break;
            default:
                debugConfigMenu.addChoice("1", 1, false);
                debugConfigMenu.addChoice("2", 2, false);
                debugConfigMenu.addChoice("3", 3, false);
                debugConfigMenu.addChoice("4", 4, true);
                debugConfigMenu.addChoice("5", 5, false);
                debugConfigMenu.addChoice("6", 6, false);
                debugConfigMenu.addChoice("7", 7, false);
                debugConfigMenu.addChoice("8", 8, false);
                debugConfigMenu.addChoice("9", 9, false);
                debugConfigMenu.addChoice("10", 10, false);
                break;
        }
*/

        //
        // Walk the menu tree starting with the strategy menu as the root
        // menu and get user choices.
        //
        FTCMenu.walkMenuTree(teamMenu, this);

        //
        // Set choices variables.
        //load variables
        //
        teamNumber = teamMenu.getCurrentChoiceText();
        allianceColor = allianceMenu.getCurrentChoiceText();
        allianceStartPosition = startPosMenu.getCurrentChoiceText();
        delay = (int)delayMenu.getCurrentValue();
        robotConfigBase = robotConfigMenu.getCurrentChoiceText();
        robotMotorType = robotMotorMenu.getCurrentChoiceText();
        robotMotorRatio = (int)robotMotorRatioMenu.getCurrentValue();
        robotMotorDirection = robotMotorDirectionMenu.getCurrentChoiceText();
        //debug = Integer.parseInt(debugConfigMenu.getCurrentChoiceText());
        debug = (int)(debugConfigMenu.getCurrentValue());

        //write the options to sharedpreferences
        editor.putString("club.towr5291.Autonomous.TeamNumber", teamNumber);
        editor.putString("club.towr5291.Autonomous.Color", allianceColor);
        editor.putString("club.towr5291.Autonomous.Position", allianceStartPosition);
        editor.putString("club.towr5291.Autonomous.Delay", String.valueOf(delay));
        editor.putString("club.towr5291.Autonomous.RobotConfigBase", robotConfigBase);
        editor.putString("club.towr5291.Autonomous.RobotMotorType", robotMotorType);
        editor.putString("club.towr5291.Autonomous.RobotMotorRatio", String.valueOf(robotMotorRatio));
        editor.putString("club.towr5291.Autonomous.RobotMotorDirection", robotMotorDirection);
        editor.putString("club.towr5291.Autonomous.Debug", String.valueOf(debug));
        editor.commit();

        //read them back to ensure they were written
        //load variables
        teamNumber = sharedPreferences.getString("club.towr5291.Autonomous.TeamNumber", "0000");
        allianceColor = sharedPreferences.getString("club.towr5291.Autonomous.Color", "Red");
        allianceStartPosition = sharedPreferences.getString("club.towr5291.Autonomous.Position", "Left");
        delay = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Delay", "0"));
        robotConfigBase = sharedPreferences.getString("club.towr5291.Autonomous.RobotConfigBase", "Custom_Mecanum");
        robotMotorType = sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorType", "Rev");
        robotMotorRatio = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorRatio", "15"));
        robotMotorDirection = sharedPreferences.getString("club.towr5291.Autonomous.RobotMotorDirection", "Forward");
        debug = Integer.parseInt(sharedPreferences.getString("club.towr5291.Autonomous.Debug", "1"));

        int lnum = 1;
        dashboard.displayPrintf(lnum++, "Team#:            " + teamNumber);
        dashboard.displayPrintf(lnum++, "Alliance:         " + allianceColor);
        dashboard.displayPrintf(lnum++, "Start:            " + allianceStartPosition);
        dashboard.displayPrintf(lnum++, "Delay:            " + String.valueOf(delay));
        dashboard.displayPrintf(lnum++, "RobotBase:        " + robotConfigBase);
        dashboard.displayPrintf(lnum++, "RobotMotor:       " + robotMotorType);
        dashboard.displayPrintf(lnum++, "RobotMotorRatio:  " + robotMotorRatio);
        dashboard.displayPrintf(lnum++, "RobotMotorDirec:  " + robotMotorDirection);
        dashboard.displayPrintf(lnum++, "Debug:            " + debug);

        fileLogger.writeEvent("AutonConfig", "Team#           " + teamNumber);
        fileLogger.writeEvent("AutonConfig", "Alliance        " + allianceColor);
        fileLogger.writeEvent("AutonConfig", "Start           " + allianceStartPosition);
        fileLogger.writeEvent("AutonConfig", "Delay           " + String.valueOf(delay));
        fileLogger.writeEvent("AutonConfig", "RobotBase       " + robotConfigBase);
        fileLogger.writeEvent("AutonConfig", "RobotMotor      " + robotMotorType);
        fileLogger.writeEvent("AutonConfig", "RobotMotorRatio " + robotMotorRatio);
        fileLogger.writeEvent("AutonConfig", "RobotMotorDirec " + robotMotorDirection);
        fileLogger.writeEvent("AutonConfig", "Debug:          " + debug);
    }

    public void initRobot()
    {
        //start the log
        fileLogger = new FileLogger(runtime,3,true);
        fileLogger.open();
        fileLogger.write("Time,SysMS,Thread,Event,Desc");
        fileLogger.writeEvent(TAG, "Log Started");
    }   //initRobot

    @Override
    public void runOpMode() throws InterruptedException {

        initRobot();

        dashboard = TOWRDashBoard.createInstance(telemetry);

        FtcRobotControllerActivity act = (FtcRobotControllerActivity)(hardwareMap.appContext);

        dashboard.setTextView((TextView)act.findViewById(R.id.textOpMode));
        dashboard.displayPrintf(0, "Starting Menu System");

        dashboard.displayPrintf(0, "INITIALIZING - Please wait for Menu");
        fileLogger.writeEvent(TAG, "SETUP");

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        doMenus();
        dashboard.displayPrintf(0, "COMPLETE - Settings Written");

        stopMode();
    }

    public void stopMode()
    {
        //stop the log
        if (fileLogger != null) {
            fileLogger.writeEvent(TAG, "Stopped");
            fileLogger.close();
            fileLogger = null;
        }
    }   //stopMode
}


