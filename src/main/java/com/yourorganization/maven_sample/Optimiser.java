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
      System.out.println("This method is used for removing unnecessary method calls inside loops");
   }

   public void AvoidEmptyIfStatement() throws IOException {
       
      System.out.println("This method is used for detecting empty if blocks");

      VoidVisitor<List<Expression>> ifStmtVisitor = new IfStmtVisitor();
      List <Expression> collector = new ArrayList<>();
      ifStmtVisitor.visit(obj.compilationUnit,collector);
      System.out.print(obj.compilationUnit);

   }

   public void avoidBooleanIfComparison()
   {
       // check the datatype of the variable in the Expression (condition inside if) and then check
       // if its being compared to true or false
       IfStmtVisitor v_obj = new IfStmtVisitor();
       List <Expression> collector = new ArrayList<>();
v_obj.visit(obj.compilationUnit,collector);
//variableType v1 = new variableType();
for(int i=0;i<collector.size();i++)
{
    System.out.println(collector.get(i));
   // System.out.println(collector.get(i).calculateResolvedType());
    try
    {
        Expression leftVariable = collector.get(i).asBinaryExpr().getLeft();
        // System.out.println(leftVariable.calculateResolvedType().describe());
        String leftvarType = leftVariable.calculateResolvedType().describe().toString();

        String operator = collector.get(i).asBinaryExpr().getOperator().toString();
        Expression rightVariable = collector.get(i).asBinaryExpr().getRight();
        //System.out.println(rightVariable.calculateResolvedType().describe());
        String rightvarType = rightVariable.calculateResolvedType().describe().toString();
        System.out.println(operator);
// now u have to check if operator = "==" and left variable is boolean and right variable is 0
        if(operator== "EQUALS")
        {
// JUST NEED TO FIND THE CLASS OF THE LEFT VARIABLE
            if((leftvarType=="boolean")&&(rightvarType=="boolean"))// OR DIFFERENT REGEX THAT PEOPLE USE TO CHECK STRING SIZE=0
            {
                System.out.println("Avoid using equality with boolean expressions");
                //condition satisfied
            }
        }
    }
    catch (IllegalStateException e)
    {

    }

    //System.out.println("condition "+i+" = "+collector.get(i).calculateResolvedType());
}
   }


   //Other methods of optimisation
    //pass condition of ifStatement to expression statement
}
