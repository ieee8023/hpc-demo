package joe;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;

public class Experiment implements Runnable {

	public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
		
		
		int cores = Runtime.getRuntime().availableProcessors()-1;
		
		cores = 4;
		ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
		
		
		String[] datasets = {
				"datasets/segment.arff",
				"datasets/hypothyroid.arff",
				"datasets/spambase.arff",
				"datasets/ecoli.arff",
		};
		
		List<Instances> instancess = new ArrayList<Instances>();
		
		for (String dataset: datasets){
			instancess.add(new Instances(new BufferedReader(new FileReader(dataset))));
		}
		
		//Another way of selecting a single dataset
		//String dataset = datasets[(int) (Math.random()*datasets.length)];
		
		for (int i : new int[]{1,2,3,4,5})
			for(Instances instances : instancess){
				
				Experiment exp = new Experiment(	
					"Test1",
					instances.relationName(),
					instances,
					new LibSVM(),
					es);
					
					//exp.run();
					es.execute(exp);
			}
		
		
		//wait forever
		es.shutdown();
		es.awaitTermination(9999, TimeUnit.DAYS);
		System.out.println("#######Finished######");
	}
	
	
	String label;
	Classifier evalclassifier;
	ThreadPoolExecutor es;
	String dataset;
	Instances instances;
	
	public Experiment(String label, String dataset, Instances instances,Classifier evalclassifier,ThreadPoolExecutor es) {
		
		super();
		this.label = label;
		this.evalclassifier = evalclassifier;
		this.es = es;
		this.dataset = dataset;
		this.instances  = instances;
	}

	public void run(){
		
		try {
			
			if (instances.classIndex() == -1)
				instances.setClassIndex(instances.numAttributes() - 1);
			System.out.println("Reading from file " + dataset + "," + instances.numInstances() + " Instances, " + instances.numAttributes() + " Attributes");

			Instances data = instances;
			Evaluation evaluation = new Evaluation(data);
			long startTime = System.currentTimeMillis();

			evaluation.crossValidateModel(evalclassifier, data, 5, new Random());
			double f1 = evaluation.weightedFMeasure();
			long timeTaken = (System.currentTimeMillis() - startTime);

			String result = "###, " + label + ", " + smallNum(f1) + ", " + evalclassifier.getClass().getSimpleName() + "," + instances.relationName() + "," + timeTaken;

			System.err.println("+" + result);

			if (es != null)
				System.out.println("+=+=Threads Left= " + es.getQueue().size());

		} catch (Exception e) {
			System.err.println("Experiment Failed");
			String result = "###, " + label + ", Error, " + evalclassifier.getClass().getSimpleName() + ", " + dataset + ", " + e.getClass().getSimpleName() + ", " + e.getMessage();
			System.out.println("+" + result);
			e.printStackTrace();
		}
	}
	
	private String smallNum(double in){
		
		return String.format("%.4f", in);
	}

}
