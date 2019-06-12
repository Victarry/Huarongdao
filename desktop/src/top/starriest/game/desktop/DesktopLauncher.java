package top.starriest.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import top.starriest.game.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 1000;
		config.width = 800;
		System.out.println("Working Directory = " +
				System.getProperty("user.dir"));
		new LwjglApplication(new Game(), config);
	}
}
