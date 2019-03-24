package frc.robot;

import org.opencv.core.Point;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Sendable;
import frc.robot.commandGroups.AutonomousRoutines;
import frc.robot.commandGroups.TestAutoPaths;
import frc.robot.commands.ActivateClimberPistons;
import frc.robot.commands.DriveOnHeadingDistance;
import frc.robot.commands.DriveToVisionTape;
import frc.robot.commands.FollowPathWeaverFile;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.CargoAcquirer;
import frc.robot.subsystems.ClimberArmWheels;
import frc.robot.subsystems.ClimberRear;
import frc.robot.subsystems.ClimberPistons;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.Gearbox;
import frc.robot.subsystems.HatchClawSubsystem;
import frc.robot.subsystems.HatchExtensionSubsystem;
import frc.robot.utilities.NRGPreferences;
import frc.robot.utilities.PositionTracker;
import frc.robot.utilities.VisionTargets;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  public static final String DEFAULT_TEST_PATH = "LEFT_TO_CARGO_FRONT_LEFT_HATCH";

  public static OI oi;

  public static Gearbox gearbox;
  public static Drive drive;
  public static CargoAcquirer cargoAcquirer;
  public static Arm arm;
  public static ClimberRear climberRear;
  public static ClimberArmWheels climberArmWheels;
  public static ClimberPistons climberPistons;
  public static HatchClawSubsystem hatchClaw;
  public static HatchExtensionSubsystem hatchExtension;

  public static PositionTracker positionTracker = new PositionTracker();
  // public static PowerDistributionPanel pdp = new PowerDistributionPanel();

  public static Watchdog watchdog = new Watchdog(0.02, () -> {
  });

  Command autonomousCommand;
  public static SendableChooser<AutoStartingPosition> autoStartingPositionChooser;
  public static SendableChooser<AutoMovement> autoMovementChooser;
  public static VisionTargets visionTargets;
  public static SendableChooser<AutoFeederPosition> autoStationPositionChooser;
  public static SendableChooser<AutoMovement> autoMovement2Chooser;
  public static SendableChooser<HabitatLevel> habLevelChooser;

  public enum AutoStartingPosition {
    LEFT, CENTER, RIGHT
  }

  public enum AutoFeederPosition {
    NONE, LEFT_FEEDER, RIGHT_FEEDER
  }

  public enum AutoMovement {
    NONE, FORWARD, CARGO_FRONT_LEFT_HATCH, CARGO_FRONT_RIGHT_HATCH, ROCKET_CLOSE
  }

  public enum HabitatLevel {
    LEVEL_1, LEVEL_2
  }

  public static Boolean isPracticeBot() {
    return NRGPreferences.BooleanPrefs.USING_PRACTICE_BOT.getValue();
  }

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    System.out.println("robotInit()");


    RobotMap.init();
    NRGPreferences.init();

    // initialize subsystems
    drive = new Drive();
    gearbox = new Gearbox();
    arm = new Arm();
    climberPistons = new ClimberPistons();
    climberRear = new ClimberRear();
    cargoAcquirer = new CargoAcquirer();
    climberArmWheels = new ClimberArmWheels();
    hatchClaw = new HatchClawSubsystem();
    hatchExtension = new HatchExtensionSubsystem();

    oi = new OI();
    visionTargets = new VisionTargets();

    autoStartingPositionChooser = new SendableChooser<AutoStartingPosition>();
    autoStartingPositionChooser.addDefault("Left", AutoStartingPosition.LEFT);
    autoStartingPositionChooser.addObject("Center", AutoStartingPosition.CENTER);
    autoStartingPositionChooser.addObject("Right", AutoStartingPosition.RIGHT);

    autoStationPositionChooser = new SendableChooser<AutoFeederPosition>();
    autoStationPositionChooser.addDefault("None", AutoFeederPosition.NONE);
    autoStationPositionChooser.addObject("Left", AutoFeederPosition.LEFT_FEEDER);
    autoStationPositionChooser.addObject("Right", AutoFeederPosition.RIGHT_FEEDER);

    autoMovementChooser = new SendableChooser<AutoMovement>();
    autoMovementChooser.addDefault("None", AutoMovement.NONE);
    autoMovementChooser.addObject("Forward", AutoMovement.FORWARD);
    autoMovementChooser.addObject("Cargo_front_left_hatch", AutoMovement.CARGO_FRONT_LEFT_HATCH);
    autoMovementChooser.addObject("Cargo_front_right_hatch", AutoMovement.CARGO_FRONT_RIGHT_HATCH);
    autoMovementChooser.addObject("Rocket_close", AutoMovement.ROCKET_CLOSE);

    autoMovement2Chooser = new SendableChooser<AutoMovement>();
    autoMovement2Chooser.addDefault("None", AutoMovement.NONE);
    autoMovement2Chooser.addObject("Forward", AutoMovement.FORWARD);
    autoMovement2Chooser.addObject("Cargo_front_left_hatch", AutoMovement.CARGO_FRONT_LEFT_HATCH);
    autoMovement2Chooser.addObject("Cargo_front_right_hatch", AutoMovement.CARGO_FRONT_RIGHT_HATCH);
    autoMovement2Chooser.addObject("Rocket_close", AutoMovement.ROCKET_CLOSE);


    habLevelChooser = new SendableChooser<HabitatLevel>();
    habLevelChooser.setDefaultOption("Level 1", HabitatLevel.LEVEL_1);
    habLevelChooser.addOption("Level 2", HabitatLevel.LEVEL_2);

    // Shuffleboard.getTab("Power").add(Robot.pdp).withPosition(0, 0).withSize(3,
    // 3);

    ShuffleboardTab autoTab = Shuffleboard.getTab("Auto");
    autoTab.add("Start", autoStartingPositionChooser).withWidget(BuiltInWidgets.kSplitButtonChooser).withPosition(0, 0)
        .withSize(4, 1);
    autoTab.add("First Hatch", autoMovementChooser).withWidget(BuiltInWidgets.kSplitButtonChooser).withPosition(0, 1)
        .withSize(4, 1);
    autoTab.add("Feeder", autoStationPositionChooser).withWidget(BuiltInWidgets.kSplitButtonChooser).withPosition(0, 2)
        .withSize(4, 1);
    autoTab.add("End", autoMovement2Chooser).withWidget(BuiltInWidgets.kSplitButtonChooser).withPosition(0, 3)
        .withSize(4, 1);
    autoTab.add("Habitat Level", habLevelChooser).withWidget(BuiltInWidgets.kSplitButtonChooser).withPosition(4, 0)
        .withSize(2, 1);

    arm.initShuffleboard();

    ShuffleboardTab testTab = Shuffleboard.getTab("Test");
    testTab.add("Test Path", (new InstantCommand(() -> {
      String pathname = NRGPreferences.StringPrefs.TEST_PATH_NAME.getValue();
      new FollowPathWeaverFile("output/" + pathname + ".pf1.csv").start();
    }))).withSize(2, 1).withPosition(0, 0);
    ShuffleboardLayout distanceButtonLayout = testTab.getLayout("Test Distance", BuiltInLayouts.kList)
        .withPosition(0, 1).withSize(2, 2);
    distanceButtonLayout.add("12 Inches", new DriveOnHeadingDistance(0, 12, 0.7));
    distanceButtonLayout.add("24 Inches", new DriveOnHeadingDistance(0, 24, 0.7));
    distanceButtonLayout.add("48 Inches", new DriveOnHeadingDistance(0, 48, 0.7));
    testTab.add("Position Tracker", positionTracker).withSize(2, 3).withPosition(2, 0);
    climberPistons.activate(false);
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

    SmartDashboard.putBoolean("Vision/cameraInverted", Robot.arm.isCameraInverted());
    boolean hasTargets = visionTargets.hasTargets();
    SmartDashboard.putBoolean("Vision/hasTargets", hasTargets);
    if (hasTargets) {
      SmartDashboard.putNumber("Vision/angleToTarget", visionTargets.getAngleToTarget());
      SmartDashboard.putNumber("Vision/distance", visionTargets.getDistanceToTarget());
      Point center = visionTargets.getCenterOfTargets();
      SmartDashboard.putNumber("Vision/centerX", center.x);
      SmartDashboard.putNumber("Vision/centerY", center.y);
    }

    SmartDashboard.putBoolean("Hatch/DefenseOK", Robot.hatchClaw.isOpen() && Robot.hatchExtension.isRetracted());
    SmartDashboard.putNumber("Trigger/Right", Robot.oi.xboxController.getRawAxis(2));
    SmartDashboard.putNumber("Trigger/Left", Robot.oi.xboxController.getRawAxis(3));
  }

  /**
   * This function is called once each time the robot e%nters Disabled mode. You
   * can use it to reset any subsystem information you want to clear when the
   * robot is disabled.
   */
  @Override
  public void disabledInit() {
    RobotMap.cameraLights.set(Value.kOff);
  }

  @Override
  public void disabledPeriodic() {
    visionTargets.update();
    Scheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    System.out.println("autonomousInit()");
    RobotMap.cameraLights.set(Value.kForward);
    RobotMap.resetSensors();
    Robot.arm.armAnglePIDInit();

    autonomousCommand = new AutonomousRoutines();
    if (autonomousCommand != null) {
      autonomousCommand.start();
    }
  }

  @Override
  public void autonomousPeriodic() {
    watchdog.reset();
    positionTracker.updatePosition();
    watchdog.addEpoch("position tracker");
    visionTargets.update();
    watchdog.addEpoch("vision targets");
    Robot.arm.armAnglePIDExecute();
    watchdog.addEpoch("arm angle PID");
    Scheduler.getInstance().run();
    watchdog.addEpoch("scheduler");
    if (watchdog.isExpired()) {
      watchdog.printEpochs();
    }
  }

  @Override
  public void teleopInit() {
    System.out.println("teleopInit()");
    RobotMap.cameraLights.set(Value.kForward);
    Robot.arm.armAnglePIDInit();
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
    watchdog.reset();
    positionTracker.updatePosition();
    watchdog.addEpoch("position tracker");
    visionTargets.update();
    watchdog.addEpoch("vision targets");
    Robot.arm.armAnglePIDExecute();
    watchdog.addEpoch("arm angle PID");
    Scheduler.getInstance().run();
    watchdog.addEpoch("scheduler");
    if (watchdog.isExpired()) {
      watchdog.printEpochs();
    }
  }

  @Override
  public void testPeriodic() {
    positionTracker.updatePosition();
    visionTargets.update();
  }
}