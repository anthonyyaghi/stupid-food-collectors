package anthonyyaghi.geneticalgorithm.agents;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class NN implements Comparable<NN> {
    public static final float FREEZE_DURATION = 3.0f;
    public static final float FIRE_INTERVAL = 1.0f;
    private ArrayList<double[][]> w;        //Array holding the weights
    private ArrayList<double[]> b;            //Array holding the biases
    private ArrayList<double[]> a;            //Array holding the output of each layer
    private int[] struct;                    //Array holding the size of each layer
    private Sprite body;
    private float speed, mainSpeed, deltaFreeze, lastFire;
    private float angularVelocity;
    private int score, nb_bullets;
    private boolean wantsToFire;


    public NN(int[] struct, float worldWidth, float worldHeight, float speed, float angularVelocity) {
        this.struct = struct.clone();

        Random random = new Random();
        Texture bodyTexture = new Texture("img/bluePlayer.png");
        body = new Sprite(bodyTexture);
        body.setScale(0.5f);
        body.setOrigin(body.getWidth() / 2, body.getHeight() / 2);
        body.setX(random.nextFloat() * worldWidth);
        body.setY(random.nextFloat() * worldHeight);
        body.setRotation(random.nextFloat() * 360);

        w = new ArrayList<double[][]>();
        b = new ArrayList<double[]>();
        a = new ArrayList<double[]>();

        for (int i = 0; i < struct.length; i++) {
            initBias(struct[i]);
            initOut(struct[i]);
            if (i > 0) initWeights(struct[i - 1], struct[i]);
        }

        this.speed = speed;
        this.mainSpeed = speed;
        deltaFreeze = 0;
        this.angularVelocity = angularVelocity;
        score = 0;
        nb_bullets = 5;
        lastFire = 0.0f;
        wantsToFire = false;
    }


    public void feedForward(double[] input) {
        if (input.length != struct[0]) {
            System.out.println("Wrong input format!!");
        } else {
            a.set(0, input);
            for (int i = 0; i < w.size(); i++) {
                double[] tempOut = new double[w.get(i).length];
                for (int j = 0; j < w.get(i).length; j++) {
                    tempOut[j] = activateNeuron(i + 1, j);
                }
                a.set(i + 1, tempOut);
            }
        }
    }

    public double[] getOutput() {
        return a.get(a.size() - 1);
    }

    public void drawCreature(SpriteBatch batch) {
        body.draw(batch);
    }

    public void moveForward(float delta) {
        //first get the direction the entity is pointed
        Vector2 direction = new Vector2();
        direction.x = (float) Math.cos(Math.toRadians(body.getRotation()));
        direction.y = (float) Math.sin(Math.toRadians(body.getRotation()));

        //normalize the vector
        direction = direction.nor();

        //Then scale it by the current speed to get the velocity
        Vector2 velocity = new Vector2();
        velocity.x = direction.x * speed;
        velocity.y = direction.y * speed;


        body.setX(body.getX() + (velocity.x * delta));
        body.setY(body.getY() + (velocity.y * delta));
    }

    public void rotateLeft(float momentum) {
        body.rotate(momentum * angularVelocity);
    }

    public void rotateRight(float momentum) {
        body.rotate(-momentum * angularVelocity);
    }

    public float getRotation() {
        return body.getRotation();
    }

    public void setScore(int s) {
        score = s;
    }

    public int getScore() {
        return score;
    }

    public Vector2 getDirection() {
        Vector2 direction = new Vector2();
        direction.x = (float) Math.cos(Math.toRadians(body.getRotation()));
        direction.y = (float) Math.sin(Math.toRadians(body.getRotation()));
        return direction.nor();
    }

    public float getX() {
        return body.getX();
    }

    public float getY() {
        return body.getY();
    }

    public boolean collidingWith(Sprite food) {
        return body.getBoundingRectangle().overlaps(food.getBoundingRectangle());
    }

    public ArrayList<Double> generateDNA() {
        ArrayList<Double> toReturn = new ArrayList<Double>();

        // encode the weights
        for (double[][] d : w) {
            for (double[] n : d) {
                for (double a : n) {
                    toReturn.add(a);
                }
            }
        }

        // encode the biases
        for (double[] layer : b) {
            for (double biase : layer) {
                toReturn.add(biase);
            }
        }

        return toReturn;
    }

    public void applyDNA(ArrayList<Double> dna) {
        int i = 0;
        for (double[][] layer : w) {
            for (double[] neuron : layer) {
                for (int z = 0; z < neuron.length; z++) {
                    neuron[z] = dna.get(i);
                    i++;
                }
            }
        }

        for (double[] layer : b) {
            for (int z = 0; z < layer.length; z++) {
                layer[z] = dna.get(i);
                i++;
            }
        }
    }


    public void setSprite(Texture newTexture) {
        body.setTexture(newTexture);
    }


    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void restoreSpeed() {
        speed = mainSpeed;
    }

    public void processFreeze(float d) {
        if (speed == 0) {
            deltaFreeze += d;
            if (deltaFreeze >= FREEZE_DURATION) {
                restoreSpeed();
                deltaFreeze = 0;
            }
        }
        if (lastFire < FIRE_INTERVAL) {
            lastFire += d;
        }
    }

    public boolean canFire() {
        if (lastFire >= FIRE_INTERVAL && nb_bullets > 0) {
            lastFire = 0.0f;
            nb_bullets--;
            return true;
        } else {
            return false;
        }
    }

    public boolean wantsToFire() {
        return wantsToFire;
    }


    //Helper methods
    private double activateNeuron(int x, int y) {
        double z = 0;
        double[] ins = a.get(x - 1);
        double[] ws = w.get(x - 1)[y];
        for (int i = 0; i < ins.length; i++) {
            z += ins[i] * ws[i];
        }
        z += b.get(x)[y];
        return sigmoid(z);
    }

    private void initBias(int size) {
        Random rnd = new Random();

        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rnd.nextGaussian();
        }
        b.add(arr);
    }

    private void initWeights(int psize, int size) {
        Random rnd = new Random();

        double[][] arr = new double[size][psize];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < psize; j++) {
                arr[i][j] = rnd.nextGaussian();
            }
        }
        w.add(arr);
    }

    private void initOut(int size) {
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = 0;
        }
        a.add(arr);
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public void printNet() {
        System.out.println("Weights:");
        for (double[][] a : w) {
            for (double[] x : a) {
                System.out.print("[");
                for (double cw : x) {
                    System.out.print(cw + " ");
                }
                System.out.print("]\t\t");
            }
            System.out.println();
        }

        System.out.println("\n\nBiases:");
        for (double[] a : b) {
            for (double x : a) {
                System.out.print(x + "\t");
            }
            System.out.println();
        }


        System.out.println("\n\nOutputs:");
        for (double[] i : a) {
            for (double x : i) {
                System.out.print(x + "\t");
            }
            System.out.println();
        }
    }

    public void act(float delta) {
        double[] decision = getOutput();
        if(decision[0] >= 0.5d){
            rotateLeft((float) (delta*decision[0]));
        }

        if(decision[1] >= 0.5d){
            moveForward((float) (delta * decision[1]));
        }

        if(decision[2] >= 0.5d){
            rotateRight((float) (delta*decision[2]));
        }

        if(decision[3] >= 0.1d){
            if(canFire()){
                wantsToFire = true;
            }
        } else {
            wantsToFire = false;
        }
    }

    @Override
    public int compareTo(NN o) {
        return o.score - score;
    }
}
