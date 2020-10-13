package club.towr5291.functions;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import club.towr5291.libraries.LibraryStateSegAutoRoverRuckus;
import club.towr5291.libraries.robotConfig;


public class ReadStepFileXML {

    public HashMap<String, LibraryStateSegAutoRoverRuckus> autonomousStep = new HashMap<>();

    int numberOfLoadedSteps = 0;

    public int getNumberLoadedSteps() {
        return numberOfLoadedSteps;
    }

    public void setNumberLoadedSteps(int numberLoadedSteps) {
        this.numberOfLoadedSteps = numberLoadedSteps;
    }

    private HashMap<String, LibraryStateSegAutoRoverRuckus> loadSteps(String fileName){
        File stepFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Sequences"), fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        Log.e("RUNNINNG LOADSTEPS", "STEPS");
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stepFile);

            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList nStepList = doc.getElementsByTagName("Step");
            System.out.println("Step elements :" + nStepList.getLength());

//            Element element = doc.getDocumentElement().getAttribute("ID");

            for (int i = 0; i < nStepList.getLength(); i++){
                Node step = nStepList.item(i);
                System.out.println("Step elements :" + step.getNodeName());

                //if (step.getNodeType() == Node.ELEMENT_NODE){
                if (step.getNodeName().equalsIgnoreCase("Step")) {
                    Element eElement = (Element) step;
                    System.out.println("Found Step :");
                    int bypassedStep = 0;
                    try {
                        bypassedStep = Integer.parseInt(eElement.getElementsByTagName("bypassed").item(0).getTextContent());
                        System.out.println("bypassedStep :" + bypassedStep);
                    } catch(Exception e) {
                        bypassedStep = 0;
                    }
                    if (bypassedStep == 0) {
                        loadSteps(
                                Double.parseDouble(eElement.getElementsByTagName("Timeout").item(0).getTextContent()),
                                eElement.getElementsByTagName("Command").item(0).getTextContent(),
                                Double.parseDouble(eElement.getElementsByTagName("Distance").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Speed").item(0).getTextContent()),
                                Boolean.parseBoolean(eElement.getElementsByTagName("Parallel").item(0).getTextContent()),
                                Boolean.parseBoolean(eElement.getElementsByTagName("Lastpos").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm1").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm2").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm3").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm4").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm5").item(0).getTextContent()),
                                Double.parseDouble(eElement.getElementsByTagName("Parm6").item(0).getTextContent())
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.autonomousStep;
    }

    private void loadSteps(double timeOut, String command, double distance, double power, boolean parallel, boolean lastPos, double parm1, double parm2, double parm3, double parm4, double parm5, double parm6)
    {
        this.numberOfLoadedSteps++;
        this.autonomousStep.put(String.valueOf(this.getNumberLoadedSteps()), new LibraryStateSegAutoRoverRuckus (this.getNumberLoadedSteps(), timeOut, command, distance, power, parallel, lastPos, parm1, parm2, parm3, parm4, parm5, parm6));
    }

    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodes.item(0);
        return node.getNodeValue();
    }

    //public HashMap<String, LibraryStateSegAutoRoverRuckus> ReadStepFile(SharedPreferences sharedPreferences) {
    public HashMap<String,LibraryStateSegAutoRoverRuckus> ReadStepFile(robotConfig robotconfig) {
        HashMap<String,LibraryStateSegAutoRoverRuckus> autonomousSteps = new HashMap<String,LibraryStateSegAutoRoverRuckus>();
        //load the sequence based on alliance colour and team
        switch (robotconfig.getAllianceColor()) {
            case "Red":
                switch (robotconfig.getAllianceStartPosition()) {
                    case "Left":
                        switch (robotconfig.getTeamNumber()) {
                            case "5291":
                                autonomousSteps = loadSteps("5291RedLeftSkyStone.xml");
                                break;
                            case "11230":
                                autonomousSteps = loadSteps("");
                                break;
                            case "11231":
                                autonomousSteps = loadSteps("");
                                break;
                        }
                        break;
                    case "Right":
                        switch (robotconfig.getTeamNumber()) {
                            case "5291":
                                autonomousSteps = loadSteps("5291RedRightSkyStone.xml");
                                break;
                            case "11230":
                                autonomousSteps = loadSteps("");
                                break;
                            case "11231":
                                autonomousSteps = loadSteps("");
                                break;
                        }
                        break;
                }
                break;
            case "Blue":
                switch (robotconfig.getAllianceStartPosition()) {
                    case "Left":
                        switch (robotconfig.getTeamNumber()) {
                            case "5291":
                                autonomousSteps = loadSteps("5291BlueLeftSkyStone.xml");
                                break;
                            case "11230":
                                autonomousSteps = loadSteps("");
                                break;
                            case "11231":
                                autonomousSteps = loadSteps("");
                                break;
                        }
                        break;
                    case "Right":
                        switch (robotconfig.getTeamNumber()) {
                            case "5291":
                                autonomousSteps = loadSteps("5291BlueRightSkyStone.xml");
                                break;
                            case "11230":
                                autonomousSteps = loadSteps("");
                                break;
                            case "11231":
                                autonomousSteps = loadSteps("");
                                break;
                        }
                        break;
                }
                break;
            case "Test":
                switch (robotconfig.getTeamNumber()) {
                    case "5291":
                        autonomousSteps = loadSteps("5291TestSkyStone.xml");
                        break;
                    case "11230":
                        autonomousSteps = loadSteps("");
                        break;
                    case "11231":
                        autonomousSteps = loadSteps("");
                        break;
                }

                break;
        }
        autonomousStep = autonomousSteps;
        return autonomousStep;
    }

    public HashMap<String, LibraryStateSegAutoRoverRuckus> insertSteps(double timeOut, String command, double distance,  double power, boolean parallel, boolean lastPos, double parm1, double parm2, double parm3, double parm4, double parm5, double parm6, int insertlocation) {
        Log.d("insertSteps", " timout " + timeOut + " command " + command + " distance " + distance + "  power " + power + " parallel " + parallel + " lastPos " + lastPos + " parm1 " + parm1 + " parm2 " + parm2 + " parm3 " + parm3 + " parm4 " + parm4 + " parm5 " + parm5 + " parm6 " + parm6);
        HashMap<String, LibraryStateSegAutoRoverRuckus> autonomousStepsTemp = new HashMap<String, LibraryStateSegAutoRoverRuckus>();
        LibraryStateSegAutoRoverRuckus processingStepsTemp;

        //move all the steps from current step to a temp location
        for (int loop = insertlocation; loop <= this.numberOfLoadedSteps; loop++) {
            processingStepsTemp = autonomousStep.get(String.valueOf(loop));
            Log.d("insertSteps", "Reading all the next steps " + loop + " timout " + processingStepsTemp.getmRobotTimeOut() + " command " + processingStepsTemp.getmRobotCommand());
            autonomousStepsTemp.put(String.valueOf(loop), autonomousStep.get(String.valueOf(loop)));
        }
        Log.d("insertSteps", "All steps loaded to a temp hasmap");

        //insert the step we want

        autonomousStep.put(String.valueOf(insertlocation), new LibraryStateSegAutoRoverRuckus (this.numberOfLoadedSteps, timeOut, command, distance, power, parallel, lastPos, parm1, parm2, parm3, parm4, parm5, parm6));
        Log.d("insertSteps", "Inserted New step");

        //move all the other steps back into the sequence
        for (int loop = insertlocation; loop <= this.numberOfLoadedSteps; loop++)
        {
            processingStepsTemp = autonomousStepsTemp.get(String.valueOf(loop));
            Log.d("insertSteps", "adding these steps back steps " + (loop + 1) + " timout " + processingStepsTemp.getmRobotTimeOut() + " command " + processingStepsTemp.getmRobotCommand());
            autonomousStep.put(String.valueOf(loop + 1), autonomousStepsTemp.get(String.valueOf(loop)));
        }
        Log.d("insertSteps", "Re added all the previous steps");
        //increment the step counter as we inserted a new step
        //mValueSteps.add(loadStep, new LibraryStateTrack(false,false));
        this.numberOfLoadedSteps++;
        return autonomousStep;
    }

    public HashMap<String, LibraryStateSegAutoRoverRuckus> activeSteps() {
        return autonomousStep;
    }

    public HashMap<String, LibraryStateSegAutoRoverRuckus> getAutonomousStep() {
        return autonomousStep;
    }
    public void setAutonomousStep(HashMap<String, LibraryStateSegAutoRoverRuckus> autonomousStep) {
        this.autonomousStep = autonomousStep;
    }

}