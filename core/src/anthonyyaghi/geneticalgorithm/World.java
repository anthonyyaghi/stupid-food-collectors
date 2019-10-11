package anthonyyaghi.geneticalgorithm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class World implements Screen{
	public static final float VISION_RANGE = 400f;
	private final int WORLD_WIDTH = 5000;
	private final int WORLD_HEIGHT = 5000;
	private final int NB_CREATURE = 120, NB_FOOD = 70;
	private final double MUTATION_RATE = 0.01d;
	private final float MOVE_SPEED = 400, ROT_SPEED = 180, DEATH_PERCENTAGE = 0.8f, BULLET_SPEED = 500;
	private final int[] NEURAL_NET_STRUCT = new int[] {9,20,10,4};

	private SpriteBatch batch;
	private BitmapFont font;

	private int maxScore, generation, totalScore, allTimeMax, strongestMan;

	private ArrayList<NN> creatures;
	private ArrayList<Sprite> food, bullets, newFood, toRemoveFood, newBullets, toRemoveBullets;
	private Texture foodTexture, strongTexture, noobTexture, bulletTexture;
	private float timeEl;
	
	
	
	private ArrayList<Double> fittistDNA, youngestDNA;
	@Override
	public void show() {
		// TODO Auto-generated method stub
		batch = new SpriteBatch();

		//Initiate the creatures array and add 50 creatures randomly 
		//Input layer: 1- food to the left
		//			   2- food in front
		//			   3- food to the right

		//Output layer: 1- rotate left
		//				2- move forward
		//				3- rotate right

		creatures = new ArrayList<NN>();
		for(int i = 0; i < NB_CREATURE; i++){
			creatures.add(new NN(NEURAL_NET_STRUCT, WORLD_WIDTH, WORLD_HEIGHT, MOVE_SPEED, ROT_SPEED));
		}


		//Initiate food array
		food = new ArrayList<Sprite>();
		foodTexture = new Texture("img/food.png");
		for (int i = 0; i < NB_FOOD; i++){
			food.add(new Sprite(foodTexture));
		}

		Random rand = new Random();
		for(Sprite f : food){
			f.setScale(0.5f);
			f.setX(rand.nextFloat()*WORLD_WIDTH);
			f.setY(rand.nextFloat()*WORLD_HEIGHT);
		}

		newFood = new ArrayList<Sprite>();
		toRemoveFood = new ArrayList<Sprite>();
		newBullets = new ArrayList<Sprite>();
		toRemoveBullets = new ArrayList<Sprite>();
		bullets = new ArrayList<Sprite>();

		timeEl = 0;
		font = new BitmapFont();
		maxScore = 0;
		generation = 1;
		totalScore = 0;
		allTimeMax = 0;
		strongestMan = 0;
		strongTexture = new Texture("img/yellowPlayer.png");
		noobTexture = new Texture(Gdx.files.internal("img/bluePlayer.png"));
		bulletTexture = new Texture("img/bullet.png");
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//check what creature sees and perform creature action
		newFood.clear();
		toRemoveFood.clear();
		newBullets.clear();
		toRemoveBullets.clear();

		for (NN creature : creatures){
			VisionValue foodVisionValue = new VisionValue();
			VisionValue creatureVisionValue = new VisionValue();
			VisionValue bulletVisionValue = new VisionValue();

			creature.processFreeze(delta);

			//check for collision and vision of food
			foodVisionValue = getFoodVision(creature);
			//check for collision and vision of bullets
			bulletVisionValue = updateBulletAndGetVision(creature, delta);
			//vision creatures
			creatureVisionValue = getCreatureVision(creature);

			creature.feedForward(new double[] {foodVisionValue.getLeft(), foodVisionValue.getCenter(), foodVisionValue.getRight(),
					creatureVisionValue.getLeft(), creatureVisionValue.getCenter(), creatureVisionValue.getRight(),
					bulletVisionValue.getLeft(), bulletVisionValue.getCenter(), bulletVisionValue.getRight()});

			creature.act(delta);

			if(creature.canFire() && creature.wantsToFire()){
				Sprite newBullet = new Sprite(bulletTexture);
				newBullet.setScale(0.3f);
				newBullet.setCenter(newBullet.getWidth()/2, newBullet.getHeight()/2);
				Vector2 direction = creature.getDirection();
				newBullet.setRotation(creature.getRotation());
				newBullet.setX(creature.getX() + (direction.x * 50));
				newBullet.setY(creature.getY() + (direction.y * 50));

				newBullets.add(newBullet);
			}
		}


		
		for(Sprite b : toRemoveBullets){
			bullets.remove(b);
		}
		bullets.addAll(newBullets);
		
		
		for(Sprite f : toRemoveFood){
			food.remove(f);
		}
		food.addAll(newFood);
		int toTrim = food.size()-NB_FOOD;
		for(int s = 0; s < toTrim; s++){
			food.remove(food.size()-1);
		}

		batch.begin();

		//Drawing the creatures
		for (NN creature : creatures){
			if (creature.getX() <= 1920 && creature.getY() <= 1080)
				creature.drawCreature(batch);
		}

		//Drawing the food
		for (Sprite f : food){
			if (f.getX() <= 1920 && f.getY() <= 1080)
				f.draw(batch);
		}

		//Drawing the bullets
		for (Sprite b : bullets){
			if (b.getX() <= 1920 && b.getY() <= 1080)
				b.draw(batch);
		}

		font.setColor(0.0f, 1.0f, 0.0f, 1.0f);
		font.draw(batch, generation + "# generation", 1920/2, 1000-10);
		font.draw(batch, "Population size: " + creatures.size(), 1920/2, 1000-25);
		font.draw(batch, "Current fittest individual: " + maxScore + " All time fittest individual: " + strongestMan, 1920/2, 1000-40);
		font.draw(batch, "Total score: " + totalScore + " All time max: " + allTimeMax, 1920/2, 1000-55);

		
		batch.end();

		//GA process
		timeEl += delta;
		if(timeEl > 15.0f){			//each 10 seconds produce a new generation
			timeEl = 0;
			generation++;
			
			
			if(generation%20 == 0){
				try{
					FileOutputStream fos= new FileOutputStream("D:\\fit.dna");
					ObjectOutputStream oos= new ObjectOutputStream(fos);
					oos.writeObject(fittistDNA);
					oos.close();
					fos.close();
					
					fos= new FileOutputStream("D:\\young.dna");
					oos= new ObjectOutputStream(fos);
					oos.writeObject(youngestDNA);
					oos.close();
					fos.close();
				}catch(IOException ioe){
					ioe.printStackTrace();
				}
				System.out.println("bye!");
			}
			
			
			Random rand = new Random();
			Collections.sort(creatures);
			maxScore = creatures.get(0).getScore();
			creatures.get(0).setSprite(strongTexture);
			
			youngestDNA = creatures.get(0).generateDNA();
			
			if (maxScore > strongestMan){
				strongestMan = maxScore;
				fittistDNA = creatures.get(0).generateDNA();
			}
			totalScore = 0;
			for(int x = 0; x < creatures.size(); x++) {
				totalScore+= creatures.get(x).getScore();
				if(x != 0) creatures.get(x).setSprite(noobTexture);
			}
			if (totalScore > allTimeMax) allTimeMax = totalScore;
			//discard weak individuals
			int deathRate = (int) (creatures.size()*DEATH_PERCENTAGE);
			for (int i = 0; i < deathRate; i++){
				creatures.remove(creatures.size()-1);
			}
			ArrayList<NN> newCreatures = new ArrayList<NN>();

			while(creatures.size() + newCreatures.size() < NB_CREATURE){
				for(int i = 0; i < creatures.size(); i++){
					//For each individual choose a random partner
					int index = rand.nextInt(creatures.size());
					if(index == i) index = (index+1)%creatures.size();
					//get both parents dna
					ArrayList<Double> dna1 = creatures.get(i).generateDNA();
					ArrayList<Double> dna2 = creatures.get(index).generateDNA();
					//Apply crossover and mutation
					ArrayList<Double> newDNA = new ArrayList<Double>();

					for(int j = 0; j < dna1.size(); j++){
						double mut = rand.nextDouble();
						if(mut < MUTATION_RATE){
							newDNA.add(rand.nextDouble());
						}
						else{
							double p = rand.nextDouble();
							if(p < 0.5d){
								newDNA.add(dna1.get(j));
							}else{
								newDNA.add(dna2.get(j));
							}
						}
					}
					//create the new individual
					NN baby = new NN(NEURAL_NET_STRUCT, WORLD_WIDTH, WORLD_HEIGHT, MOVE_SPEED, ROT_SPEED);
					baby.applyDNA(newDNA);
					newCreatures.add(baby);
				}
			}

			creatures.addAll(newCreatures);
			//reset the scores in the new generation
			for(NN c : creatures) c.setScore(0);
		}


	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		batch.dispose();
	}

	private void updateVisionValue(float angleWithObject, Vector2 objectPosition, double closeness, VisionValue currentValue) {
		if(angleWithObject > -60f && angleWithObject <= -20f){
			if(objectPosition.len() <= VISION_RANGE && closeness > currentValue.getRight()){
				currentValue.setRight(closeness);
			}
		}
		else if (angleWithObject > -20f && angleWithObject <= 20f){
			if(objectPosition.len() <= VISION_RANGE && closeness > currentValue.getCenter()){
				currentValue.setCenter(closeness);
			}
		}
		else if (angleWithObject > 20f && angleWithObject <= 60f){
			if(objectPosition.len() <= VISION_RANGE && closeness > currentValue.getLeft()){
				currentValue.setLeft(closeness);
			}
		}
	}

	private VisionValue getFoodVision(NN creature) {
		VisionValue foodVision = new VisionValue();

		for(Sprite f : food){
			if(creature.collidingWith(f)){
				creature.setScore(creature.getScore() + 200);
				toRemoveFood.add(f);
				Sprite newUnit = new Sprite(foodTexture);
				newUnit.setScale(0.5f);
				Random rand = new Random();
				newUnit.setX(rand.nextFloat()*WORLD_WIDTH);
				newUnit.setY(rand.nextFloat()*WORLD_HEIGHT);
				newFood.add(newUnit);
			}

			Vector2 foodPosition = new Vector2(f.getX()-creature.getX(), f.getY()-creature.getY());
			Vector2 creatureHeadingDirection = creature.getDirection();
			float angleWithFood = creatureHeadingDirection.angle(foodPosition);	//angle between the create and the food, from -90 to 0 food is on the right, from 0 to 90 food is on the left

			//dividing the areas:
			//right: -60 to -20
			//center: -20 to 20
			//left: 20 to  60
			float closeness = (400f-foodPosition.len())/400f;
			updateVisionValue(angleWithFood, foodPosition, closeness, foodVision);
		}
		return foodVision;
	}

	private VisionValue updateBulletAndGetVision(NN creature, float delta) {
		VisionValue bulletVision = new VisionValue();

		for(Sprite b : bullets){
			if(creature.collidingWith(b)){
				creature.setSpeed(0);
				toRemoveBullets.add(b);
			}


			// check if bullets is too far and remove it
			Vector2 bulletDistance = new Vector2(b.getX() - (WORLD_WIDTH/2), b.getY() - (WORLD_HEIGHT/2));
			if(bulletDistance.len() > WORLD_WIDTH*3 && !toRemoveBullets.contains(b)) toRemoveBullets.add(b);

			//move the bullet
			//first get the direction the entity is pointed
			Vector2 Bulletdirection = new Vector2();
			Bulletdirection.x = (float) Math.cos(Math.toRadians(b.getRotation()));
			Bulletdirection.y = (float) Math.sin(Math.toRadians(b.getRotation()));

			//normalize the vector
			Bulletdirection = Bulletdirection.nor();

			//Then scale it by the current speed to get the velocity
			Vector2 velocity = new Vector2();
			velocity.x = Bulletdirection.x * BULLET_SPEED/creatures.size();
			velocity.y = Bulletdirection.y * BULLET_SPEED/creatures.size();

			b.setX(b.getX() + (velocity.x * delta));
			b.setY(b.getY() + (velocity.y * delta));


			Vector2 bulletPosition = new Vector2(b.getX()-creature.getX(), b.getY()-creature.getY());
			Vector2 creatureHeadingDirection = creature.getDirection();
			float angleWithBullet = creatureHeadingDirection.angle(bulletPosition);
			float closeness = (VISION_RANGE -bulletPosition.len())/VISION_RANGE;
			updateVisionValue(angleWithBullet, bulletPosition, closeness, bulletVision);
		}
		return bulletVision;
	}

	private VisionValue getCreatureVision(NN creature) {
		VisionValue creatureVision = new VisionValue();
		for(NN c : creatures){
			if(c != creature){
				Vector2 otherPosition = new Vector2(c.getX()-creature.getX(), c.getY()-creature.getY());
				Vector2 creatureHeadingDirection = creature.getDirection();
				float angleWithOther = creatureHeadingDirection.angle(otherPosition);	//angle between the create and the food, from -90 to 0 food is on the right, from 0 to 90 food is on the left
				float closeness = (400f-otherPosition.len())/400f;
				updateVisionValue(angleWithOther, otherPosition, closeness, creatureVision);
			}
		}
		return creatureVision;
	}

}
