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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
        avoidStringConcatenationInLoop();
        System.out.println("--------------");
        avoidSynchronizedInLoop();
        System.out.println("--------------");
        avoidStringTokenizer();
        System.out.println("--------------");
        avoidNewWithString();
        System.out.println("--------------");
    }
    
    public void avoidStringConcatenationInLoop(){
        System.out.println("This method is used to detect concatenation of strings inside loops");
        VoidVisitor<List<Statement>> forBodyVisitor = new ForBodyVisitor();
        VoidVisitor<List<Statement>> whileBodyVisitor = new WhileBodyVisitor();
        VoidVisitor<List<Statement>> ifBodyVisitor = new IfBodyVisitor();
        List <Statement> collector = new ArrayList<>();
        List <Statement> ifcollector = new ArrayList<>();
        forBodyVisitor.visit(obj.compilationUnit,collector);
        whileBodyVisitor.visit(obj.compilationUnit,collector);
        ifBodyVisitor.visit(obj.compilationUnit,ifcollector);
        for(int i=0;i<collector.size();i++){
            List <ExpressionStmt> exprStatements = collector.get(i).findAll(ExpressionStmt.class);
             
             for(int j=0;j<exprStatements.size();j++){
                 Node expr = exprStatements.get(j).getChildNodes().get(0);
                    
                    if(expr instanceof AssignExpr){
                         Expression left = ((AssignExpr)expr).getTarget();  
                         Expression right = ((AssignExpr)expr).getValue();
                         String datatype = left.calculateResolvedType().describe().toString();
                         if(datatype.equalsIgnoreCase("java.lang.String") && right instanceof BinaryExpr){
                            System.out.println("Avoid string concatenation in loops.");
                            System.out.println(expr.getBegin());
                            System.out.println(expr+"\n");
                            
                         }
                    }
             }
             
        }
    }
    
    public HashSet<NameExpr> getBinaryExprVariables(BinaryExpr binaryExpr,HashSet<NameExpr> variables){
    
    binaryExpr.ifBinaryExpr(binary -> {
    Expression left = binary.getLeft();
    Expression right = binary.getRight();
    left.ifNameExpr(variables::add);

    if(left instanceof BinaryExpr) getBinaryExprVariables((BinaryExpr)left,variables);
    right.ifNameExpr(variables::add);
    if(right instanceof BinaryExpr) getBinaryExprVariables((BinaryExpr)right,variables);
});
    return variables;
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

    public void avoidBooleanIfComparison() throws IOException
    {



        for(BinaryExpr bex : obj.compilationUnit.findAll(BinaryExpr.class))
        {

            if(bex.getLeft() instanceof MethodCallExpr) {
//                Expression mce = bex.getLeft();
//                System.out.println("Trying to calculate type from "+ mce.toString());
//                String type = bex.calculateResolvedType().describe();
//                System.out.println("Type is "+ type);
                if(((bex.getRight().toString().equals("true"))||(bex.getRight().toString().equals("false"))))
                {
                    System.out.println("Binary expression= "+bex.toString()+" left= "+bex.getLeft());

                    System.out.println("avoid evaluating boolean in IfComparsion");
                }
            }
            else {
                if(bex.getLeft().calculateResolvedType().describe().equals("boolean")&&(((bex.getRight().toString().equals("true"))||(bex.getRight().toString().equals("false")))))
                {
                    System.out.println("Binary expression= "+bex.toString()+" left= "+bex.getLeft());

                    System.out.println("avoid evaluating boolean in IfComparsion");
                }
            }

            
        }

    }


    public void avoidDuplicateCode ()
    {
        System.out.println("This Method is used for detecting duplicate code");
        //IfStmtCollector i_obj = new IfStmtCollector();
        List<IfStmt> ifstatements= new ArrayList<>();
        //i_obj.visit(obj.compilationUnit,ifstatements);

        for(int i=0;i<ifstatements.size();i++)
        {
            System.out.println("If Statement body");
// resolve statements later
            //List<Node> thenStmts =   ifstatements.get(i).getThenStmt().getChildNodes().stream().toList();
           // List<Statement> elseStmts = ifstatements.get(i).getElseStmt().stream().toList();

        }

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
           if(objectcreationList.get(i).getChildNodes().size()>1){
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
    }

    public void avoidSynchronizedInLoop()
    {
        //forflag =2;

        // answer to explanation : https://qr.ae/przCDl
        // You should have the loop inside the sync block.
        // This reduces the overhead of changing thread states again and again after each cycle of loop.
        // And the other threads don’t have any other code to complete so they can wait anyway.
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
//            System.out.println("for stmt encountered at line "+forStmtCollect.get(i).getBegin()+" and ends at "+forStmtCollect.get(i).getEnd());
            //System.out.println(forStmtCollect.get(i));
            List <SynchronizedStmt> synchStatements;
            synchStatements = forStmtCollect.get(i).findAll(SynchronizedStmt.class);
            //System.out.println("Synchronized list size "+synchStatements.size());
         if(synchStatements.size()>0)
         {
             for(int j=0;j<synchStatements.size();j++)
             {
                 
                 System.out.println("Avoid Synch Statement in for loop!");
                 System.out.println(synchStatements.get(j).getBegin());
                 System.out.println(synchStatements.get(j)+"\n");
             }
         }
            //svisit.visit(forStmtCollect.get(i).getBody(),synchStatements);
        }

        for(int i=0;i<whileStmtCollect.size();i++)
        {
//            System.out.println("while stmt encountered at line "+whileStmtCollect.get(i).getBegin()+" and ends at "+whileStmtCollect.get(i).getEnd());
            //System.out.println(forStmtCollect.get(i));
            List <SynchronizedStmt> synchStatements;
            synchStatements = whileStmtCollect.get(i).findAll(SynchronizedStmt.class);
            //System.out.println("Synchronized list size "+synchStatements.size());
            if(synchStatements.size()>0)
            {
                for(int j=0;j<synchStatements.size();j++)
                {
                     System.out.println("Avoid Synch Statement in while loop!");
                     System.out.println(synchStatements.get(j).getBegin());
                     System.out.println(synchStatements.get(j)+"\n");
                }
            }
            //svisit.visit(forStmtCollect.get(i).getBody(),synchStatements);
        }
    }
   //Other methods of optimisation
    //pass condition of ifStatement to expression statement

    public void avoidStringTokenizer() throws NoSuchMethodException
    {
        // reference to javadocs which encourages to avoid StringTokenizer: https://docs.oracle.com/javase/6/docs/api/java/util/StringTokenizer.html
        System.out.println("------------");
        System.out.println("\nThis method is used to prevent usage of StringTokenizer");
        List<ObjectCreationExpr> objectcreationList = new ArrayList<ObjectCreationExpr>();
        getCalledMethods g_obj = new getCalledMethods();
        g_obj.visit(obj.compilationUnit,objectcreationList);
        //System.out.println("Called methods");
for(int i=0;i<objectcreationList.size();i++)
{

    if(objectcreationList.get(i).getChildNodes().get(0).toString().equals("StringTokenizer"))
    {
        System.out.println("String Tokenizer Method Encountered on Line "+objectcreationList.get(i).getBegin()+". Avoid Using String Tokenizer. Use split() method instead!");
    }
}
    }
    public void avoidNewWithString() throws NoSuchMethodException
    {
        //add reference also please

        // https://stackoverflow.com/a/8680451
        // this answer points out that duplicate objects are created when using the new keyword, which can
        // take up additional un-necessary space on the heap.
        System.out.println("------------");
        System.out.println("\nThis method is used to prevent usage of New Keyword with string");
        List<ObjectCreationExpr> objectcreationList = new ArrayList<ObjectCreationExpr>();
        getCalledMethods g_obj = new getCalledMethods();
        g_obj.visit(obj.compilationUnit,objectcreationList);
        //System.out.println("Called methods");
        for(int i=0;i<objectcreationList.size();i++)
        {

            if(objectcreationList.get(i).getChildNodes().get(0).toString().equals("String"))
            {
                System.out.println("String Constructor Method Encountered on Line "+objectcreationList.get(i).getBegin()+". Avoid calling String Constructors for Objects that already exist on top of the heap!");
            }
            //System.out.println(objectcreationList.get(i));
        }
    }


}
