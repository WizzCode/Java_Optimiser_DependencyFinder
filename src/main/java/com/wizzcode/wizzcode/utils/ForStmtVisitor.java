package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class ForStmtVisitor extends VoidVisitorAdapter<List<Expression>>{
    @Override
 public void visit(ForStmt forStmt, List<Expression> collector) {
      
        collector.add(forStmt.getCompare().get());
        super.visit(forStmt, collector);
        }
 


}
