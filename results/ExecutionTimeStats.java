import java.util.*;
public class ExecutionTimeStats {
    public static void main(String[] args) {

  List<Double> beforeExecution = new ArrayList<Double>();
  List<Double> afterExecution = new ArrayList<Double>();
      for(int i=0;i<30000;i++)
          {
    
            long startTime1= System.nanoTime();
            before_opti();
            long endTime1 = System.nanoTime();
            long elapsedTime1 = endTime1-startTime1;
            beforeExecution.add((double)elapsedTime1);
            System.out.flush();
        
            
            long startTime2= System.nanoTime();
            after_opti();
            long endTime2 = System.nanoTime();
            long elapsedTime2 = endTime2-startTime2;
            afterExecution.add((double)elapsedTime2);
            System.out.flush();
  
          }
          
          OptionalDouble average_b = beforeExecution
          .stream()
          .mapToDouble(a -> a)
          .average();  
          
          OptionalDouble average_a= afterExecution
          .stream()
          .mapToDouble(a -> a)
          .average(); 
          System.out.println("Average before: " + average_b);
          System.out.println("Average after: " + average_a);
          double standardDeviation_b = calculateStandardDeviation(beforeExecution);
        System.out.println("Standard Deviation before: " + standardDeviation_b);

        double standardDeviation_a = calculateStandardDeviation(afterExecution);
        System.out.println("Standard Deviation after: " + standardDeviation_a);
    }

    private static double calculateStandardDeviation(List<Double> dataList) {
      double mean = calculateMean(dataList);
      double sumSquaredDiff = 0.0;
      for (double data : dataList) {
          double diff = data - mean;
          sumSquaredDiff += diff * diff;
      }
      double variance = sumSquaredDiff / dataList.size();
      return Math.sqrt(variance);
  }

  private static double calculateMean(List<Double> dataList) {
      double sum = 0.0;
      for (double data : dataList) {
          sum += data;
      }
      return sum / dataList.size();
  }
  
   
  
  
      public static void before_opti()
      {
         //Code before optimisation
        
      }
  
  
    
    public static void after_opti()
    {
        //Code after optimisation   
   
    }
  
  }


  