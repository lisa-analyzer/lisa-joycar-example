import java.io.*;

class JoyCar
{
	public native int initializeWiringPi();
	public native void initializePCF8591();
	//public native int[] readData(); //remove
	public native int readUpDown();
	public native int readLeftRight();
	public native boolean isButtonPressed();
	public native void initializeServo();
	public native void turnRight();
	public native void turnLeft();
	public native void turnAtAngle(int angle);
	public native void initializeMotor();
	public native void runMotor(int value);
	
   	//public int joystick[];

    static
    {
        System.loadLibrary("joycar");
    }

    public static void main(String[] args)
    {
        
        System.out.println("...<< Integrated IOT Analysis  >>...\n");
		
        JoyCar rc = new JoyCar();
       // rc.joystick = new int[3];
        try {
			if(rc.initializeWiringPi()==0){
				System.out.println("WiringPi setup Failed... exiting program... \n");
				System.exit(0);
			}
			rc.initializePCF8591();
			rc.initializeServo();
			rc.initializeMotor();
			
			while(true){
				//rc.joystick = rc.readData();

				//System.out.println("x = "+rc.joystick[0]+"   y = "+rc.joystick[1]+"   z = "+rc.joystick[2]);
				rc.runMotor(rc.readUpDown());
				
				if(rc.readLeftRight()>=200){
					//System.out.println("turning right....  "+"x = "+rc.joystick[0]+"   y = "+rc.joystick[1]+"   z = "+rc.joystick[2]);
					rc.turnRight();
				}
				else if(rc.readLeftRight()<=100){
					//System.out.println("turning left....  "+"x = "+rc.joystick[0]+"   y = "+rc.joystick[1]+"   z = "+rc.joystick[2]);
					rc.turnLeft();
				}
				else if (rc.isButtonPressed()){
					rc.turnAtAngle(0);
					//System.out.println("alining at 0 degree....  "+"x = "+rc.joystick[0]+"   y = "+rc.joystick[1]+"   z = "+rc.joystick[2]);
				}
				
				//Thread.sleep(200);
			}      	        	
		} catch (Exception e) {
            e.printStackTrace();
        }
    }
}