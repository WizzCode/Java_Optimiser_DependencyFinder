package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class BinaryExprExtract extends VoidVisitorAdapter<List<BinaryExpr>> {
    @Override
    public void visit(BinaryExpr n, List<BinaryExpr> bicollect)
    {
        super.visit(n,bicollect);
        bicollect.add(n);
    }
}
