package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class WhileStmtVisitor extends VoidVisitorAdapter<List<Expression>>{
    @Override
 public void visit(WhileStmt whileStmt, List<Expression> collector) {
         
       collector.add(whileStmt.getCondition());
        super.visit(whileStmt, collector);
        }
 


}
