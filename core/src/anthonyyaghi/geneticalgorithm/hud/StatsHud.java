package anthonyyaghi.geneticalgorithm.hud;

import anthonyyaghi.geneticalgorithm.utils.GameConfig;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class StatsHud {
    public static final String GENERATION_TEXT = "Generation: ";
    public static final String POPULATION_SIZE_TEXT = "Population size: ";
    public static final String TOP_SCORE_TEXT = "Top score: ";
    private FitViewport viewport;
    private Stage stage;

    private Label generationLabel;
    private Label populationSizeLabel;
    private Label topScoreLabel;
    private int generation;
    private int populationSize;
    private int topScore;

    public StatsHud(SpriteBatch spriteBatch) {
        viewport = new FitViewport(GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, spriteBatch);

        generation = 0;
        populationSize = 0;
        topScore = 0;

        Table table = new Table();
        table.top();
        table.setFillParent(true);
        generationLabel = new Label(GENERATION_TEXT + generation, new Label.LabelStyle(new BitmapFont(), Color.YELLOW));
        populationSizeLabel = new Label(POPULATION_SIZE_TEXT + populationSize, new Label.LabelStyle(new BitmapFont(), Color.YELLOW));
        topScoreLabel = new Label(TOP_SCORE_TEXT + topScore, new Label.LabelStyle(new BitmapFont(), Color.YELLOW));

        table.add(generationLabel).expandX();
        table.row();
        table.add(populationSizeLabel).expandX();
        table.row();
        table.add(topScoreLabel).expandX();

        stage.addActor(table);
    }

    public void update(int generation, int populationSize, int topScore) {
        this.generation = generation;
        this.populationSize = populationSize;
        this.topScore = topScore;

        generationLabel.setText(GENERATION_TEXT + generation);
        populationSizeLabel.setText(POPULATION_SIZE_TEXT + populationSize);
        topScoreLabel.setText(TOP_SCORE_TEXT + topScore);
    }

    public Stage getStage() {
        return stage;
    }
}
