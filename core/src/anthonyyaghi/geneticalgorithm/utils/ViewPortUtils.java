package anthonyyaghi.geneticalgorithm.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewPortUtils {
    private static final int DEFAULT_CELL_SIZE = 250;

    public static void drawGrid(Viewport viewport, ShapeRenderer renderer) {
        Color oldColor = new Color(renderer.getColor());

        int worldWidth = (int) GameConfig.WORLD_WIDTH;
        int worldHeight = (int) GameConfig.WORLD_HEIGHT;

        renderer.setProjectionMatrix(viewport.getCamera().combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        renderer.setColor(Color.WHITE);

        for (int x = 0; x < worldWidth; x += DEFAULT_CELL_SIZE) {
            renderer.line(x, 0, x, worldHeight);
        }

        for (int y = 0; y < worldHeight; y += DEFAULT_CELL_SIZE) {
            renderer.line(0, y, worldWidth, y);
        }

        renderer.setColor(Color.GREEN);
        renderer.line(0, 0, 0, worldHeight);
        renderer.line(0, 0, worldWidth, 0);
        renderer.line(0, worldHeight, worldWidth, worldHeight);
        renderer.line(worldWidth, 0, worldWidth, worldHeight);

        renderer.end();
        renderer.setColor(oldColor);
    }

    private ViewPortUtils() {
    }
}
