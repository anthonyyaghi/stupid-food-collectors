package anthonyyaghi.geneticalgorithm;

import com.badlogic.gdx.Game;

public class MyGdxGame extends Game {

	@Override
	public void create () {
		setScreen(new World());
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose () {
		super.dispose();

	}
}
