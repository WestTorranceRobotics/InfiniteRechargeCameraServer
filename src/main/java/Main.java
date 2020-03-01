import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import edu.wpi.cscore.HttpCamera;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.VideoCamera;
import edu.wpi.cscore.VideoSource.ConnectionStrategy;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

public final class Main {

  private static final int[] cameraPorts = {0, 2, 4};
  public static final int NUMBER_CAMERAS = cameraPorts.length;

  private Main() {
  }

  /**
   * Main.
   */
  public static void main(String... args) {

    CameraServer server = CameraServer.getInstance();
    
    VideoCamera[] cameras = new VideoCamera[NUMBER_CAMERAS + 1];
    for (int i = 0; i < NUMBER_CAMERAS; i++) {
      cameras[i] = server.startAutomaticCapture(cameraPorts[i]);
      cameras[i].setConnectionStrategy(ConnectionStrategy.kKeepOpen);
    }
    cameras[NUMBER_CAMERAS] = new HttpCamera("limelight", "http://limelight.local:5800");
    
    MjpegServer output = (MjpegServer) server.getServer();
    output.setSource(cameras[0]);
    output.setResolution(720, 480);
    output.setCompression(0);
    output.setDefaultCompression(0);
    output.setFPS(30);

    NetworkTable table = NetworkTableInstance.getDefault().getTable("rpi");
    NetworkTableEntry aimbot = table.getEntry("aimbot");
    NetworkTableEntry camera = table.getEntry("camera");

    NetworkTableEntry pipeline = NetworkTableInstance.getDefault()
      .getTable("limelight").getEntry("pipeline");
    
    camera.addListener((change) -> {
      int selected = (int) change.value.getDouble();
      output.setSource(cameras[selected]);
      if (aimbot.getDouble(0) != 1 && selected == NUMBER_CAMERAS) {
        pipeline.setDouble(1);
      }
      if (aimbot.getDouble(0) == 1 && pipeline.getDouble(0) != 0) {
        pipeline.setDouble(0);
      }
    }, generateAllFlagsMask());

    Shuffleboard.getTab("Driving Display").add("Selected Camera View", output)
    .withSize(8, 4).withPosition(1, 0).withWidget(BuiltInWidgets.kCameraStream);

    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }

  /**
   * Iterates through the fields in {@link EntryListenerFlags} to find all
   * public, static, final integers and mask them together. It is assumed that
   * no other integers will exist in the class than those denoting possible flags,
   * and that only public, static, and final integers may represent flag masks.
   * All non-integer fields or fields without the correct modifiers are skipped,
   * while those found are masked together with a bitwise OR.
   * 
   * @return A mask representing all possible entry listener change flags
   */
  private static int generateAllFlagsMask() {
    int mask = 0;
    for (Field f : EntryListenerFlags.class.getDeclaredFields()) {
      if (f.getType() == Integer.TYPE) {
        int mods = f.getModifiers();
        if ((mods & Modifier.PUBLIC) == 0
          || (mods & Modifier.STATIC) == 0
          || (mods & Modifier.FINAL) == 0) {
            continue;
        }
        try {
          mask |= f.getInt(new EntryListenerFlags(){});
        } catch (ReflectiveOperationException ex) {
            // ignore constants that we can't get, because
            // they must not be intended as flag bit masks
        }
      }
    }
    return mask;
  }
}
