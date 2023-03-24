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
import com.github.javaparser.ast.stmt.SynchronizedStmt;
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
import com.github.javaparser.ast.stmt.Statement;

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
        System.out.println("--------------");
        AvoidEmptyIfStatement();   
        System.out.println("--------------");
        avoidBooleanIfComparison();
        System.out.println("--------------");
        catchPrimitivesInConstructor();
        System.out.println("--------------");
        loopInvariantCodeMotion();
        System.out.println("--------------");
        avoidSynchronizedInLoop();
        System.out.println("--------------");
    }
    
    public void loopInvariantCodeMotion(){

        VoidVisitor<List<Statement>> forBodyVisitor = new ForBodyVisitor();
        VoidVisitor<List<Statement>> whileBodyVisitor = new WhileBodyVisitor();
        List <Statement> collector = new ArrayList<>();
        forBodyVisitor.visit(obj.compilationUnit,collector);
        whileBodyVisitor.visit(obj.compilationUnit,collector);
        for(int i=0;i<collector.size();i++) System.out.println(collector.get(i));
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
            if(collector.get(i) instanceof MethodCallExpr){
             System.out.println("\nMethod call detected! Avoid method calls in loop");
             System.out.println(collector.get(i).getBegin());
             System.out.println(collector.get(i));
    }
            Expression leftvar = collector.get(i).asBinaryExpr().getLeft();
            Expression rightvar = collector.get(i).asBinaryExpr().getRight();
           
            if((leftvar instanceof MethodCallExpr)||(rightvar instanceof MethodCallExpr))
            {
                
                System.out.println("\nMethod call detected! Avoid method calls in loop");
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                
              
            }
            else if(leftvar instanceof BinaryExpr){
            ArrayList<Node> subExprListLeft = new ArrayList<>(leftvar.getChildNodes());
            
            for(int j =0;j<subExprListLeft.size();j++){
               
                if(subExprListLeft.get(j) instanceof MethodCallExpr){
                
                System.out.println("\nMethod call detected! Avoid method calls in loop");
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                
                }
            }
    }
            else if(rightvar instanceof BinaryExpr){
            ArrayList<Node> subExprListRight = new ArrayList<>(rightvar.getChildNodes());
            for(int j =0;j<subExprListRight.size();j++){
               
                if(subExprListRight.get(j) instanceof MethodCallExpr){
                System.out.println("\nMethod call detected! Avoid method calls in loop");
                System.out.println(collector.get(i).getBegin());
                System.out.println(collector.get(i));
                
                }
            }
            } 
        
    }
    catch (IllegalStateException e)
    {

    }
      }
      
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
//            System.out.println(collector.get(i));

            // here u can get stuff on both sides of the and or OR
            List<BinaryExpr> myList = new ArrayList<BinaryExpr>();
            BinaryExprExtract b_obj = new BinaryExprExtract();
            try
            {
                b_obj.visit((BinaryExpr) collector.get(i), myList);
            }
            catch (ClassCastException e)
            {
Node e1 = collector.get(i).getChildNodes().get(0);
                while ( e1.getChildNodes().get(0) instanceof EnclosedExpr)
                {
                    //System.out.println(e1);
                 e1 = e1.getChildNodes().get(0);
                }
                    b_obj.visit((BinaryExpr) e1.getChildNodes().get(0),myList);
            }

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
                   
                    System.out.println("\nAvoid using equality with boolean expressions ");
                    System.out.println(n.getBegin());
                    System.out.println(n);
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

    public void catchPrimitivesInConstructor() throws NoSuchMethodException
    {
        System.out.println("------------");
        System.out.println("\nThis method is used to catch un-necessary declarations of primitives in respective constructors");
        List<ObjectCreationExpr> objectcreationList = new ArrayList<ObjectCreationExpr>();
        getCalledMethods g_obj = new getCalledMethods();
        g_obj.visit(obj.compilationUnit,objectcreationList);
        for(int i=0;i<objectcreationList.size();i++)
        {
            Node e =  objectcreationList.get(i).getChildNodes().get(1);

            String typeMethod = objectcreationList.get(i).getChildNodes().get(0).toString();// type of the method

           String typeObject = e.getClass().getSimpleName();// type of the object passed to method
        
        if(((typeMethod.equals("Integer"))&&(typeObject.equals("IntegerLiteralExpr")))||((typeMethod.equals("Character"))&&(typeObject.equals("CharLiteralExpr"))))
            {
                System.out.println("\nAvoid passing primitives to constructors");
                System.out.println(objectcreationList.get(i).getBegin());
                System.out.println(objectcreationList.get(i));
            }
        }
    }

    public void avoidSynchronizedInLoop()
    {
        //forflag =2;
        List<Statement> forStmtCollect = new ArrayList<>();
        List<Statement> whileStmtCollect = new ArrayList<>();
        ForBodyVisitor f_obj = new ForBodyVisitor();
        f_obj.visit(obj.compilationUnit,forStmtCollect);
        WhileBodyVisitor w_obj = new WhileBodyVisitor();
        w_obj.visit(obj.compilationUnit,whileStmtCollect);
        System.out.println("This method is used to detect synchronized statements inside Loops");
//SynchronizedVisit svisit = new SynchronizedVisit();

        for(int i=0;i<forStmtCollect.size();i++)
        {
            System.out.println("for stmt encountered at line "+forStmtCollect.get(i).getBegin()+" and ends at "+forStmtCollect.get(i).getEnd());
            //System.out.println(forStmtCollect.get(i));
            List <SynchronizedStmt> synchStatements;
            synchStatements = forStmtCollect.get(i).findAll(SynchronizedStmt.class);
            //System.out.println("Synchronized list size "+synchStatements.size());
         if(synchStatements.size()>0)
         {
             for(int j=0;j<synchStatements.size();j++)
             {
                 System.out.println("Synch statement encountered at "+synchStatements.get(i).getBegin());
                 System.out.println("Avoid Synch Statement in for loop!");
             }
         }
            //svisit.visit(forStmtCollect.get(i).getBody(),synchStatements);
        }

        for(int i=0;i<whileStmtCollect.size();i++)
        {
            System.out.println("while stmt encountered at line "+whileStmtCollect.get(i).getBegin()+" and ends at "+whileStmtCollect.get(i).getEnd());
            //System.out.println(forStmtCollect.get(i));
            List <SynchronizedStmt> synchStatements;
            synchStatements = whileStmtCollect.get(i).findAll(SynchronizedStmt.class);
            //System.out.println("Synchronized list size "+synchStatements.size());
            if(synchStatements.size()>0)
            {
                for(int j=0;j<synchStatements.size();j++)
                {
                    System.out.println("Synch statement encountered at "+synchStatements.get(i).getBegin());
                    System.out.println("Avoid Synch Statement in while loop!");
                }
            }
            //svisit.visit(forStmtCollect.get(i).getBody(),synchStatements);
        }
    }
   //Other methods of optimisation
    //pass condition of ifStatement to expression statement
}
