package anthonyyaghi.geneticalgorithm.agents;

import java.util.ArrayList;

public interface Agent extends Comparable<Agent> {
//    TODO start using this interface instead of the NN class
    double getScore();
    void setScore(double score);
    ArrayList<Double> generateDNA();
    void applyDNA(ArrayList<Double> dna);
}
