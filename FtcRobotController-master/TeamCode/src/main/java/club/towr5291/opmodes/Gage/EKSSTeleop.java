package club.towr5291.opmodes.Gage;

//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp (name = "EK-SS-TELEOP v2.1", group = "Gage")
//@Disabled
public class EKSSTeleop extends OpMode {

    private DcMotor lm2 =         null;
    private DcMotor rm2 =         null;
    private DcMotor lm1 =         null;
    private DcMotor rm1 =         null;
    private DcMotor rWheel =      null;
    private DcMotor lWheel =      null;
//    private Servo capServo =      null;
    private Servo grabberServo =  null;
    private DcMotor liftMotor =   null;
    private Servo hServo1 =       null;
    private Servo hServo2 =       null;
    private DcMotor extendMotor = null;
//    private Servo extendServo =  null;

    static final double MAX_POS     =  1.0;     // Maximum rotational position
    static final double MIN_POS     =  0.0;     // Minimum rotational positionFORWARD private CRServo intakeServ = null;
    private double leftPower1 = 0;
    private double leftPower2 = 0;
    private double rightPower1 = 0;
    private double rightPower2 = 0;
    private double hPos = 0;
    private double currentExtend = 0;
    @Override
    public void init() {
        //Telemetry Data
        telemetry.addData("Status:", "INIT");
        telemetry.update();

        //Hardware Map
        lm1 = hardwareMap.get(DcMotor.class, "lm1");
        lm2 = hardwareMap.get(DcMotor.class, "lm2");
        rm1 = hardwareMap.get(DcMotor.class, "rm1");
        rm2 = hardwareMap.get(DcMotor.class, "rm2");
        rWheel = hardwareMap.get(DcMotor.class, "rWheel");
        lWheel = hardwareMap.get(DcMotor.class, "lWheel");
        liftMotor = hardwareMap.get(DcMotor.class, "liftMotor");
        //grabberServo = hardwareMap.servo.get("extendServo");
        grabberServo = hardwareMap.get(Servo.class, "grabberServo");
        //hServo2 = hardwareMap.servo.get("hServo2");
        //hServo1 = hardwareMap.servo.get("hServo1");
        hServo1 = hardwareMap.get(Servo.class, "hServo1");
        hServo2 = hardwareMap.get(Servo.class, "hServo2");
        //     intakeServ = hardwareMap.crservo.get("intakeS");
        //capServo = hardwareMap.servo.get("capServo");
//        capServo = hardwareMap.get(Servo.class, "capServo");
        extendMotor = hardwareMap.get(DcMotor.class, "extendMotor");
//        extendServo = hardwareMap.get(Servo.class, "extendServo");
        //Change Motor Directions
        lm1.setDirection(DcMotorSimple.Direction.FORWARD);
        lm2.setDirection(DcMotorSimple.Direction.FORWARD);
        rm1.setDirection(DcMotorSimple.Direction.REVERSE);
        rm2.setDirection(DcMotorSimple.Direction.FORWARD);

        extendMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        telemetry.addData("Status:", "INIT END");

    }

    @Override
    public void loop() {

//        if (gamepad2.left_bumper){
//            intakeServ.setPower(0.5);
//        } else if (gamepad2.right_bumper){
//            intakeServ.setPower(-0.5);
//        }else{
//            intakeServ.setPower(0);
//        }

        rWheel.setPower(-gamepad2.left_stick_y);
        lWheel.setPower(gamepad2.left_stick_y);

//        if (gamepad2.dpad_up){
//            capServo.setPosition(1);
//            telemetry.addData("Grabber Servo2:", "Off");
//        }
//        else if (gamepad2.dpad_down){
//            capServo.setPosition(-1);
//            telemetry.addData("Grabber Servo2:", "On");
//        }

        if (liftMotor.getPower() > -0.1)liftMotor.setPower(gamepad2.right_trigger);
        if (liftMotor.getPower() < 0.1) liftMotor.setPower(-gamepad2.left_trigger);

        if (gamepad2.dpad_up) {
            grabberServo.setPosition(1);
//            telemetry.addData("Grabber Servo1:", "Off");
        }else if (gamepad2.dpad_down){
            grabberServo.setPosition(0);
//            telemetry.addData("Grabber Servo1:", "On");
        }
        hPos = gamepad2.right_stick_y;
        if (hPos >= MAX_POS ) {
            hPos = MAX_POS;
        } else {
            if (hPos <= MIN_POS ) {
                hPos = MIN_POS;
            }
        hServo1.setPosition(1 - hPos);
        hServo2.setPosition(hPos);

        if (gamepad2.right_bumper){
            extendMotor.setPower(0.7);
        }else if (gamepad2.left_bumper){
            extendMotor.setPower(-0.7);

        }else {
            extendMotor.setPower(0);
        }

        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;
        leftPower1 = Range.clip(drive - turn - gamepad1.left_stick_x, -1.0, 1.0);
        leftPower2 = Range.clip(drive - turn + gamepad1.left_stick_x, -1.0, 1.0);
        rightPower1 = Range.clip(drive + turn + gamepad1.left_stick_x, -1.0, 1.0);
        rightPower2 = Range.clip(drive + turn - gamepad1.left_stick_x, -1.0, 1.0);

        lm1.setPower(leftPower1);
        lm2.setPower(leftPower2);
        rm1.setPower(rightPower1);
        rm2.setPower(rightPower2);

        telemetry.addData("LeftPower 1", lm1.getPower());
        telemetry.addData("LeftPower 2", lm2.getPower());
        telemetry.addData("RightPower 1", rm1.getPower());
        telemetry.addData("RightPower 2", rm2.getPower());

        telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower1, rightPower1);
    }
}}