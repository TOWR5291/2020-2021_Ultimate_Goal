package club.towr5291.libraries;

public class LibraryMotorType {

    public double GEARRATIO = 1;
    public double COUNTSPERROTATION;
    public double PULSEPERROTATION;

    private double dblENCODER_CPR_REV = 28;
    private double dblENCODER_CPR_REV40SPUR = 1120;
    private double dblENCODER_CPR_REV20SPUR = 560;
    private double dblENCODER_CPR_REV20ORBITAL = 560;
    private double dblENCODER_CPR_REV15ORBITAL = 420;
    private double dblENCODER_CPR_ANDY20ORBITAL = 537.6;
    private double dblENCODER_CPR_ANDY20SPUR = 560;
    private double dblENCODER_CPR_ANDY40SPUR = 1120;
    private double dblENCODER_CPR_ANDY60SPUR = 1680;
    private double dblENCODER_CPR_ANDY3_7ORBITAL = 0;

    private double dblENCODER_PPR_REV = 2.8;
    private double dblENCODER_PPR_REV40SPUR = dblENCODER_PPR_REV * 40;
    private double dblENCODER_PPR_REV20SPUR = dblENCODER_PPR_REV * 20;
    private double dblENCODER_PPR_REV03ORBITAL = dblENCODER_PPR_REV * 3;
    private double dblENCODER_PPR_REV04ORBITAL = dblENCODER_PPR_REV * 4;
    private double dblENCODER_PPR_REV05ORBITAL = dblENCODER_PPR_REV * 5;
    private double dblENCODER_PPR_REV20ORBITAL = dblENCODER_PPR_REV * 20;
    private double dblENCODER_PPR_ANDY20ORBITAL = 134.4;
    private double dblENCODER_PPR_ANDY20SPUR = 28;
    private double dblENCODER_PPR_ANDY40SPUR = 28;
    private double dblENCODER_PPR_ANDY60SPUR = 28;
    private double dblENCODER_PPR_ANDY3_7ORBITAL = 0;

    private double dblGearRatioREV40SPUR = 40;
    private double dblGearRatioREV20SPUR = 20;
    private double dblGearRatioREV03ORBITAL = 3;
    private double dblGearRatioREV04ORBITAL = 4;
    private double dblGearRatioREV05ORBITAL = 5;
    private double dblGearRatioREV20ORBITAL = 20;
    private double dblGearRatioANDY20ORBITAL = 19.2;
    private double dblGearRatioANDY20SPUR = 20;
    private double dblGearRatioANDY40SPUR = 40;
    private double dblGearRatioANDY60SPUR = 60;
    private double dblGearRatioANDY3_7ORBITAL = 3.7;

    public enum MotorTypes {
        REV40SPUR ("REV40SPUR"),
        REV20SPUR ("REV20SPUR"),
        REV01ORBIT ("REV01ORBIT"),
        REV03ORBIT ("REV03ORBIT"),
        REV04ORBIT ("REV04ORBIT"),
        REV05ORBIT ("REV05ORBIT"),
        REV20ORBIT ("REV20ORBIT"),
        ANDY20SPUR ("ANDY20SPUR"),
        ANDY40SPUR ("ANDY40SPUR"),
        ANDY60SPUR ("ANDY60SPUR"),
        ANDY20ORBIT ("ANDY20ORBIT"),
        ANDY3_7ORBIT ("ANDY3_7ORBIT");

        public String name;

        MotorTypes(String name){
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public boolean isAndyMark(){
            if(this.name.equals("ANDY20SPUR") || this.name.equals("ANDY40SPUR") || this.name.equals("ANDY60SPUR") || this.name.equals("ANDY20ORBIT") || this.name.equals("ANDY3_7ORBIT")) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void loadData(MotorTypes motorTypes){
        switch(motorTypes){
            case REV40SPUR:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV40SPUR;
                this.PULSEPERROTATION = this.dblENCODER_PPR_REV40SPUR;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV40SPUR;
                break;

            case REV20SPUR:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV20SPUR;
                this.PULSEPERROTATION = this.dblENCODER_PPR_REV20SPUR;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV20SPUR;
                break;

            case REV20ORBIT:
                this.PULSEPERROTATION = this.dblENCODER_PPR_REV20ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV20ORBITAL;
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV20ORBITAL;
                break;

            case REV01ORBIT:
                //this.PULSEPERROTATION = this.PULSEPERROTATION*this.dblENCODER_PPR_REV03ORBITAL;
                //this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV03ORBITAL;
                //this.COUNTSPERROTATION = this.dblENCODER_CPR_REV*this.GEARRATIO;
                break;

            case REV03ORBIT:
                this.PULSEPERROTATION = this.PULSEPERROTATION*this.dblENCODER_PPR_REV03ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV03ORBITAL;
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV*this.GEARRATIO;
                break;

            case REV04ORBIT:
                this.PULSEPERROTATION = this.PULSEPERROTATION*this.dblENCODER_PPR_REV04ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV04ORBITAL;
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV*this.GEARRATIO;
                break;

            case REV05ORBIT:
                this.PULSEPERROTATION = this.PULSEPERROTATION*this.dblENCODER_PPR_REV05ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioREV05ORBITAL;
                this.COUNTSPERROTATION = this.dblENCODER_CPR_REV*this.GEARRATIO;
                break;

            case ANDY20SPUR:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_ANDY20SPUR;
                this.PULSEPERROTATION = this.dblENCODER_PPR_ANDY20SPUR;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioANDY20SPUR;
                break;

            case ANDY40SPUR:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_ANDY40SPUR;
                this.PULSEPERROTATION = this.dblENCODER_PPR_ANDY40SPUR;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioANDY40SPUR;
                break;

            case ANDY60SPUR:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_ANDY60SPUR;
                this.PULSEPERROTATION = this.dblENCODER_PPR_ANDY60SPUR;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioANDY60SPUR;
                break;

            case ANDY20ORBIT:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_ANDY20ORBITAL;
                this.PULSEPERROTATION = this.dblENCODER_PPR_ANDY20ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioANDY20ORBITAL;
                break;

            case ANDY3_7ORBIT:
                this.COUNTSPERROTATION = this.dblENCODER_CPR_ANDY3_7ORBITAL;
                this.PULSEPERROTATION = this.dblENCODER_PPR_ANDY3_7ORBITAL;
                this.GEARRATIO = this.GEARRATIO*this.dblGearRatioANDY3_7ORBITAL;
                break;
        }
    }

    public LibraryMotorType(){
        //Nothing In here yet :)
    }

    public double getGEARRATIO() {
        return GEARRATIO;
    }

    public double getCOUNTSPERROTATION(int ratio, MotorTypes motorType) {
        if (motorType == MotorTypes.REV01ORBIT) {
            switch (ratio) {
                case 3:
                    loadData(MotorTypes.REV03ORBIT);
                    break;
                case 4:
                    loadData(MotorTypes.REV04ORBIT);
                    break;
                case 5:
                    loadData(MotorTypes.REV05ORBIT);
                    break;
                case 9:
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV03ORBIT);
                    break;
                case 12:
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    break;
                case 15:
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV05ORBIT);
                    break;
                case 16:
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    break;
                case 20:
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV05ORBIT);
                    break;
                case 25:
                    loadData(MotorTypes.REV05ORBIT);
                    loadData(MotorTypes.REV05ORBIT);
                    break;
                case 27:
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV03ORBIT);
                    break;
                case 60:
                    loadData(MotorTypes.REV03ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV05ORBIT);
                    break;
                case 64:
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    break;
                case 125:
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    loadData(MotorTypes.REV04ORBIT);
                    break;
            }
        }
        return COUNTSPERROTATION;
    }

    public double getPULSEPERROTATION() {
        return PULSEPERROTATION;
    }

}