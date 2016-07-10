package com.guoyonghui.calculator.core;

import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Stack;

public class ArithmeticExpression {

    private static final String NUMBER_REGULAR_EXPR = "^[+,-]?[0-9]+(.[0-9]+)?(E[+,-]?[0-9]+)?$";    //数字正则表达式

    private ArrayList<String> mInfixExpr;        //算式中缀表达式
    private ArrayList<Object> mPostfixExpr;        //算式后缀表达式

    private Stack<String> mOperatorStack;        //操作符栈
    private Stack<BigDecimal> mOperandStack;    //操作数栈

    public ArithmeticExpression() {
        mInfixExpr = new ArrayList<>();
        mPostfixExpr = new ArrayList<>();

        mOperatorStack = new Stack<>();
        mOperandStack = new Stack<>();
    }

    /**
     * 构建表达式
     */
    public void express(String expr) {
        if (expr == null || expr.length() == 0) {
            throw new IllegalStateException("Expression can not be null or empty");
        }

        obtainInfixExpr(expr);
        obtainPostfixExpr();
    }

    /**
     * 根据后缀表达式计算表达式的值
     */
    public BigDecimal value() {
        try {
            //解析后缀表达式的每一个元素，该方法内所指栈均为操作数栈
            for (Object element : mPostfixExpr) {
                if (element instanceof BigDecimal) {
                    //若当前解析元素为数字则压入操作数栈
                    mOperandStack.push((BigDecimal) element);
                } else {
                    //否则当前解析元素为操作符，则取栈顶的两个元素进行操作符对应的运算，将运算结果压入栈
                    String op = (String) element;
                    if ("p".equals(op) || "m".equals(op)) {
                        //若操作符为正负号，则操作数只有一个
                        mOperandStack.push(calculate(mOperandStack.pop(), null, op));
                    } else {
                        mOperandStack.push(calculate(mOperandStack.pop(), mOperandStack.pop(), op));
                    }
                }
            }
            BigDecimal originValue = mOperandStack.pop().stripTrailingZeros();
            if (originValue.precision() - originValue.scale() <= 8 && originValue.scale() < 0) {
                return originValue.setScale(0, BigDecimal.ROUND_HALF_DOWN);
            } else {
                return originValue;
            }
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * 清除表达式当前状态
     */
    public void clear() {
        mInfixExpr.clear();
        mPostfixExpr.clear();

        mOperatorStack.clear();
        mOperandStack.clear();
    }

    /**
     * 根据原表达式构建中缀表达式
     */
    private void obtainInfixExpr(String expr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if (isDigit(ch) || ch == '.' || ch == 'E') {
                builder.append(ch);
            } else {
                if (ch == '+' || ch == '-') {
                    if(i > 0 && expr.charAt(i - 1) =='E') {
                        builder.append(ch);
                        continue;
                    }
                }
                if (builder.length() > 0) {
                    mInfixExpr.add(builder.toString());
                    builder.setLength(0);
                }
                if (ch == '+' || ch == '-') {
                    //操作符为+/-时，若上一个解析的符号存在且不为左括号也不为数字时，则此时+/-代表正负号而非加减符号
                    if (mInfixExpr.size() == 0 || !(")".equals(mInfixExpr.get(mInfixExpr.size() - 1)) || mInfixExpr.get(mInfixExpr.size() - 1).matches(NUMBER_REGULAR_EXPR))) {
                        ch = (ch == '+') ? 'p' : ch;
                        ch = (ch == '-') ? 'm' : ch;
                    }
                }

                builder.append(ch);
                mInfixExpr.add(builder.toString());
                builder.setLength(0);
            }
        }
        if (builder.length() > 0) {
            mInfixExpr.add(builder.toString());
            builder.setLength(0);
        }
        Log.d("Test", "infix expression: " + mInfixExpr);
    }

    /**
     * 根据中缀表达式构建后缀表达式
     */
    private void obtainPostfixExpr() {
        //解析中缀表达式的每一个元素，该方法内所指栈均为操作符栈
        for (String element : mInfixExpr) {
            if (element.matches(NUMBER_REGULAR_EXPR)) {
                //若解析元素为数字则直接加入后缀表达式
                mPostfixExpr.add(BigDecimal.valueOf(Double.parseDouble(element)));
            } else if ("(".equals(element)) {
                //若解析元素为左括号则直接压入栈
                mOperatorStack.push(element);
            } else if (")".equals(element)) {
                //若解析元素为右括号则依次弹出栈顶元素并将其加入后缀表达式，直至栈顶元素为右括号为止，并将右括号从栈中弹出
                while (!"(".equals(mOperatorStack.peek())) {
                    mPostfixExpr.add(mOperatorStack.pop());
                }
                mOperatorStack.pop();
            } else {
                //若解析元素为操作符，若栈顶元素为左括号则直接将操作符压入栈
                //若栈顶元素不为左括号，则若当前解析的操作符的优先级不大于栈顶的操作符的优先级，
                //则依次弹出栈顶元素，直至栈为空或栈顶元素为左括号或栈顶元素的优先级小于当前解析元素的优先级为止
                while (!mOperatorStack.empty() && !"(".equals(mOperatorStack.peek()) && priority(mOperatorStack.peek()) >= priority(element)) {
                    mPostfixExpr.add(mOperatorStack.pop());
                }
                mOperatorStack.push(element);
            }
        }
        //解析完毕后若栈不为空则依次弹出栈中所有元素并将其加入后缀表达式
        while (!mOperatorStack.empty()) {
            mPostfixExpr.add(mOperatorStack.pop());
        }
        Log.d("Test", "post expression: " + mPostfixExpr);
    }

    /**
     * 获取操作符的优先级
     */
    private int priority(String op) {
        switch (op) {
            case "+":
            case "-":
                return 1;
            case "×":
            case "÷":
                return 2;
            case "m":
            case "p":
                return 3;

            default:
                return -1;
        }
    }

    /**
     * 判断字符是否为数字
     */
    private boolean isDigit(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    /**
     * 根据两个操作数和操作符计算二元算式的值
     */
    private BigDecimal calculate(BigDecimal rightOperand, BigDecimal leftOperand, String op) {
        switch (op) {
            case "+":
                return leftOperand.add(rightOperand);
            case "p":
                return rightOperand;
            case "-":
                return leftOperand.subtract(rightOperand);
            case "m":
                return rightOperand.multiply(BigDecimal.valueOf(-1));
            case "×":
                return leftOperand.multiply(rightOperand);
            case "÷":
                if (rightOperand.compareTo(BigDecimal.valueOf(0)) == 0) {
                    throw new IllegalStateException("Dividend can not be zero.");
                }
                return leftOperand.divide(rightOperand, 10, BigDecimal.ROUND_HALF_DOWN);
            default:
                return null;
        }
    }

}