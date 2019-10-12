package anthonyyaghi.geneticalgorithm.desktop;

import anthonyyaghi.geneticalgorithm.StupidGame;
import anthonyyaghi.geneticalgorithm.utils.GameConfig;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Neural Nets Fun!";
		config.width = (int) GameConfig.VIEW_HEIGHT;
		config.height = (int) GameConfig.VIEW_WIDTH;
		new LwjglApplication(new StupidGame(), config);
	}
}
