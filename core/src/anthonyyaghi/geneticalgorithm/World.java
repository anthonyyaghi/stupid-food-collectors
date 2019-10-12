package anthonyyaghi.geneticalgorithm;

import anthonyyaghi.geneticalgorithm.agents.AgentConfig;
import anthonyyaghi.geneticalgorithm.agents.NN;
import anthonyyaghi.geneticalgorithm.algo.GA;
import anthonyyaghi.geneticalgorithm.utils.CameraControls;
import anthonyyaghi.geneticalgorithm.utils.GameConfig;
import anthonyyaghi.geneticalgorithm.utils.ViewPortUtils;
import anthonyyaghi.geneticalgorithm.utils.VisionValue;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Random;


public class World implements Screen {

    public static final float GA_INTERVAL = 15.0f;
    public final float VISION_RANGE = 400f;
    private final int NB_CREATURE = 120, NB_FOOD = 70;
    private final double MUTATION_RATE = 0.01d, SURVIVAL_RATE = 0.2d;
    private final float BULLET_SPEED = 500;
    private final float LINE_SPACING = 100f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;
    private CameraControls cameraControls;
    private SpriteBatch batch;
    private BitmapFont font;
    private ArrayList<Sprite> food, bullets, newFood, toRemoveFood, newBullets, toRemoveBullets;

    private int atbScore, generation;
    private ArrayList<NN> creatures;
    private Texture foodTexture, topTexture, normalTexture, bulletTexture;
    private float timeElapsed;

    private GA gaEvolver;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();
        batch = new SpriteBatch();
        cameraControls = new CameraControls();
        cameraControls.setStartPosition(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2);

        gaEvolver = new GA(normalTexture, topTexture, SURVIVAL_RATE, MUTATION_RATE);

        creatures = new ArrayList<NN>();
        for (int i = 0; i < NB_CREATURE; i++) {
            creatures.add(new NN(AgentConfig.NEURAL_NET_STRUCT, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT,
                    AgentConfig.MOVE_SPEED, AgentConfig.ROT_SPEED));
        }
        atbScore = 0;

        //Initiate food array
        food = new ArrayList<Sprite>();
        foodTexture = new Texture("img/food.png");
        for (int i = 0; i < NB_FOOD; i++) {
            food.add(new Sprite(foodTexture));
        }

        Random rand = new Random();
        for (Sprite f : food) {
            f.setScale(0.5f);
            f.setX(rand.nextFloat() * GameConfig.WORLD_WIDTH);
            f.setY(rand.nextFloat() * GameConfig.WORLD_HEIGHT);
        }

        newFood = new ArrayList<Sprite>();
        toRemoveFood = new ArrayList<Sprite>();
        newBullets = new ArrayList<Sprite>();
        toRemoveBullets = new ArrayList<Sprite>();
        bullets = new ArrayList<Sprite>();

        timeElapsed = 0;
        font = new BitmapFont();
        generation = 1;
        topTexture = new Texture("img/yellowPlayer.png");
        normalTexture = new Texture(Gdx.files.internal("img/bluePlayer.png"));
        bulletTexture = new Texture("img/bullet.png");
    }

    @Override
    public void render(float delta) {
        updateWorld(delta);

        cameraControls.handleInput(delta);
        cameraControls.applyToCamera(camera);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setProjectionMatrix(camera.combined);
        ViewPortUtils.drawGrid(viewport, renderer);
        renderWorld();
    }

    private void renderWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //Drawing the creatures
        for (NN creature : creatures) {
            if (creature.getX() <= GameConfig.WORLD_WIDTH && creature.getY() <= GameConfig.WORLD_HEIGHT)
                creature.drawCreature(batch);
        }

        //Drawing the food
        for (Sprite f : food) {
            if (f.getX() <= GameConfig.WORLD_WIDTH && f.getY() <= GameConfig.WORLD_HEIGHT)
                f.draw(batch);
        }

        //Drawing the bullets
        for (Sprite b : bullets) {
            if (b.getX() <= GameConfig.WORLD_WIDTH && b.getY() <= GameConfig.WORLD_HEIGHT)
                b.draw(batch);
        }

        font.getData().setScale(5);
        font.setColor(0.0f, 1.0f, 0.0f, 1.0f);
        font.draw(batch, generation + "# generation", GameConfig.WORLD_WIDTH / 2 - 300, GameConfig.WORLD_HEIGHT - 1 * LINE_SPACING);
        font.draw(batch, "Population size: " + creatures.size(), GameConfig.WORLD_WIDTH / 2 - 300, GameConfig.WORLD_HEIGHT - 2 * LINE_SPACING);
        font.draw(batch, "All time top individual: " + atbScore, GameConfig.WORLD_WIDTH / 2 - 300, GameConfig.WORLD_HEIGHT - 3 * LINE_SPACING);

        batch.end();
    }

    private void updateWorld(float delta) {
        newFood.clear();
        toRemoveFood.clear();
        newBullets.clear();
        toRemoveBullets.clear();

        for (NN creature : creatures) {
            creature.processFreeze(delta);

            VisionValue foodVisionValue = getFoodVision(creature);
            VisionValue creatureVisionValue = updateBulletAndGetVision(creature, delta);
            VisionValue bulletVisionValue = getCreatureVision(creature);

            creature.feedForward(new double[]{foodVisionValue.getLeft(), foodVisionValue.getCenter(), foodVisionValue.getRight(),
                    creatureVisionValue.getLeft(), creatureVisionValue.getCenter(), creatureVisionValue.getRight(),
                    bulletVisionValue.getLeft(), bulletVisionValue.getCenter(), bulletVisionValue.getRight()});

            creature.act(delta);

            if (creature.canFire() && creature.wantsToFire()) {
                Sprite newBullet = new Sprite(bulletTexture);
                newBullet.setScale(0.3f);
                newBullet.setCenter(newBullet.getWidth() / 2, newBullet.getHeight() / 2);
                Vector2 direction = creature.getDirection();
                newBullet.setRotation(creature.getRotation());
                newBullet.setX(creature.getX() + (direction.x * 50));
                newBullet.setY(creature.getY() + (direction.y * 50));

                newBullets.add(newBullet);
            }
        }


        for (Sprite b : toRemoveBullets) {
            bullets.remove(b);
        }
        bullets.addAll(newBullets);


        for (Sprite f : toRemoveFood) {
            food.remove(f);
        }
        food.addAll(newFood);
        int toTrim = food.size() - NB_FOOD;
        for (int s = 0; s < toTrim; s++) {
            food.remove(food.size() - 1);
        }

        //GA process
        timeElapsed += delta;
        evolveWithGA();
    }

    private void evolveWithGA() {
        if (timeElapsed > GA_INTERVAL) {
            timeElapsed = 0;
            generation++;
            atbScore = gaEvolver.evolve(creatures, atbScore);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    private void updateVisionValue(float angleWithObject, Vector2 objectPosition, double closeness, VisionValue currentValue) {
        if (angleWithObject > -60f && angleWithObject <= -20f) {
            if (objectPosition.len() <= VISION_RANGE && closeness > currentValue.getRight()) {
                currentValue.setRight(closeness);
            }
        } else if (angleWithObject > -20f && angleWithObject <= 20f) {
            if (objectPosition.len() <= VISION_RANGE && closeness > currentValue.getCenter()) {
                currentValue.setCenter(closeness);
            }
        } else if (angleWithObject > 20f && angleWithObject <= 60f) {
            if (objectPosition.len() <= VISION_RANGE && closeness > currentValue.getLeft()) {
                currentValue.setLeft(closeness);
            }
        }
    }

    private VisionValue getFoodVision(NN creature) {
        VisionValue foodVision = new VisionValue();

        for (Sprite f : food) {
            if (creature.collidingWith(f)) {
                creature.setScore(creature.getScore() + 200);
                toRemoveFood.add(f);
                Sprite newUnit = new Sprite(foodTexture);
                newUnit.setScale(0.5f);
                Random rand = new Random();
                newUnit.setX(rand.nextFloat() * GameConfig.WORLD_WIDTH);
                newUnit.setY(rand.nextFloat() * GameConfig.WORLD_HEIGHT);
                newFood.add(newUnit);
            }

            Vector2 foodPosition = new Vector2(f.getX() - creature.getX(), f.getY() - creature.getY());
            Vector2 creatureHeadingDirection = creature.getDirection();
            float angleWithFood = creatureHeadingDirection.angle(foodPosition);    //angle between the create and the food, from -90 to 0 food is on the right, from 0 to 90 food is on the left

            //dividing the areas:
            //right: -60 to -20
            //center: -20 to 20
            //left: 20 to  60
            float closeness = (400f - foodPosition.len()) / 400f;
            updateVisionValue(angleWithFood, foodPosition, closeness, foodVision);
        }
        return foodVision;
    }

    private VisionValue updateBulletAndGetVision(NN creature, float delta) {
        VisionValue bulletVision = new VisionValue();

        for (Sprite b : bullets) {
            if (creature.collidingWith(b)) {
                creature.setSpeed(0);
                toRemoveBullets.add(b);
            }


            // check if bullets is too far and remove it
            Vector2 bulletDistance = new Vector2(b.getX() - (GameConfig.WORLD_WIDTH / 2), b.getY() - (GameConfig.WORLD_HEIGHT / 2));
            if (bulletDistance.len() > GameConfig.WORLD_WIDTH * 3 && !toRemoveBullets.contains(b))
                toRemoveBullets.add(b);

            //move the bullet
            //first get the direction the entity is pointed
            Vector2 Bulletdirection = new Vector2();
            Bulletdirection.x = (float) Math.cos(Math.toRadians(b.getRotation()));
            Bulletdirection.y = (float) Math.sin(Math.toRadians(b.getRotation()));

            //normalize the vector
            Bulletdirection = Bulletdirection.nor();

            //Then scale it by the current speed to get the velocity
            Vector2 velocity = new Vector2();
            velocity.x = Bulletdirection.x * BULLET_SPEED / creatures.size();
            velocity.y = Bulletdirection.y * BULLET_SPEED / creatures.size();

            b.setX(b.getX() + (velocity.x * delta));
            b.setY(b.getY() + (velocity.y * delta));


            Vector2 bulletPosition = new Vector2(b.getX() - creature.getX(), b.getY() - creature.getY());
            Vector2 creatureHeadingDirection = creature.getDirection();
            float angleWithBullet = creatureHeadingDirection.angle(bulletPosition);
            float closeness = (VISION_RANGE - bulletPosition.len()) / VISION_RANGE;
            updateVisionValue(angleWithBullet, bulletPosition, closeness, bulletVision);
        }
        return bulletVision;
    }

    private VisionValue getCreatureVision(NN creature) {
        VisionValue creatureVision = new VisionValue();
        for (NN c : creatures) {
            if (c != creature) {
                Vector2 otherPosition = new Vector2(c.getX() - creature.getX(), c.getY() - creature.getY());
                Vector2 creatureHeadingDirection = creature.getDirection();
                float angleWithOther = creatureHeadingDirection.angle(otherPosition);    //angle between the create and the food, from -90 to 0 food is on the right, from 0 to 90 food is on the left
                float closeness = (400f - otherPosition.len()) / 400f;
                updateVisionValue(angleWithOther, otherPosition, closeness, creatureVision);
            }
        }
        return creatureVision;
    }

}
