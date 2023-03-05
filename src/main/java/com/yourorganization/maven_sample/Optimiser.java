package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    }
    
   public void avoidMethodCalls(){
      System.out.println("This method is used for removing unnecessary method calls inside loops");
   }

   public void AvoidEmptyIfStatement() throws IOException {
       
      System.out.println("This method is used for detecting empty if blocks");

      VoidVisitor<List<String>> ifStmtVisitor = new IfStmtVisitor();
      List <String> collector = new ArrayList<>();
      ifStmtVisitor.visit(obj.compilationUnit,collector);
      

   }

   //Other methods of optimisation
}
