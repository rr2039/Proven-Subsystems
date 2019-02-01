/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.I2C;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  /*
  Here are the registry values
  More information can be found at http://www.revrobotics.com/content/docs/TMD3782_v2.pdf
  For wiring and general information: https://www.revrobotics.com/content/docs/REV-31-1537-DS.pdf  
  */ 
  protected final static int CMD = 0x80;
  protected final static int MULTI_BYTE_BIT = 0x20;
  protected final static int ENABLE_REGISTER  = 0x00;
  protected final static int ATIME_REGISTER   = 0x01;
  protected final static int PPULSE_REGISTER  = 0x0E;

  protected final static int ID_REGISTER     = 0x12;
  protected final static int CDATA_REGISTER  = 0x14; // Clear (Alpha) register
  protected final static int RDATA_REGISTER  = 0x16; // Red register
  protected final static int GDATA_REGISTER  = 0x18; // Green register
  protected final static int BDATA_REGISTER  = 0x1A; // Blue register
  protected final static int PDATA_REGISTER  = 0x1C; // Proximity register

  protected final static int PON   = 0b00000001;
  protected final static int AEN   = 0b00000010;
  protected final static int PEN   = 0b00000100;
  protected final static int WEN   = 0b00001000;
  protected final static int AIEN  = 0b00010000;
  protected final static int PIEN  = 0b00100000;

  private final double integrationTime = 10;
  private ByteBuffer buffer = ByteBuffer.allocate(10);
  private short alpha = 0, red = 0, green = 0, blue = 0, prox = 0;
  
  I2C sensor = new I2C(I2C.Port.kOnboard, 0x39);

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    //Some initializing for the sensor
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    sensor.write(CMD | 0x00, PON | AEN | PEN);
    sensor.write(CMD | 0x01, (int) (256-integrationTime/2.38)); //configures the integration time (time for updating color data)
    sensor.write(CMD | 0x0E, 0b1111);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    buffer.clear();
    //First the data is loaded into a buffer, then we can access it with buffer.getshort()
    sensor.read(CMD | MULTI_BYTE_BIT | CDATA_REGISTER, 10, buffer);
    
    alpha = buffer.getShort(0);
    if(red < 0) { alpha += 0b10000000000000000; }
    
    red = buffer.getShort(2);
    if(red < 0) { red += 0b10000000000000000; }
    
    green = buffer.getShort(4);
    if(green < 0) { green += 0b10000000000000000; }
    
    blue = buffer.getShort(6); 
    if(blue < 0) { blue += 0b10000000000000000; }
    
    prox = buffer.getShort(8); 
    if(prox < 0) { prox += 0b10000000000000000; }
    SmartDashboard.putNumber("redValue", red);
    SmartDashboard.putNumber("greenValue", green);
    SmartDashboard.putNumber("blueValue", blue);
    SmartDashboard.putNumber("alphaValue", alpha);
    SmartDashboard.putNumber("proximityValue", prox);
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
