package com.wizzcode.wizzcode.Optimisation;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;
import java.util.ArrayList;
import java.util.HashMap;

public class IfStmtVisitor extends VoidVisitorAdapter<List<Expression>>{
    int flag;
    List<List<String>> optimisations = new ArrayList<>();
    List<String> opti;
    HashMap<String,String> justifications = new HashMap<>();
    
    @Override
 public void visit(IfStmt ifStmt, List<Expression> collector) {
         

        Optimiser obj = new Optimiser("");
        flag = obj.flag;
//        optimisations = obj.optimisations;
        opti = obj.opti;
       //System.out.println("reached here");
        if(flag==2){
             Statement then = ifStmt.getThenStmt();
            
             if (then instanceof BlockStmt) {
            BlockStmt blockThenStmt = then.asBlockStmt();
             
          if(blockThenStmt.getStatements().size()==0)
          {
              System.out.println("\nEmpty if encountered! Avoid empty if");
              System.out.println(ifStmt.getBegin());
              System.out.println(ifStmt);
              opti = new ArrayList<>();
              opti.add(ifStmt.toString());
              opti.add(ifStmt.getBegin().get().toString());
              opti.add("empty_if");
              optimisations.add(opti); 
                  
              
          }
                  
        }
        }
        
        collector.add(ifStmt.getCondition());
        //System.out.println(collector.size());
        super.visit(ifStmt, collector);
//blockStmtVisit b_obj = new blockStmtVisit();
//b_obj.visit(ifStmt,null);


        
        
         }

    public List<List<String>> getOptimisations() {
        return optimisations;
    }
}
