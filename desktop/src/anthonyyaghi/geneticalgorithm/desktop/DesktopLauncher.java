package anthonyyaghi.geneticalgorithm.desktop;

import anthonyyaghi.geneticalgorithm.MyGdxGame;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Neural Nets Fun!";
		config.width = 1920;
		config.height = 1080;
		new LwjglApplication(new MyGdxGame(), config);
	}
}
