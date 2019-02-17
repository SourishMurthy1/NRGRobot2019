package frc.robot;

import org.opencv.core.Point;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Sendable;
import frc.robot.commandGroups.AutonomousRoutines;
import frc.robot.commands.ActivateClimberPistons;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.CargoAcquirer;
import frc.robot.subsystems.ClimberMotor;
import frc.robot.subsystems.ClimberPistons;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.Gearbox;
import frc.robot.subsystems.HatchClawSubsystem;
import frc.robot.subsystems.HatchExtensionSubsystem;
import frc.robot.utilities.PositionTracker;
import frc.robot.utilities.PreferenceKeys;
import frc.robot.utilities.VisionTargets;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  public static OI oi;

  public static Gearbox gearbox;
  public static Drive drive;
  public static CargoAcquirer cargoAcquirer;
  public static Arm arm;
  public static ClimberMotor climberMotor;
  public static ClimberPistons climberPistons;
  public static HatchClawSubsystem hatchClaw;
  public static HatchExtensionSubsystem hatchExtension;

  public static PositionTracker positionTracker = new PositionTracker();
  public static PowerDistributionPanel pdp = new PowerDistributionPanel();

  public static Preferences preferences;

  public static VisionTargets visionTargets;
  
  Command autonomousCommand;
	public static SendableChooser<AutoStartingPosition> autoStartingPositionChooser;
  public static SendableChooser<AutoMovement> autoMovementChooser;
  
  public enum AutoStartingPosition {
		LEFT, CENTER, RIGHT
	}

	public enum AutoMovement {
		NONE, FORWARD, CARGO_FRONT_LEFT_HATCH, CARGO_FRONT_RIGHT_HATCH
	}

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    System.out.println("robotInit()");

		preferences = Preferences.getInstance();
    RobotMap.init();
    //initialize subsystems
    drive = new Drive();
    gearbox = new Gearbox();
    arm = new Arm();
    climberMotor = new ClimberMotor();
    climberPistons = new ClimberPistons();
    cargoAcquirer = new CargoAcquirer(); 
    hatchClaw = new HatchClawSubsystem();
    hatchExtension = new HatchExtensionSubsystem();
  
    oi = new OI();
    LiveWindow.addSensor("pdp", "pdp", Robot.pdp);
    visionTargets = new VisionTargets();

    autoStartingPositionChooser = new SendableChooser<AutoStartingPosition>();
		autoStartingPositionChooser.addDefault("Left", AutoStartingPosition.LEFT);
		autoStartingPositionChooser.addObject("Center", AutoStartingPosition.CENTER);
		autoStartingPositionChooser.addObject("Right", AutoStartingPosition.RIGHT);

		autoMovementChooser = new SendableChooser<AutoMovement>();
    autoMovementChooser.addDefault("None", AutoMovement.NONE);
		autoMovementChooser.addDefault("Forward", AutoMovement.FORWARD);
    autoMovementChooser.addDefault("Cargo_front_left_hatch", AutoMovement.CARGO_FRONT_LEFT_HATCH);
    autoMovementChooser.addDefault("Cargo_front_right_hatch", AutoMovement.CARGO_FRONT_RIGHT_HATCH);

		SmartDashboard.putData("Choose autonomous position", autoStartingPositionChooser);
		SmartDashboard.putData("Choose autonomous movement", autoMovementChooser);

		System.out.println("robotInit() done");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("PositionTracker/x", positionTracker.getX());
    SmartDashboard.putNumber("PositionTracker/y", positionTracker.getY());
    SmartDashboard.putNumber("PositionTracker/maxVelocity", positionTracker.getMaxVelocity());
    SmartDashboard.putData("LeftEncoder", RobotMap.driveLeftEncoder);
    SmartDashboard.putData("RightEncoder", RobotMap.driveRightEncoder);
    SmartDashboard.putNumber("Gyro", RobotMap.navx.getAngle());
    SmartDashboard.putData("DriveSubsystem", Robot.drive);

    boolean hasTargets = visionTargets.hasTargets();
    SmartDashboard.putBoolean("Vision/hasTargets", hasTargets);
    if(hasTargets) {
      SmartDashboard.putNumber("Vision/angleToTarget", visionTargets.getAngleToTarget());
      SmartDashboard.putNumber("Vision/distance", visionTargets.getDistanceToTarget());
      Point center = visionTargets.getCenterOfTargets();
      SmartDashboard.putNumber("Vision/centerX", center.x);
      SmartDashboard.putNumber("Vision/centerY", center.y);
    }
  }

  /**
   * This function is called once each time the robot enters Disabled mode. You
   * can use it to reset any subsystem information you want to clear when the
   * robot is disabled.
   */
  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
    visionTargets.update();
    Scheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    System.out.println("autonomousInit()");
    RobotMap.resetSensors();
		autonomousCommand = new AutonomousRoutines();
    if (autonomousCommand != null) {
      autonomousCommand.start();
    }
  }

  @Override
  public void autonomousPeriodic() {
    positionTracker.updatePosition();
    visionTargets.update();
    Scheduler.getInstance().run();
  }

  @Override
  public void teleopInit() {
    System.out.println("teleopInit()");
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
    positionTracker.updatePosition();
    visionTargets.update();
    Scheduler.getInstance().run();
  }

  @Override
  public void testPeriodic() {
    positionTracker.updatePosition();
    visionTargets.update();
  }

  public void initPreferences() {
		if (preferences.getBoolean(PreferenceKeys.WRITE_DEFAULT, true)) {
      preferences.putBoolean(PreferenceKeys.WRITE_DEFAULT, false);

			preferences.putDouble(PreferenceKeys.TURN_P_TERM, Drive.DEFAULT_TURN_P);
			preferences.putDouble(PreferenceKeys.TURN_I_TERM, Drive.DEFAULT_TURN_I);
			preferences.putDouble(PreferenceKeys.TURN_D_TERM, Drive.DEFAULT_TURN_D);

			preferences.putDouble(PreferenceKeys.DRIVE_P_TERM, Drive.DEFAULT_DRIVE_P);
			preferences.putDouble(PreferenceKeys.DRIVE_I_TERM, Drive.DEFAULT_DRIVE_I);
      preferences.putDouble(PreferenceKeys.DRIVE_D_TERM, Drive.DEFAULT_DRIVE_D);
      
			preferences.putDouble(PreferenceKeys.ARM_P_TERM, Arm.DEFAULT_ARM_P);
			preferences.putDouble(PreferenceKeys.ARM_I_TERM, Arm.DEFAULT_ARM_I);
			preferences.putDouble(PreferenceKeys.ARM_D_TERM, Arm.DEFAULT_ARM_D);
			preferences.putDouble(PreferenceKeys.ARM_UP_MAX_POWER, Arm.ARM_UP_MAX_POWER);
			preferences.putDouble(PreferenceKeys.ARM_DOWN_MAX_POWER, Arm.ARM_DOWN_MAX_POWER);

			preferences.putInt(PreferenceKeys.ARM_STOWED_TICKS, Arm.DEFAULT_ARM_STOWED_TICKS);
			preferences.putInt(PreferenceKeys.ARM_CARGO_SHIP_TICKS, Arm.DEFAULT_ARM_CARGO_SHIP_TICKS);
			preferences.putInt(PreferenceKeys.ARM_ROCKET_CARGO_LOW_TICKS, Arm.DEFAULT_ARM_ROCKET_CARGO_LOW_TICKS);
			preferences.putInt(PreferenceKeys.ARM_ROCKET_CARGO_MEDIUM_TICKS, Arm.DEFAULT_ARM_ROCKET_CARGO_MEDIUM_TICKS);
      preferences.putInt(PreferenceKeys.ARM_ROCKET_CARGO_HIGH_TICKS, Arm.DEFAULT_ARM_ROCKET_CARGO_HIGH_TICKS);

			preferences.putBoolean(PreferenceKeys.USE_PHYSICAL_AUTO_CHOOSER, true);
			preferences.putBoolean(PreferenceKeys.USING_PRACTICE_BOT, true);
		}
  }
}