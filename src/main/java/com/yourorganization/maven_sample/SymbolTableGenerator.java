package com.yourorganization.maven_sample;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;





import java.util.ArrayList;
import java.util.List;


/**
 * Some code that uses JavaParser.
 */
public class SymbolTableGenerator {
    //    public static void main(String[] args) {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;
    
    public void symbolsolverparsing() throws FileNotFoundException{
        
         
        System.out.println("Entered function");
        combinedTypeSolver = new CombinedTypeSolver();
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        ParseResult<CompilationUnit> parseResultcompilationUnit;
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add((new JavaParserTypeSolver("src/main/java")));
        combinedTypeSolver.add((new JavaParserTypeSolver("src/test/java")));
        symbolSolver = new JavaSymbolSolver(this.combinedTypeSolver);
        parserConfiguration.setSymbolResolver(this.symbolSolver);
        parser = new JavaParser(parserConfiguration);
        FileInputStream in = new FileInputStream("src/main/resources/SampleProgramNew.java");
        parseResultcompilationUnit = this.parser.parse(in);
        compilationUnit = parseResultcompilationUnit.getResult().get();
        
        
    }
    
    
    public void parseInputCode(String filename) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(SymbolTableGenerator.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        cu = sourceRoot.parse("", filename);
    
        Log.info("Compilation Unit generated");

        cu.accept(new ModifierVisitor<Void>() {
            /**
             * For every if-statement, see if it has a comparison using "!=".
             * Change it to "==" and switch the "then" and "else" statements around.
             */
            @Override
            public Visitable visit(IfStmt n, Void arg) {
                // Figure out what to get and what to cast simply by looking at the AST in a debugger! 
                n.getCondition().ifBinaryExpr(binaryExpr -> {
                    if (binaryExpr.getOperator() == BinaryExpr.Operator.NOT_EQUALS && n.getElseStmt().isPresent()) {
                        /* It's a good idea to clone nodes that you move around.
                            JavaParser (or you) might get confused about who their parent is!
                        */
                        Statement thenStmt = n.getThenStmt().clone();
                        Statement elseStmt = n.getElseStmt().get().clone();
                        n.setThenStmt(elseStmt);
                        n.setElseStmt(thenStmt);
                        binaryExpr.setOperator(BinaryExpr.Operator.EQUALS);
                    }
                });
                return super.visit(n, arg);
            }
        }, null);

    }

    public VariableAttributes variableDependencyInput() {
       System.out.println(compilationUnit);
        VariableAttributes obj1= new VariableAttributes();
        for (TypeDeclaration typeDec : compilationUnit.getTypes()) {
            List<BodyDeclaration> members = typeDec.getMembers();
            if (members != null) {
                for (BodyDeclaration member : members) {
                    if (member.isMethodDeclaration()) {
                        MethodDeclaration function = (MethodDeclaration) member;
                        System.out.println("Method name: " + function.getNameAsString());

                    }
                }
            }
        }
        compilationUnit.findAll(VariableDeclarator.class).forEach(variable -> {
            String rightSide;
            System.out.println("Name " + variable.getNameAsString());
            obj1.variable_array.add(variable.getNameAsString());//adding to variable array


            System.out.println("Type " + variable.getTypeAsString());
//            System.out.println("Right side "+variable.getInitializer());
            if (variable.getInitializer().isPresent()) {
                rightSide = variable.getInitializer().get().toString();
                ArrayList<String> dependents = new ArrayList<String>();
                for (String val: rightSide.split(" "))
                {
                    if (isStringOnlyAlphabet(val))
                    {
                        if(!dependents.contains(val))
                        {
                            dependents.add(val);
                        }
                        if(!obj1.right.add(val))
                        {
                            obj1.right.add(val);
                        }
                       // this could add the same variable appearing twice ,
                        //two times
                    }
                }
                if(!dependents.isEmpty()) obj1.dependence_dict.put(variable.getNameAsString(),dependents);
                    // printing the final value.;

                System.out.println("Right side " + rightSide); // a
                System.out.println("Initialiser Type " + variable.getInitializer().get().getClass().getName());
            }
        });
        // This saves all the files we just read to an output directory.  
//        sourceRoot.saveAll(
//                // The path of the Maven module/project which contains the LogicPositivizer class.
//                CodeGenerationUtils.mavenModuleRoot(SymbolTableGenerator.class)
//                        // appended with a path to "output"
//                        .resolve(Paths.get("output")));

      return obj1;
    }
    
    public void attributes(){

        System.out.println("-------------------------------");
        
        for (ClassOrInterfaceDeclaration cd : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            System.out.println("Class "+cd.getNameAsString()); 
            System.out.println("Class Variables");
            for(FieldDeclaration ff:cd.getFields())
    {
        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+ff.getVariable(0).getName());
    }
    
    for (MethodDeclaration method : cd.getMethods()) {
        System.out.println("Method");
        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()); 
        System.out.println("Method Variables");
        method.getBody().ifPresent(blockStatement -> {
                    
            for( VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                
                System.out.println("Declared Variable: "+variable.getNameAsString());
                if(variable.getInitializer().isPresent()) System.out.println("Initialiser: "+variable.getInitializer().get());
                for (NameExpr nameExp : blockStatement.findAll(NameExpr.class)) {
                    if (nameExp.getNameAsString().equals(variable.getNameAsString())) {
                            
                        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()+"."+nameExp.getNameAsString());
                        Node parentNode = nameExp.getParentNode().get();
                        System.out.println("Variable used in Expression: "+parentNode);
                        System.out.println("Expression type: "+parentNode.getClass());
                        if (parentNode instanceof AssignExpr) {
                            
                            Expression left = ((AssignExpr)parentNode).getTarget();  
                            Expression right = ((AssignExpr)parentNode).getValue();
                            System.out.println("LHS: "+left); 
                            System.out.println("RHS: "+right); 
                        }
                                           
                    }
                    
                   
                }
                
               
                
            }
        });
    }
}
        System.out.println("-------------------------------");
        
        
//        for (NameExpr nameExpr : compilationUnit.findAll(NameExpr.class)) {
// try{
//    ResolvedValueDeclaration calledVariable = nameExpr.resolve();
//   
//    String variableName = calledVariable.getName();
//    String variableType = calledVariable.getType().describe();
//    System.out.println(variableName);        
//    System.out.println(variableType); 
//    String type = "Read";
//
//    Node parentNode = nameExpr.getParentNode().get();
//    System.out.println(parentNode); 
//    System.out.println(parentNode.getClass());
//    System.out.println(parentNode instanceof AssignExpr); 
//    if (parentNode instanceof AssignExpr) {
//                    
//        Expression target = ((AssignExpr)parentNode).getTarget();     
//        //Note that arrayAccessExp is not covered in this code
//        if (target.isNameExpr()) {
//            if (target.asNameExpr().getNameAsString().equals(variableName)) 
//                    type = "Write";
//        }
//    }
//    System.out.println(type); 
//    }
//    
//     catch (UnsolvedSymbolException e){
//            System.out.println("Expression cannot be resolved");
//        }
//}
    
//        for (TypeDeclaration typeDec : compilationUnit.getTypes()) {
//        List<BodyDeclaration> members = typeDec.getMembers(); 
//        if (members != null) {
//            for (BodyDeclaration member : members) {
//               
//                if (member.isMethodDeclaration()) {
//                    MethodDeclaration field = (MethodDeclaration) member;
//                    System.out.println("Method name: " + field.getNameAsString());
//
//                    
//                    Stream<String> stream= field.getBody().get().getStatements().stream().map(Node::toString);
//                    List<String> list = new ArrayList<>();
//                    list = stream.collect(Collectors.toList());
//                    for(int i=0;i<list.size();i++){
//                        
//                        if(list.get(i).contains("=") || list.get(i).indexOf(" ") == list.get(i).lastIndexOf(" ")){
//                            
//                             String statements[] = list.get(i).split("=");
//                             String left = statements[0];
//                             String right="";
//                             if(statements.length>1) right = statements[1];
//                                  
//                            
//                             System.out.println("left "+left);
//                             System.out.println("right "+right);
//                        }
//                        
//                       
//                        
//                    }
//
//                    
//                }
//            }
//        }
//    }
        
       
        
         
    }
    
    public static boolean isStringOnlyAlphabet(String str)
    {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}
