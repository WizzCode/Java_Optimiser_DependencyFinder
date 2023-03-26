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
        loopInvariantCodeMotion();
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
    
    public void loopInvariantCodeMotion(){
        System.out.println("This method is used to detect loop invariant code");
        VoidVisitor<List<Statement>> forBodyVisitor = new ForBodyVisitor();
        VoidVisitor<List<Statement>> whileBodyVisitor = new WhileBodyVisitor();
        VoidVisitor<List<List<Expression>>> forVariableVisitor = new ForVariableVisitor();
        VoidVisitor<List<Expression>> whileStmtVisitor = new WhileStmtVisitor();
        
        List <Statement> collector = new ArrayList<>();
        List<List<Expression>> for_loop_variable_collector = new ArrayList<>();
        List<Expression> while_loop_variable_collector = new ArrayList<>();
        List<Node> expressions = new ArrayList<>();
        
        forBodyVisitor.visit(obj.compilationUnit,collector);
        
        int for_loops_count = collector.size();
        whileBodyVisitor.visit(obj.compilationUnit,collector);
       
        forVariableVisitor.visit(obj.compilationUnit,for_loop_variable_collector);
        whileStmtVisitor.visit(obj.compilationUnit,while_loop_variable_collector);
        
        Optimiser obj = new Optimiser();
        List<List<String>> reaching_def=new ArrayList<>();
        HashMap<String,Integer> def_location = new HashMap<>();
        //Initial status of variables, definition is inside loop then 0 else 1
        
        for(int i=0;i<collector.size();i++){
        def_location = new HashMap<>();
        reaching_def = new ArrayList<>();
        expressions = new ArrayList<>();
        System.out.println("------------------");
        System.out.println("Block");
        System.out.println(collector.get(i));
        
        //Getting for loop update variables
       
        if(i<for_loops_count){
        List<Expression> loop_update_list = for_loop_variable_collector.get(i);
        for(int lv = 0;lv<loop_update_list.size();lv++){
            
            if(loop_update_list.get(lv) instanceof AssignExpr){
                 String left = ((AssignExpr)loop_update_list.get(lv)).getTarget().toString(); 
                 def_location.put(left, 0);
            }
            else if(loop_update_list.get(lv) instanceof UnaryExpr){
                    Expression unary = ((UnaryExpr) loop_update_list.get(lv)).getExpression();
                    if(unary instanceof NameExpr) def_location.put(unary.toString(),0);
                }
        }
        }
        
        //Getting while loop condition variables
        else{
            
            Expression while_condition = while_loop_variable_collector.get(i-for_loops_count);
            if(while_condition instanceof BinaryExpr) {
                Expression while_left = ((BinaryExpr) while_condition).getLeft();
                if(while_left instanceof BinaryExpr){
                HashSet<NameExpr> while_variables = new HashSet<>();
                while_variables = obj.getBinaryExprVariables((BinaryExpr)while_left,while_variables);
                           List<NameExpr> variables_list = new ArrayList<NameExpr>(while_variables);
                           for(int l =0;l<while_variables.size();l++) {
                               String binaryexpr_string = variables_list.get(l).toString();
                               def_location.put(binaryexpr_string,0);
                           }
            }
            }
        }
                
        //Analysing Loop statements
        for (Node node : collector.get(i).getChildNodes()) {
            List<String> statement = new ArrayList<>();
            if (node instanceof ExpressionStmt) {
                
                Node expr = node.getChildNodes().get(0);
                
                expressions.add(expr);
                if(expr instanceof AssignExpr){
                         Expression array_index;                         
                         String left = ((AssignExpr)expr).getTarget().toString(); 
                         statement.add(left);
                         def_location.put(left, 1);
                         if(((AssignExpr)expr).getTarget() instanceof ArrayAccessExpr) {
                            Expression array_left = ((AssignExpr)expr).getTarget();
                            ArrayAccessExpr array = (ArrayAccessExpr) array_left;
                            array_index = array.getIndex();
                            //Only considering namexpr for now
                            String right_string = array_index.toString();
                            statement.add(right_string);
                            if(!def_location.containsKey(right_string)) def_location.put(right_string,1);
                         }
                         
                         Expression right = ((AssignExpr)expr).getValue();
                         
                         HashSet<NameExpr> variables = new HashSet<>();
                         if(right instanceof BinaryExpr){
                            
                           variables = obj.getBinaryExprVariables((BinaryExpr)right,variables);
                           List<NameExpr> variables_list = new ArrayList<NameExpr>(variables);
                           
                           for(int l =0;l<variables.size();l++) {
                               
                               //Right binary exp without array
                               String binaryexpr_string = variables_list.get(l).toString();
                               
                               statement.add(binaryexpr_string);
                               if(!def_location.containsKey(binaryexpr_string)) def_location.put(binaryexpr_string,1);
                           
                           }
                         }
                         
                         else if(right instanceof NameExpr){
                             String right_string = right.toString();
                             statement.add(right_string);
                             if(!def_location.containsKey(right_string)) def_location.put(right_string,1);
                         }
                         else if(right instanceof ArrayAccessExpr){
                             ArrayAccessExpr array = (ArrayAccessExpr)right;
                             array_index = array.getIndex();
                             String right_string = right.toString();
                             statement.add(right_string);
                             if(!def_location.containsKey(right_string)) def_location.put(right_string,1);
                         }
                         
                    }   
                else if(expr instanceof UnaryExpr){
                    Expression unary = ((UnaryExpr) expr).getExpression();
                    statement.add(unary.toString());
                    statement.add(unary.toString());
                    if(unary instanceof NameExpr){
                        if(def_location.containsKey(unary.toString())) def_location.replace(unary.toString(),1);
                        else def_location.put(unary.toString(),1);
                        
                    }
                }
                
                
                if(!statement.isEmpty()){
                    reaching_def.add(statement);
                    if(statement.size()==2 && statement.get(0).equals(statement.get(1))){
               def_location.replace(statement.get(0),0);
           }
                  }
            }
            
        }
//        System.out.println(reaching_def);    
//        System.out.println(expressions);
//        System.out.println(def_location);

        //Reaching definition algorithm without nested loops
     
         for(int m =0;m<reaching_def.size();m++){

           List<String> statement_variables = reaching_def.get(m);
           if(statement_variables.size()>1){
           String right_variable = statement_variables.get(0);
           
           
           //Left variables
           int count = 0;
           for(int n =1;n<statement_variables.size();n++){
              String left_variable = statement_variables.get(n);
              
              if(def_location.get(left_variable)== 1) count = count +1;
           }
           if(count == statement_variables.size()-1 && def_location.get(right_variable)==1){
               System.out.println("Statement is loop invariant");
               System.out.println(expressions.get(m).getBegin());
               System.out.println(expressions.get(m));
               
           }
           else{
              def_location.replace(right_variable,0); 
           }
           
         }
           else{
              System.out.println("Statement is loop invariant");
              System.out.println(expressions.get(m).getBegin());
              System.out.println(expressions.get(m)); 
           }
         } 
        
    }
         
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
