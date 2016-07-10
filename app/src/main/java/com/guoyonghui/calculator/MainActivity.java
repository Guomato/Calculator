package com.guoyonghui.calculator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.guoyonghui.calculator.core.ArithmeticExpression;
import com.guoyonghui.calculator.core.InputFSM;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private boolean mFinished = false;

    private InputFSM mFSM;

    private ArithmeticExpression mExpr;

    private BigDecimal mLastResult;

    @BindView(R.id.bt_one)
    Button mOneButton;

    @BindView(R.id.bt_two)
    Button mTwoButton;

    @BindView(R.id.bt_three)
    Button mThreeButton;

    @BindView(R.id.bt_four)
    Button mFourButton;

    @BindView(R.id.bt_five)
    Button mFiveButton;

    @BindView(R.id.bt_six)
    Button mSixButton;

    @BindView(R.id.bt_seven)
    Button mSevenButton;

    @BindView(R.id.bt_eight)
    Button mEightButton;

    @BindView(R.id.bt_nine)
    Button mNineButton;

    @BindView(R.id.bt_zero)
    Button mZeroButton;

    @BindView(R.id.bt_point)
    Button mPointButton;

    @BindView(R.id.bt_add)
    Button mAddButton;

    @BindView(R.id.bt_subtract)
    Button mSubtractButton;

    @BindView(R.id.bt_multiply)
    Button mMultiplyButton;

    @BindView(R.id.bt_divide)
    Button mDivideButton;

    @BindView(R.id.bt_backspace)
    Button mBackspaceButton;

    @BindView(R.id.bt_reset)
    Button mResetButton;

    @BindView(R.id.bt_calculate)
    Button mCalculateButton;

    @BindView(R.id.bt_brackets)
    Button mBracketsButton;

    @BindView(R.id.bt_exponent)
    Button mExponentButton;

    @BindView(R.id.calculate_area)
    TextView mCalculateAreaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFSM = new InputFSM();

        mExpr = new ArithmeticExpression();
    }

    @OnClick({R.id.bt_one, R.id.bt_two, R.id.bt_three, R.id.bt_four, R.id.bt_five, R.id.bt_six, R.id.bt_seven, R.id.bt_eight, R.id.bt_nine, R.id.bt_zero, R.id.bt_point, R.id.bt_add, R.id.bt_subtract, R.id.bt_multiply, R.id.bt_divide, R.id.bt_backspace, R.id.bt_reset, R.id.bt_calculate, R.id.bt_brackets, R.id.bt_exponent})
    public void onClick(Button bt) {
        if(mFinished) {
            processBeforeCalculating(bt == mAddButton || bt == mSubtractButton || bt == mMultiplyButton || bt == mDivideButton);
        }

        if(bt == mBackspaceButton) {
            backspace();
        } else if(bt == mCalculateButton) {
            obtainResult();
        } else if(bt == mResetButton) {
            reset();
        } else {
            input(bt.getText().toString());
        }
    }

    /**
     * 在一次计算结束后下一次计算开始前进行预处理
     * 若上一次计算结果不为空并且用户按键输入为四则运算符,则此时进行连续运算处理
     * 否则进行正常运算处理
     * @param userPressOperator 用户按键输入是否为四则运算符
     */
    private void processBeforeCalculating(boolean userPressOperator) {
        mCalculateAreaTextView.setText("");
        mFSM.clear();
        mExpr.clear();

        if(userPressOperator && mLastResult != null) {
            String lastResultString = (mLastResult.compareTo(BigDecimal.ZERO) == 0) ? "0" : mLastResult.toString();
            mCalculateAreaTextView.append(lastResultString);
            for(int i = 0; i < lastResultString.length(); i++) {
                mFSM.input(lastResultString.charAt(i));
            }
        }

        mFinished = false;
    }

    /**
     * 退格处理
     * 若算式不为空则进行一次退格并且对状态机进行回退处理
     */
    private void backspace() {
        String expr = mCalculateAreaTextView.getText().toString();
        if(expr.length() > 0) {
            mCalculateAreaTextView.setText(expr.substring(0, expr.length() - 1));
            mFSM.backspace();
        }
    }

    /**
     * 获取计算结果
     * 若算式为空则返回
     * 若当前状态机处于可接受状态则进行计算
     * 若计算结果不为空则打印计算结果
     * 否则提示错误信息
     */
    private void obtainResult() {
        String exprString = mCalculateAreaTextView.getText().toString();
        if(exprString.length() == 0) {
            return;
        }

        if(mFSM.accept()) {
            mExpr.clear();
            mExpr.express(exprString);
            BigDecimal result = mExpr.value();
            if(result != null) {
                mCalculateAreaTextView.append("\n=" + result);
                mLastResult = result;
                mFinished = true;
                return;
            }
        }
        Toast.makeText(MainActivity.this, "非法的表达式", Toast.LENGTH_SHORT).show();
    }

    /**
     * 重置
     */
    private void reset() {
        mLastResult = null;
        mFinished = false;
        mFSM.clear();
        mExpr.clear();
        mCalculateAreaTextView.setText("");
    }

    /**
     * 用户输入
     * @param inputString 用户按键输入的字符串
     */
    private void input(String inputString) {
        char ch;
        if(inputString.length() > 1) {
            if(mFSM.canInputLB()) {
                ch = '(';
            } else if(mFSM.canInputRB()) {
                ch = ')';
            } else {
                return;
            }
        } else {
            ch = inputString.charAt(0);
        }

        if(mFSM.input(ch)) {
            mCalculateAreaTextView.append(ch + "");
        }
    }

}
