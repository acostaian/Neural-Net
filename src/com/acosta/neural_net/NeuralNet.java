package com.acosta.neural_net;

import com.acosta.neural_net.perceptron.Perceptron;
import com.acosta.neural_net.perceptron.Node;

import java.util.ArrayList;
import java.util.Random;

public class NeuralNet {

    // ATTRIBUTES
    private ArrayList<ArrayList<Node>> layers;
    private int iterations;

    private double[] netTInputs;
    private double[] netTOutputs;
    private double[] outputs;


    // CONSTRUCTOR
    public NeuralNet (int[] layers) {
        this.layers = new ArrayList<>();
        this.iterations = 0;

        netTInputs = null;
        netTOutputs = null;
        outputs = null;

        initLayers(layers);
    }


    // GETTERS
    public int getIterations() {
        return iterations;
    }


    // SETTERS
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setInputs (double[] inputs) {
        this.netTInputs = inputs;

        for (Node perceptron : layers.get(0)) { // FOREACH FIRST LAYER PERCEPTRON
            double[] newInputs = new double[0];
            for (int i = 0; i < inputs.length;i++) {
                for (String inputName : perceptron.getInputNodes()) { // FOREACH INPUT NODE
                    if (inputName.equals(Integer.toString(i))) {
                        double[] auxInputs = new double[newInputs.length + 1];
                        for (int j = 0; j < newInputs.length; j++) {
                            auxInputs[j] = newInputs[j];
                        }
                        auxInputs[newInputs.length] = inputs[i];
                        newInputs = auxInputs;
                    }
                }
            }
            perceptron.setTInputs(newInputs);
        }
    }

    public void setOutputs (double[] newOutputs) {
        this.netTOutputs = newOutputs;

        // FOREACH OUTPUT
        for (int i = 0; i < newOutputs.length; i++) {
            // FOREACH LAST LAYER PERCEPTRON
            for (Node perceptron : layers.get(layers.size() - 1)) {
                // FOREACH OUTPUT NODE
                for (String outputName : perceptron.getOutputNodes()) {
                    if (outputName.equals(Integer.toString(i)))
                        perceptron.setTOutput(newOutputs[i]);
                }
            }
        }
    }


    // METHODS
    private void initLayers (int[] layers) {
        for (int i = 0; i < layers.length; i++) {
            this.layers.add(new ArrayList<>());
            for (int j = 0; j < layers[i]; j++) {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append(i);
                strBuilder.append(j);
                this.layers.get(i).add(new Node(strBuilder.toString(), 0.5));
            }
        }
    }

    public void train (double[][] newInputs, double[][] newOutputs) {
        Random rand = new Random();

        if (newInputs.length == newOutputs.length) {
            for (int i = 0; i < iterations; i++) {
                //Train a random set of inputs
                int number = rand.nextInt(newInputs.length);

                setInputs(newInputs[number]);
                setOutputs(newOutputs[number]);

                outputs = think();

                // CALCULATE ERROR
                double totalError = 0;
                for (int j = 0; j < layers.get(layers.size() - 1).size(); j++) {
                    Node lastLayerPerceptron = layers.get(layers.size() - 1).get(j);

                    double error = lastLayerPerceptron.getTOutput() - outputs[j];
                    totalError += error;

                    lastLayerPerceptron.setPartialError(error);
                }

                backpropagate();
            }
        }
    }

    public double[] think () {
        // FOREACH LAYER
        for (int i = 0; i < layers.size(); i++) {
            outputs = new double[layers.get(i).size()];
            // FOREACH PERCEPTRON
            for (int j = 0; j < layers.get(i).size(); j++) {
                outputs[j] = layers.get(i).get(j).think();

                for (String outputName : layers.get(i).get(j).getOutputNodes()) {
                    Node outputNode = getNode(outputName);

                    if (outputNode != null) {
                        double[] auxInputs;

                        if (outputNode.getTInputs() == null)
                            auxInputs = new double[outputNode.getInputNodes().size()];
                        else
                            auxInputs = outputNode.getTInputs();

                        auxInputs[outputNode.getInputNodes().indexOf(layers.get(i).get(j).getName())] = outputs[j];

                        outputNode.setTInputs(auxInputs);
                    }
                }
            }
        }

        return outputs;
    }

    public double[] think (double[] newInputs) {
        setInputs(newInputs);
        // FOREACH LAYER
        for (int i = 0; i < layers.size(); i++) {
            outputs = new double[layers.get(i).size()];
            // FOREACH PERCEPTRON
            for (int j = 0; j < layers.get(i).size(); j++) {
                outputs[j] = layers.get(i).get(j).think();

                for (String outputName : layers.get(i).get(j).getOutputNodes()) {
                    Node outputNode = getNode(outputName);

                    if (outputNode != null) {
                        double[] auxInputs;

                        if (outputNode.getTInputs() == null)
                            auxInputs = new double[outputNode.getInputNodes().size()];
                        else
                            auxInputs = outputNode.getTInputs();

                        auxInputs[outputNode.getInputNodes().indexOf(layers.get(i).get(j).getName())] = outputs[j];

                        outputNode.setTInputs(auxInputs);
                    }
                }
            }
        }
        return outputs;
    }

    public double[][] think (double[][] newInputs) {
        double[][] finalOutputs = new double[newInputs.length][layers.get(layers.size() -1).size()];

        for (int i = 0; i < newInputs.length; i++) {
            finalOutputs[i] = think(newInputs[i]);
        }

        return finalOutputs;
    }

    public void connect (String giver, String taker) {
        if (getNode(giver) != null)
            getNode(giver).setOutputNode(taker);
        if (getNode(taker) != null)
            getNode(taker).setInputNode(giver);
    }

    private Node getNode (String name) {
        Node result = null;

        for (ArrayList<Node> layer : layers) {
            for (Node perceptron : layer) {
                if (perceptron.getName().equals(name))
                    result = perceptron;
            }
        }

        return result;
    }

    private void backpropagate () {
        // FOREACH LAYER
        for (int i = layers.size() - 2; i >= 0; i--) {
            // FOREACH PERCEPTRON
            for (int j = 0; j < layers.get(i).size(); j++) {
                Node perceptron = layers.get(i).get(j);

                // Calculates partial error
                double partialError = 0;

                // Foreach perceptron in the next layer
                for (int k = 0; k < layers.get(i + 1).size(); k++) {
                    Node nextLayerPerceptron = layers.get(i + 1).get(k);

                    double nextLayerError = nextLayerPerceptron.getPartialError();

                    partialError += nextLayerError * Perceptron.sigmoidDerivative(nextLayerPerceptron.getOutput()) * nextLayerPerceptron.getWeights()[nextLayerPerceptron.getInputNodes().indexOf(perceptron.getName())];
                }

                perceptron.setPartialError(partialError);
            }
        }
        adjustAll();
    }

    private void adjustAll() {
        for (ArrayList<Node> layer : layers) {
            for (Node perceptron : layer) {
                perceptron.adjustWeights(perceptron.getPartialError());
                perceptron.adjustBiasWeights(perceptron.getPartialError());
            }
        }
    }

}