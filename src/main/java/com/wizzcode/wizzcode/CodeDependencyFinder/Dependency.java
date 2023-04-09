package com.wizzcode.wizzcode.CodeDependencyFinder;

import java.io.FileNotFoundException;
import java.util.*;


import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;


public class Dependency {
    public ArrayList<String> attribute_array=new ArrayList<String>();
    public ArrayList<String> right=new ArrayList<String>();
    public HashMap<String,ArrayList<String>> dependence_dict = new HashMap<String,ArrayList<String>>();
    public String path;
    Map<Integer, Map<String, String>> IdgraphNodes = new LinkedHashMap<>();
    Map<Integer,int[] > depArray = new LinkedHashMap<>();
    
//    public static void main(String[] args) throws FileNotFoundException, Exception {
//
//        Dependency vdobj = new Dependency();
//        vdobj.calldependency();
//        vdobj.dependencyinput();
//        vdobj.attributeDependencyAlgo();
//        vdobj.attributeDependencyMatrix();
//
//    }

    public Dependency(String path) {
        this.path = path;
    }

    public void calldependency() throws FileNotFoundException, Exception{
        System.out.println("Calling Dependency Finder");
        dependencyinput();
        attributeDependencyAlgo();
        attributeDependencyMatrix();
//        for (Map.Entry<Integer, Map<String,String>> entry : IdgraphNodes.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//        System.out.println(IdgraphNodes.size());
//        for (Map.Entry<Integer, int[]> entry : depArray.entrySet()) {
//            System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
//        }
//        System.out.println(depArray.size());
    }

    public void dependencyinput() throws FileNotFoundException, Exception{

        SymbolTableGenerator obj = new SymbolTableGenerator();
        obj.symbolsolverparsing(this.path);
        ProgramAttributes obj2=  obj.findAttributes();
        IdgraphNodes = obj.getGraphNodes();
        attribute_array=obj2.attribute_array;
        right=obj2.right;
        dependence_dict=obj2.dependence_dict;
        
    }

    public void attributeDependencyAlgo() throws Exception{

        int n=attribute_array.size();
        HashMap<String,String> attribute_status = new HashMap<String,String>();

//        System.out.println("\nDetermining Dependencies.......\n");
//        System.out.println("Determining Status of Attributes.......\n");
//        System.out.println("Status: Description\n");
//        System.out.println("Isolated: No attributes depend on it and it has no interaction with other attributes\n");
//        System.out.println("Precedence: No attribute depends on it but the attribute itself depends on some attribute\n");
//        System.out.println("Intermediate: Has neither isolated nor precedence status\n");
        System.out.println("---------------------------------------------------------------------------------------------\n");
        for(int i=0;i<n;i++){
            String attribute=attribute_array.get(i);
            String status;
            if(!right.contains(attribute)){
                if(!dependence_dict.containsKey(attribute)) status = "Isolated";
                else status = "Precedence";
            }
            else status="Intermediate";
            attribute_status.put(attribute,status);
        }




//        System.out.println("-----------------Attributes Classification-----------------\n");
//
//        Formatter fmt = new Formatter();
//        fmt.format("%15s %20s %21s\n", "Attribute\t|", "Dependent on\t|", "Status of Attribute");
//
//        for (int i=0;i<n;i++) {
//            String attribute=attribute_array.get(i);
//
//            List<String> dependencies = new ArrayList<String>();
//            if(dependence_dict.containsKey(attribute)) dependencies= dependence_dict.get(attribute);
//            String dependent_on;
//            if(dependencies.isEmpty()) dependent_on="--";
//            else dependent_on = String.join(",",dependencies );
//
//            String status = attribute_status.get(attribute);
//
//
//            fmt.format("%14s %13s %17s\n", attribute +"\t|", dependent_on +"\t|",status );
//        }
//
//        System.out.println(fmt);
    }

    public void attributeDependencyMatrix() throws Exception{
        int noAttributes = attribute_array.size();
        int depMatrix[][] = new int[noAttributes][noAttributes];
        ArrayList<String> graphNodes = new ArrayList<>();
        for (String attribute: attribute_array){
            graphNodes.add(attribute);
        }

        String parent="";
        for(int i=0;i<graphNodes.size();i++){
            parent = graphNodes.get(i);
            if(dependence_dict.containsKey(parent)){
                ArrayList<String> childs = dependence_dict.get(parent);
                for(String child: childs){
                    depMatrix[attribute_array.indexOf(parent)][attribute_array.indexOf(child)]=1;
                }
            }
        }

        System.out.println("Generating Depedency Matrix.......\n");
        System.out.println("If the value of the matrix at a position\nwhere the row denotes attribute1 and the column denotes attribute2 is 1 \nthen attribute1 is dependent on attribute2\n");
        System.out.println("----------------------------------Depenedency Matrix---------------------------------------\n");

        System.out.printf("%-5s", "");
        for (int i = 0; i < depMatrix[0].length; i++) {
            System.out.printf("%-5s", attribute_array.get(i));
        }
        System.out.println();
        for (int i = 0; i < depMatrix.length; i++){
            // Loop through all elements of current row
            System.out.printf("%-5s", attribute_array.get(i));
            depArray.put(i,depMatrix[i]);
            for (int j = 0; j < depMatrix[i].length; j++){
                System.out.printf("%-5d",depMatrix[i][j]);
            }
            System.out.println("\n");
        }
        
//        TransitivityMatrix(depMatrix);
        exportToExcel(depMatrix);
    }

    public void TransitivityMatrix(int depMatrix[][]) throws Exception
    {
        int dimension = depMatrix.length;
        boolean newMatrix[][] = new boolean [dimension][dimension];
        int newMatrixInt[][] = new int [dimension][dimension];
        for (int i = 0; i < newMatrix.length; i++){
            Arrays.fill(newMatrix[i], false);
            Arrays.fill(newMatrixInt[i], 0);
        }
        for(int i=0;i<depMatrix[0].length;i++) // i attribute iterates through columns
        {
            for(int j=0;j<depMatrix.length;j++) // j attribute iterates through rows
            {
                if((depMatrix[j][i]==1)) //only performing analysis on dependencyMatrix
                {
                    newMatrix[j][i]=true; //we need to show the original dependency as well in the transitive dependency
                    newMatrixInt[j][i]=1;
                    int lenarr[]  =depMatrix[i];
                    for(int k=0;k<lenarr.length;k++)
                    {
                        if(lenarr[k]==1)
                        {
                            newMatrix[j][k]=true;
                            newMatrixInt[j][k]=1;
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
            System.out.printf("%-5s", attribute_array.get(i));
        }
        System.out.println();
        for (int i = 0; i < newMatrix.length; i++){
            // Loop through all elements of current row
            System.out.printf("%-5s", attribute_array.get(i));
            for (int j = 0; j < newMatrix[i].length; j++){
                if(newMatrix[i][j]==false) System.out.printf("%-5d",0);
                else System.out.printf("%-5d",1);
            }
            System.out.println("\n");
        }
    }

    private void exportToExcel(int matrix[][]) throws Exception{
        Workbook workbook = new Workbook();

        // Obtaining the reference of the worksheet
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        String[] title = new String[] { "Dependency Matrix" };
        cells.importArray(title, 0, 0, true);
        cells.importArrayList(attribute_array,0,1, false);
        cells.importArrayList(attribute_array,1,0, true);
        cells.importArray(matrix, 1, 1);
        workbook.save("src/main/resources/output.xlsx");
    }
    
    public Map<Integer, Map<String, String>> getNodes() {
        return IdgraphNodes;
    }

    public Map<Integer, int[]> getDepArray() {
        return depArray;
    }

}