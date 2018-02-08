package test;

import java.awt.Robot;
import java.awt.event.InputEvent;

public class TestMoveMouse00 {

	public static void main(String[] args) {
	    try {
	      Robot robot = new Robot();
	      // Move the cursor 
	      robot.mouseMove(300, 500);
	      // And then, Left Click
	      robot.mousePress(InputEvent.BUTTON1_MASK);
	      robot.mouseRelease(InputEvent.BUTTON1_MASK);        

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	  }

}
