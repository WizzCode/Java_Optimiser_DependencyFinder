package com.yourorganization.maven_sample;


import java.io.FileNotFoundException;
import java.util.Arrays;


import java.util.*;

public class VariableDependency {
    public ArrayList<String> variable_array=new ArrayList<String>();
    public ArrayList<String> right=new ArrayList<String>();
    public HashMap<String,ArrayList<String>> dependence_dict = new HashMap<String,ArrayList<String>>();
    public String path;

    public VariableDependency(String path) {
        this.path = path;
    }

    public static void main(String[] args) throws FileNotFoundException {

        VariableDependency vdobj = new VariableDependency("src/main/resources/OptimiserInput.java");
        vdobj.vdInput();
        vdobj.variableDependencyAlgo();
        vdobj.variableDependencyMatrix();

        
    }

    public void vdInput() throws FileNotFoundException{
        SymbolTableGenerator obj = new SymbolTableGenerator();

//        obj.parseInputCode("SampleProgramNew.java");
        obj.symbolsolverparsing(this.path);
        System.out.println("CU"+obj.compilationUnit);
        VariableAttributes obj2=  obj.variableDependencyInput();
        System.out.println("list of all variables ");
        for (int i = 0; i < obj2.variable_array.size();i++)
        {
            System.out.println(obj2.variable_array.get(i));
        }

        System.out.println("dependence dict ");
        System.out.println(Arrays.asList(obj2.dependence_dict));

        //obj.parseInputCode("SumOfNumbers1.java");
        
        
            variable_array=obj2.variable_array;
              right=obj2.right;
             dependence_dict=obj2.dependence_dict;


    }

    public void variableDependencyAlgo(){

        int n=variable_array.size();
        HashMap<String,String> variable_status = new HashMap<String,String>();

        System.out.println("\nDetermining Dependencies.......\n");
        System.out.println("Determining Status of Variables.......\n");
        System.out.println("Status: Description\n");
        System.out.println("Isolated: No variables depend on it and it has no interaction with other variables\n");
        System.out.println("Precedence: No variable depends on it but the variable itself depends on some variable\n");
        System.out.println("Intermediate: Has neither isolated nor precedence status\n");
        System.out.println("---------------------------------------------------------------------------------------------\n");
        for(int i=0;i<n;i++){
            String variable=variable_array.get(i);
            String status;
            if(!right.contains(variable)){
                if(!dependence_dict.containsKey(variable)) status = "Isolated";
                else status = "Precedence";
            }
            else status="Intermediate";
            variable_status.put(variable,status);
        }




        System.out.println("-----------------Variable Classification-----------------\n");

        Formatter fmt = new Formatter();
        fmt.format("%25s %25s %31s\n", "Variable\t|", "Dependent on\t|", "Status of Variable");

        for (int i=0;i<n;i++) {
            String variable=variable_array.get(i);

            List<String> dependencies = new ArrayList<String>();
            if(dependence_dict.containsKey(variable)) dependencies= dependence_dict.get(variable);
            String dependent_on;
            if(dependencies.isEmpty()) dependent_on="--";
            else dependent_on = String.join(",",dependencies );

            String status = variable_status.get(variable);


            fmt.format("%24s %23s %27s\n", variable +"\t|", dependent_on +"\t|",status );
        }

        System.out.println(fmt);
    }

    public void variableDependencyMatrix(){
        int noVars = variable_array.size();
        boolean depMatrix[][] = new boolean[noVars][noVars];
        ArrayList<String> graphNodes = new ArrayList<>();
        for (String var: variable_array){
            graphNodes.add(var);
        }
        String parent="", var="";
        if (graphNodes.size()!=0){
            parent = graphNodes.remove(0);
            var = parent;
        }
        while(graphNodes.size()!=0){
            //checking childs of parent
            var = parent;
            ArrayList<String> childs = dependence_dict.get(var);

            if (childs!=null){
                String child="";
                for(String c : childs){
                    if(graphNodes.contains(c)){
                        graphNodes.remove(c);
                        child = c;
                    }
                    depMatrix[variable_array.indexOf(c)][variable_array.indexOf(var)]=true;
                }
                if(child==""){
                    parent = graphNodes.remove(0);
                }else{
                    parent = child;
                }
            }

            //checking parents of var
            else{
                boolean flag = false;
                for (String key: dependence_dict.keySet()){
                    if(dependence_dict.get(key).contains(var)){
                        String child = var;
                        parent = key;
                        if (graphNodes.contains(parent)){
                            graphNodes.remove(parent);
                        }
                        depMatrix[variable_array.indexOf(child)][variable_array.indexOf(parent)]=true;
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    if(graphNodes.contains(var)){
                        graphNodes.remove(var);
                    }
                    parent = graphNodes.remove(0);
                }
            }
        }
        
        System.out.println("Generating Depedency Matrix.......\n");
        System.out.println("If the value of the matrix at a position\nwhere the row denotes variable1 and the column denotes variable2 is 1 \nthen variable2 is dependent on variable1\n");
        System.out.println("----------------------------------Depenedency Matrix---------------------------------------\n");
   
        System.out.printf("%-5s", "");
        for (int i = 0; i < depMatrix[0].length; i++) {
        System.out.printf("%-5s", variable_array.get(i));
        }
        System.out.println();
        for (int i = 0; i < depMatrix.length; i++){
            // Loop through all elements of current row
            System.out.printf("%-5s", variable_array.get(i));
            for (int j = 0; j < depMatrix[i].length; j++){
                if(depMatrix[i][j]==false) System.out.printf("%-5d",0);
                else System.out.printf("%-5d",1);
            }
            System.out.println("\n");
        }
TransitivityMatrix(depMatrix);
    }

    public void TransitivityMatrix(boolean depMatrix[][])
    {
        int dimension = depMatrix.length;
        boolean newMatrix[][] = new boolean [dimension][dimension];
        for (int i = 0; i < newMatrix.length; i++){
            Arrays.fill(newMatrix[i], false);
        }
for(int i=0;i<depMatrix[0].length;i++) // i variable iterates through columns
{
    for(int j=0;j<depMatrix.length;j++) // j variable iterates through rows
    {
if((depMatrix[j][i]==true)) //only performing analysis on dependencyMatrix
{
    newMatrix[j][i]=true; //we need to show the original dependency as well in the transitive dependency
  boolean lenarr[]  =depMatrix[i];
  for(int k=0;k<lenarr.length;k++)
  {
      if(lenarr[k]==true)
      {
          newMatrix[j][k]=true;
      }
  }

}
    }
}

//print new matrix here

        System.out.println("---------Transitive Dependency Matrix-------------");
System.out.println(" ");
        System.out.printf("%-5s", "");
        for (int i = 0; i < newMatrix[0].length; i++) {
            System.out.printf("%-5s", variable_array.get(i));
        }
        System.out.println();
        for (int i = 0; i < newMatrix.length; i++){
            // Loop through all elements of current row
            System.out.printf("%-5s", variable_array.get(i));
            for (int j = 0; j < newMatrix[i].length; j++){
                if(newMatrix[i][j]==false) System.out.printf("%-5d",0);
                else System.out.printf("%-5d",1);
            }
            System.out.println("\n");
        }
    }

}
