package club.towr5291.opmodes.Gage;

import com.qualcomm.hardware.adafruit.AdafruitI2cColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import club.towr5291.functions.FileLogger;
import club.towr5291.opmodes.OpModeMasterLinear;
@Autonomous
public class GageAutonEncoder extends OpModeMasterLinear {

    private ElapsedTime runtime = new ElapsedTime();

    private DcMotor lm2 = null;
    private DcMotor rm2 = null;
    private DcMotor lm1 = null;
    private DcMotor rm1 = null;
//    AdafruitI2cColorSensor colorSensor = null;

    public FileLogger fileLogger;
    private int debug = 3;

    static final double MotorCountsPerRev = 1440;
    boolean done = false;
    int CurrentInstruct = 0;
    int CurrentInstructVar = CurrentInstruct * 4;

    String Instruct[] =
            {"MOVE", "DONE"}; //First = type of move. 2nd = speed. 3rd & 4th = left/right inches. 5th = timeout
    int InstructVar[] =
            {1,21,21,5};



    @Override
    public void runOpMode() throws InterruptedException {

        lm1 = hardwareMap.get(DcMotor.class, "lm1");
        lm2 = hardwareMap.get(DcMotor.class, "lm2");
        rm1 = hardwareMap.get(DcMotor.class, "rm1");
        rm2 = hardwareMap.get(DcMotor.class, "rm2");
//        colorSensor = hardwareMap.get(AdafruitI2cColorSensor.class,"colorSensor");

        lm1.setDirection(DcMotorSimple.Direction.FORWARD);
        lm2.setDirection(DcMotorSimple.Direction.FORWARD);
        rm1.setDirection(DcMotorSimple.Direction.REVERSE);
        rm2.setDirection(DcMotorSimple.Direction.REVERSE);

        fileLogger = new FileLogger(runtime, 1, true);
        fileLogger.open();
        fileLogger.write("Time,SysMS,Thread,Event,Desc");
        fileLogger.setEventTag("runOpMode()");
        fileLogger.writeEvent("Log Started");

        waitForStart();

//        while (opModeIsActive() && (done == false)){
//            fileLogger.writeEvent("Looped");
//            switch (Instruct[CurrentInstruct]){
//                case "MOVE":
//                    fileLogger.writeEvent("Tried MOVE");
//                    DriveByEncoder(InstructVar[CurrentInstructVar], InstructVar[CurrentInstructVar + 1], InstructVar[CurrentInstructVar + 2], InstructVar[CurrentInstructVar + 3]);
//                    fileLogger.writeEvent("Attempted MOVE done");
//                    break;
//                case "DONE":
//                    fileLogger.writeEvent("DONE");
//                    done = true;
//                    break;
//            }
//
//            CurrentInstruct = CurrentInstruct + 1;
//            CurrentInstructVar = CurrentInstruct * 4;
//
//            fileLogger.writeEvent("Changed Vars");
//
//        }
    }
    private void DriveByEncoder(double speed, double leftInches, double rightInches, double timeOut){


        int leftTarget;
        int rightTarget;

        leftTarget = (int)(leftInches * MotorCountsPerRev);
        rightTarget = (int)(rightInches * MotorCountsPerRev);

        lm1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lm2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rm1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rm2.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();
        lm1.setPower(Math.abs(speed));
        lm2.setPower(Math.abs(speed));
        rm1.setPower(Math.abs(speed));
        rm2.setPower(Math.abs(speed));

        fileLogger.writeEvent("MOVING ROBOT");

        while (opModeIsActive() &&
                (runtime.seconds() < timeOut) &&
                (lm1.isBusy() && rm1.isBusy())) {

            // Display it for the driver.
            telemetry.addData("Path1",  "Running to %7d :%7d", leftTarget,  rightTarget);
            telemetry.addData("Path2",  "Running at %7d :%7d",
                    lm1.getCurrentPosition(),
                    rm1.getCurrentPosition());
            telemetry.update();

            lm1.setPower(0);
            lm2.setPower(0);
            rm1.setPower(0);
            rm2.setPower(0);
        }
    }
}
