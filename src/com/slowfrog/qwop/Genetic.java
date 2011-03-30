package com.slowfrog.qwop;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Genetic {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Genetic g = new Genetic();

  }

  private Map<String, Individual> population;

  public Genetic() {
    this.population = new HashMap<String, Individual>();
    this.readPopulation("runs.txt");
    System.out
        .println("Population: " + this.population.size() + " individuals");
    int totalRuns = 0;
    for (Individual indiv : this.population.values()) {
      totalRuns += indiv.runs.size();
    }
    System.out.println("Total runs: " + totalRuns);

    IFilter<RunInfo> fiveMetersNotCrashed = new AndFilter<RunInfo>(
        new MinDistFilter(5), new NotFilter<RunInfo>(new CrashedFilter()));
    IFilter<Individual> individualFilter = new MinRatioFilter(
        fiveMetersNotCrashed);
    List<Individual> good = this.filter(individualFilter);
    System.out.println("Runners up to 5m and not crashed at least 50%: " + good.size());
  }

  public List<Individual> filter(IFilter<Individual> filter) {
    List<Individual> ret = new ArrayList<Individual>();
    for (Individual individual : this.population.values()) {
      if (filter.matches(individual)) {
        ret.add(individual);
      }
    }
    return ret;
  }

  public void readPopulation(String filename) {
    try {
      BufferedReader input = new BufferedReader(new FileReader(filename));
      try {
        String line;
        int linenum = 0;
        while ((line = input.readLine()) != null) {
          ++linenum;
          if (line.length() > 0) {
            try {
              RunInfo info = RunInfo.unmarshal(line);
              Individual indiv = this.population.get(info.string);
              if (indiv == null) {
                indiv = new Individual(info.string, null);
                this.population.put(info.string, indiv);
              }
              indiv.runs.add(info);
            } catch (RuntimeException e) {
              System.out.println("Error on line " + linenum);
              throw e;
            }
          }
        }

      } finally {
        try {
          input.close();
        } catch (IOException e) {
          throw new RuntimeException("Error closing file: " + filename);
        }
      }

    } catch (FileNotFoundException e) {
      throw new RuntimeException("File not found: " + filename, e);
    } catch (IOException e) {
      throw new RuntimeException("Error reading file: " + filename, e);
    }
  }
}
