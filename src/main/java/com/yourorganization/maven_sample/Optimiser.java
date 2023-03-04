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
        System.out.println(obj.compilationUnit); //Access compilation unit used for parsing
        
        //Different methods are called here 
        avoidMethodCalls();
        AvoidEmptyIfStatement();
    }
    
   public void avoidMethodCalls(){
      System.out.println("This method is used for removing unnecessary method calls inside loops");
   }

   public void AvoidEmptyIfStatement() throws IOException {


       FileInputStream in = new FileInputStream("src/main/resources/OptimiserInput.java");

       CompilationUnit cu;
       try {
           // parse the file
           cu = parse((in));
       } finally {
           in.close();
       }

      // System.out.println(cu.toString());
       VoidVisitor<List<String>> ifStmtVisitor = new IfStmtVisitor();
      List <String> collector = new ArrayList<>();
       ifStmtVisitor.visit(cu,collector);

   }


   
   //Other methods of optimisation
}
//the Visitors are classes which are used to find specific parts of the AST

//In most cases you will want to determine what you are looking for, then define a visitor that will
//operate on it. Using a visitor is very easy to find all nodes of a certain kind, like all the method
//declarations or all the multiplication expressions.

//Once we have a compilation unit to work with we can define our first visitor. Our visitor is going
//to extend a class named VoidVisitorAdaptor.
//Although that sounds like an odd name we can break in down into its constituent parts. The Void
//means we’re not expecting the visit to return anything, i.e. this visitor may produce a side effect,
//but will not operate on the underlying tree. There are other types of visitors that will do this, which
//we will come to later.
//The Adaptor refers to the class being part of an adaptor pattern, which provided default implementations of the VoidVisitor interface, which accepts around 90 different types as arguments (see
//Appendix B for the full list). So when defining your own visitor you will want to override the visit
//method for the type you are interested in. In practice, this means that you can define only the method
//to handle a certain type of nodes (e.g. field declarations), while your visitor will not do anything
//with the dozens of other node types you are not interested in. If you were instead to implement
//VoidVisitor directly you would have to define dozens of methods, one for each node type: not very
//practical.