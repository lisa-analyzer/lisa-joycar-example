import java.io.*;

class JoyCar {
	public native int initializeWiringPi();

	public native void initializePCF8591();

	public native int readUpDown();

	public native int readLeftRight();

	public native boolean isButtonPressed();

	public native void initializeServo();

	public native void turnRight();

	public native void turnLeft();

	public native void turnAtAngle(int angle);

	public native void initializeMotor();

	public native void runMotor(int value);

	static {
		// this is needed only when executing, can be ignored by lisa
		System.loadLibrary("joycar");
	}

	public static void main(String[] args) {

		JoyCar rc = new JoyCar();
		if (rc.initializeWiringPi() == 0)
			return;
		rc.initializePCF8591();
		rc.initializeServo();
		rc.initializeMotor();

		while (true) {
			rc.runMotor(rc.readUpDown());

			if (rc.readLeftRight() >= 200)
				rc.turnRight();
			else if (rc.readLeftRight() <= 100)
				rc.turnLeft();
			else if (rc.isButtonPressed())
				rc.turnAtAngle(0);
		}
	}
}