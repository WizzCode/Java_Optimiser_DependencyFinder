package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.javaparser.StaticJavaParser.parse;

//This class contains different methods for optimisation.

public class Optimiser  {
    static int flag;
    
    SymbolTableGenerator obj = new SymbolTableGenerator();
    
    //This method is called in the Main class which has the main method and is the entry point of the project
    public void calloptimiser() throws FileNotFoundException, Exception{
        System.out.println("Calling Optimiser");
        obj.symbolsolverparsing();
//        System.out.println(obj.compilationUnit); //Access compilation unit used for parsing
        
        //Different methods are called here 
        avoidMethodCalls();
        AvoidEmptyIfStatement();
        
        avoidBooleanIfComparison();
    }
    
   public void avoidMethodCalls(){
      flag =1;
      System.out.println("This method is used for detecting unnecessary method calls inside loops");
      VoidVisitor<List<Expression>> forStmtVisitor = new ForStmtVisitor();
      VoidVisitor<List<Expression>> whileStmtVisitor = new WhileStmtVisitor();
      List <Expression> collector = new ArrayList<>();
      forStmtVisitor.visit(obj.compilationUnit,collector);
      whileStmtVisitor.visit(obj.compilationUnit,collector);
      for(int i=0;i<collector.size();i++){
          
          try
    {
            Expression leftvar = collector.get(i).asBinaryExpr().getLeft();
            Expression rightvar = collector.get(i).asBinaryExpr().getRight();
            if((leftvar instanceof MethodCallExpr)||(rightvar instanceof MethodCallExpr))
            {
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                System.out.println("Avoid method calls in loop");
              
            }
            else if(leftvar instanceof BinaryExpr){
            ArrayList<Node> subExprListLeft = new ArrayList<>(leftvar.getChildNodes());
            
            for(int j =0;j<subExprListLeft.size();j++){
               
                if(subExprListLeft.get(j) instanceof MethodCallExpr){
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                System.out.println("Avoid method calls in loop");
                }
            }
    }
            else if(rightvar instanceof BinaryExpr){
            ArrayList<Node> subExprListRight = new ArrayList<>(rightvar.getChildNodes());
            for(int j =0;j<subExprListRight.size();j++){
               
                if(subExprListRight.get(j) instanceof MethodCallExpr){
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                System.out.println("Avoid method calls in loop");
                }
            }
            } 
        
    }
    catch (IllegalStateException e)
    {

    }
      }
      
      System.out.println("--------------");

   }

   public void AvoidEmptyIfStatement() throws IOException {
       
      System.out.println("This method is used for detecting empty if blocks");
      flag = 2;
      VoidVisitor<List<Expression>> ifStmtVisitor = new IfStmtVisitor();
      List <Expression> collector = new ArrayList<>();
      ifStmtVisitor.visit(obj.compilationUnit,collector);
//      System.out.print(obj.compilationUnit);

   }

    public void avoidBooleanIfComparison() {
        flag = 3;
        System.out.println("This method is used for detecting unnecessary boolean comparison");
        // check the datatype of the variable in the Expression (condition inside if) and then check
        // if its being compared to true or false
        IfStmtVisitor v_obj = new IfStmtVisitor();
        List<Expression> collector = new ArrayList<>();
        v_obj.visit(obj.compilationUnit, collector);
//variableType v1 = new variableType();
        for (int i = 0; i < collector.size(); i++) {
            System.out.println(collector.get(i));

            // here u can get stuff on both sides of the and or OR
            List<BinaryExpr> myList = new ArrayList<BinaryExpr>();
            BinaryExprExtract b_obj = new BinaryExprExtract();
            b_obj.visit((BinaryExpr) collector.get(i), myList);
            for (int k = 0; k < myList.size(); k++) {
                helperForOptimizationMethod(myList.get(k));
                // System.out.println("printing from mlist= " + myList.get(k));
            }


        }
    }
    public void helperForOptimizationMethod(BinaryExpr n) {
        Expression leftVariable = n.getLeft();
        // System.out.println(leftVariable.calculateResolvedType().describe());
        String leftvarType = leftVariable.calculateResolvedType().describe().toString();

        String operator = n.getOperator().toString();
        Expression rightVariable = n.getRight();
        // System.out.println(rightVariable.calculateResolvedType().describe());
        String rightvarType = rightVariable.calculateResolvedType().describe().toString();

// now u have to check if operator = "==" and left variable is boolean and right variable is 0
        try
        {
            if (operator == "EQUALS") {
// JUST NEED TO FIND THE CLASS OF THE LEFT VARIABLE
                if ((leftvarType == "boolean") && (rightvarType == "boolean"))// OR DIFFERENT REGEX THAT PEOPLE USE TO CHECK STRING SIZE=0
                {
                    //  System.out.println(collector.get(i));
                    //System.out.println(collector.get(i).getBegin());
                    System.out.println("Avoid using equality with boolean expressions "+n);
                    //condition satisfied
                }

            }

        }

        catch (IllegalStateException e)
        {

        }


        //Other methods of optimisation
        //pass condition of ifStatement to expression statement
    }

   //Other methods of optimisation
    //pass condition of ifStatement to expression statement
}
