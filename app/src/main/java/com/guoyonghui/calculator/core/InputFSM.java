package com.guoyonghui.calculator.core;

import android.util.Log;

import java.util.Stack;

public class InputFSM {

    private int status; //状态机当前状态

    private Stack<Integer> statusStack; //状态机历史状态栈

    private Stack<Character> bracketStack;  //未匹配的左括号栈

    public InputFSM() {
        clear();
    }

    /**
     * 初始化有穷状态机
     */
    public void clear() {
        status = 0;
        statusStack = new Stack<>();
        bracketStack = new Stack<>();
    }

    /**
     * 输入以使状态机进行状态转移
     * @param ch 输入的字符
     * @return 输入的字符是否被接受
     */
    public boolean input(char ch){
        if(ch == '(') {
            //当输入的字符为左括号时直接将其压入栈
            bracketStack.push(ch);
        } else if(ch == ')') {
            //当输入的字符为右括号时
            //若括号栈不为空则删除栈顶元素以进行括号匹配
            //若括号栈为空则拒绝当前输入的字符
            if(!bracketStack.isEmpty()) {
                bracketStack.pop();
            } else {
                return false;
            }
        }

        boolean acceptSingle = true;

        //记录状态转移前状态机的状态
        int temp = status;

        //根据当前状态以及输入确定状态机的转移或拒绝当前的输入
        switch (status) {
            case 0:
                if (isDigit(ch)) {
                    status = 2;
                } else if (isSign(ch)) {
                    status = 1;
                } else if (isLB(ch)) {
                    status = 5;
                } else {
                    acceptSingle = false;
                }
                break;
            case 1:
                if(isDigit(ch)) {
                    status = 2;
                } else if(isLB(ch)) {
                    status = 5;
                } else {
                    acceptSingle = false;
                }
                break;
            case 2:
                if (isDigit(ch)) {
                    status = 2;
                } else if (isPoint(ch)) {
                    status = 3;
                } else if (isRB(ch)) {
                    status = 6;
                } else if (isOperator(ch)) {
                    if (ch == '÷') {
                        status = 8;
                    } else {
                        status = 7;
                    }
                } else if(isExponent(ch)) {
                    status = 12;
                } else {
                    acceptSingle = false;
                }
                break;
            case 3:
                if (isDigit(ch)) {
                    status = 4;
                } else {
                    acceptSingle = false;
                }
                break;
            case 4:
                if (isDigit(ch)) {
                    status = 4;
                } else if (isRB(ch)) {
                    status = 6;
                } else if (isOperator(ch)) {
                    if (ch == '÷') {
                        status = 8;
                    } else {
                        status = 7;
                    }
                } else if(isExponent(ch)) {
                    status = 12;
                } else {
                    acceptSingle = false;
                }
                break;
            case 5:
                if (isLB(ch)) {
                    status = 5;
                } else if (isSign(ch)) {
                    status = 1;
                } else if (isDigit(ch)) {
                    status = 2;
                } else {
                    acceptSingle = false;
                }
                break;
            case 6:
                if (isRB(ch)) {
                    status = 6;
                } else if (isOperator(ch)) {
                    if (ch == '÷') {
                        status = 8;
                    } else {
                        status = 7;
                    }
                } else {
                    acceptSingle = false;
                }
                break;
            case 7:
                if(isDigit(ch)) {
                    status = 2;
                } else if(isLB(ch)) {
                    status = 5;
                } else {
                    acceptSingle = false;
                }
                break;
            case 8:
                if (isLB(ch)) {
                    status = 5;
                } else if(isDigit(ch)) {
                    if(ch == '0') {
                        status = 9;
                    } else {
                        status = 2;
                    }
                } else {
                    acceptSingle = false;
                }
                break;
            case 9:
                if(isPoint(ch)) {
                    status = 10;
                } else {
                    acceptSingle = false;
                }
                break;
            case 10:
                if(isDigit(ch)) {
                    if (ch == '0') {
                        status = 10;
                    } else {
                        status = 11;
                    }
                } else {
                    acceptSingle = false;
                }
                break;
            case 11:
                if(isDigit(ch)) {
                    status = 11;
                } else if(isRB(ch)) {
                    status = 6;
                } else if (isOperator(ch)) {
                    if (ch == '÷') {
                        status = 8;
                    } else {
                        status = 7;
                    }
                } else if(isExponent(ch)) {
                    status = 12;
                } else {
                    acceptSingle = false;
                }
                break;
            case 12:
                if(isSign(ch)) {
                    status = 13;
                } else if(isDigit(ch)){
                    status = 14;
                } else {
                    acceptSingle = false;
                }
                break;
            case 13:
                if(isDigit(ch)) {
                    status = 14;
                } else{
                    acceptSingle = false;
                }
                break;
            case 14:
                if(isDigit(ch)) {
                    status = 14;
                } else if(isRB(ch)) {
                    status = 6;
                } else if (isOperator(ch)) {
                    if (ch == '÷') {
                        status = 8;
                    } else {
                        status = 7;
                    }
                } else {
                    acceptSingle = false;
                }
                break;

            default:
                break;
        }

        Log.d("Test", "Input: " + status + "\t" + acceptSingle);

        //若当前输入被接受则将状态转移前的状态压入栈
        if(acceptSingle) {
            statusStack.push(temp);
        }

        return acceptSingle;
    }

    /**
     * 回退至状态机的上一个状态
     */
    public void backspace() {
        if(status == 5) {
            //若当前状态为5则说明上一个输入的信号为左括号,因此需要弹出括号栈的栈顶元素
            bracketStack.pop();
        } else if(status == 6) {
            //若当前状态为6则说明上一个输入的信号为右括号,因此需要向括号栈中压入左括号
            bracketStack.push('(');
        }

        status = statusStack.pop();

        Log.d("Test", "Backspace: " + bracketStack);
    }

    /**
     * 判断状态机当前状态是否为可接受状态
     * 可接受的条件为:状态机处于可接受的状态并且括号栈为空
     * @return 状态机是否为可接受状态
     */
    public boolean accept() {
        return (status == 2 || status == 4 || status == 6 || status == 11 || status == 14) && bracketStack.isEmpty();
    }

    public boolean canInputLB() {
        return status == 0 || status == 1 || status == 5 || status == 7 || status == 8;
    }

    public boolean canInputRB() {
        return status == 2 || status == 4 || status == 6 || status == 8 || status == 11;
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '×' || ch == '÷';
    }

    private boolean isSign(char ch) {
        return ch == '+' || ch == '-';
    }

    private boolean isLB(char ch) {
        return ch == '(';
    }

    private boolean isRB(char ch) {
        return ch == ')';
    }

    private boolean isPoint(char ch) {
        return ch == '.';
    }

    private boolean isExponent(char ch) {
        return ch == 'E';
    }

}
