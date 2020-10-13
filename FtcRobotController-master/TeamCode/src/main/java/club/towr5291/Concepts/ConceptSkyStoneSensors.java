/*
Copyright (c) 2018 FIRST

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of FIRST nor the names of its contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package club.towr5291.Concepts;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import club.towr5291.robotconfig.HardwareSensorsSkyStone;

/**
 * {@link ConceptSkyStoneSensors} illustrates how to use the REV Robotics
 * Time-of-Flight Range Sensor.
 *
 * The op mode assumes that the range sensor is configured with a name of "sensor_range".
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 *
 * @see <a href="http://revrobotics.com">REV Robotics Web Page</a>
 */
@TeleOp(name = "SkyStone: Base Sensors", group = "Concept")
//@Disabled
public class ConceptSkyStoneSensors extends LinearOpMode {

    private HardwareSensorsSkyStone sensors       = new HardwareSensorsSkyStone();

    @Override
    public void runOpMode() {

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        //Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor)sensorRange;
        sensors.init(hardwareMap);
        telemetry.addData(">>", "Press start to continue");
        telemetry.update();

        waitForStart();
        while(opModeIsActive()) {
            // generic DistanceSensor methods.
            //telemetry.addData("deviceName",sensorRange.getDeviceName() );
            boolean blackLeft  = (sensors.distanceColorSideLeft().alpha() / sensors.distanceColorSideLeft().red()) > 2.9 ? true : false;
            boolean blackRight = (sensors.distanceColorSideRight().alpha() / sensors.distanceColorSideRight().red()) > 2.9 ? true : false;

            telemetry.addData("range Front Left       ", String.format("%.01f cm", sensors.distanceFrontLeftCM()));
            telemetry.addData("range Front Right      ", String.format("%.01f cm", sensors.distanceFrontRightCM()));
            telemetry.addData("range Rear Left        ", String.format("%.01f cm", sensors.distanceRearLeftCM()));
            telemetry.addData("range Rear Right       ", String.format("%.01f cm", sensors.distanceRearRightCM()));
            telemetry.addData("range Side Left        ", String.format("%.01f cm", sensors.distanceSideLeftCM()));
            telemetry.addData("range Side Right       ", String.format("%.01f cm", sensors.distanceSideRightCM()));
            telemetry.addData("range Side Left        ", String.format("%.01f cm", sensors.distanceColorSideLeftCM()));
            telemetry.addData("Color Side Left Alpha  ", sensors.distanceColorSideLeft().alpha());
            telemetry.addData("Color Side Left Red    ", sensors.distanceColorSideLeft().red());
            telemetry.addData("Color Side Left Green  ", sensors.distanceColorSideLeft().green());
            telemetry.addData("Color Side Left Blue   ", sensors.distanceColorSideLeft().blue());
            telemetry.addData("range Side Right       ", String.format("%.01f cm", sensors.distanceColorSideRightCM()));
            telemetry.addData("Color Side Right Alpha ", sensors.distanceColorSideRight().alpha());
            telemetry.addData("Color Side Right Red   ", sensors.distanceColorSideRight().red());
            telemetry.addData("Color Side Right Green ", sensors.distanceColorSideRight().green());
            telemetry.addData("Color Side Right Blue  ", sensors.distanceColorSideRight().blue());
            telemetry.addData("Color Side Left BLACK  ", blackLeft);
            telemetry.addData("Color Side Right BLACK ", blackRight);

            // Rev2mDistanceSensor specific methods.
            //telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            //telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.update();
        }
    }

}