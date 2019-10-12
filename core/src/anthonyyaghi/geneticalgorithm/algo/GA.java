package anthonyyaghi.geneticalgorithm.algo;

import anthonyyaghi.geneticalgorithm.agents.Agent;
import anthonyyaghi.geneticalgorithm.agents.AgentConfig;
import anthonyyaghi.geneticalgorithm.agents.NN;
import anthonyyaghi.geneticalgorithm.utils.GameConfig;
import com.badlogic.gdx.graphics.Texture;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GA {
    public static void saveDNA(List<Double> dna, String path) {
        try{
            FileOutputStream fos= new FileOutputStream(path);
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(dna);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }


    Random rand;
    Texture normalTexture;
    Texture topTexture;
    double survivalRate;
    double mutationRate;

    public GA(Texture normalText, Texture topText, double survivalRate, double mutationRate) {
        this.rand = new Random();
        this.normalTexture = normalText;
        this.topTexture = topText;
        this.survivalRate = survivalRate;
        this.mutationRate = mutationRate;
    }

    public int evolve(List<NN> population, int atbScore) {
        Collections.sort(population);
        int bestScore = population.get(0).getScore();

        if (bestScore < atbScore){
            bestScore = atbScore;
        }

        //discard weak individuals
        int popSize = population.size();
        int toEliminate = (int) (population.size() * (1 - survivalRate));

        for (int i = 0; i < toEliminate; i++){
            population.remove(population.size()-1);
        }
        ArrayList<NN> newCreatures = new ArrayList<NN>();

        while(population.size() + newCreatures.size() < popSize){
            for(int i = 0; i < population.size(); i++){
                //For each individual choose a random partner
                int index = rand.nextInt(population.size());
                if(index == i) index = (index+1)%population.size();
                //get both parents dna
                ArrayList<Double> dna1 = population.get(i).generateDNA();
                ArrayList<Double> dna2 = population.get(index).generateDNA();
                //Apply crossover and mutation
                ArrayList<Double> newDNA = new ArrayList<Double>();
                crossOver(dna1, dna2, newDNA);

                //create the new individual
                NN baby = new NN(AgentConfig.NEURAL_NET_STRUCT, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT,
                        AgentConfig.MOVE_SPEED, AgentConfig.ROT_SPEED);
                baby.applyDNA(newDNA);
                newCreatures.add(baby);
            }
        }

        population.addAll(newCreatures);
        //reset the scores in the new generation
        for(NN c : population) c.setScore(0);

        return bestScore;
    }

    private void crossOver(ArrayList<Double> dna1, ArrayList<Double> dna2, ArrayList<Double> newDNA) {
        for(int j = 0; j < dna1.size(); j++){
            double pMutation = rand.nextDouble();
            if(pMutation < mutationRate){
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
    }
}
