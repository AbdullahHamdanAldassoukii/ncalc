/*
 * Copyright 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.calculator.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.view.View;

import com.duy.calculator.R;
import com.duy.calculator.activities.base.AbstractEvaluatorActivity;
import com.duy.calculator.calc.BasicCalculatorActivity;
import com.duy.calculator.evaluator.EvaluateConfig;
import com.duy.calculator.evaluator.MathEvaluator;
import com.duy.calculator.evaluator.thread.Command;
import com.duy.calculator.model.SolveItem;
import com.duy.calculator.tokenizer.Tokenizer;
import com.duy.calculator.utils.ConfigApp;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;

import static com.duy.calculator.R.string.solve;


public class SolveEquationActivity extends AbstractEvaluatorActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {
    private static final String STARTED = SolveEquationActivity.class.getName() + "started";

    protected SharedPreferences preferences;
    private boolean isDataNull = true;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.solve_equation);
        mHint1.setHint(getString(R.string.input_equation));
        btnSolve.setText(solve);
        getIntentData();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isStarted = preferences.getBoolean(STARTED, false);
        if ((!isStarted) || ConfigApp.DEBUG) {
            if (isDataNull) mInputFormula.setText("2x^2 + 3x + 1");
            clickHelp();
        }
    }

    @Override
    public void clickHelp() {
        final SharedPreferences.Editor editor = preferences.edit();
        TapTarget target0 = TapTarget.forView(mInputFormula,
                getString(R.string.input_equation),
                getString(R.string.input_equation_here))
                .drawShadow(true)
                .cancelable(true)
                .targetCircleColor(R.color.colorAccent)
                .transparentTarget(true)
                .outerCircleColor(R.color.colorPrimary)
                .dimColor(R.color.colorPrimaryDark).targetRadius(70);

        TapTarget target = TapTarget.forView(btnSolve,
                getString(R.string.solve_equation),
                getString(R.string.push_solve_button))
                .drawShadow(true)
                .cancelable(true)
                .targetCircleColor(R.color.colorAccent)
                .transparentTarget(true)
                .outerCircleColor(R.color.colorPrimary)
                .dimColor(R.color.colorPrimaryDark).targetRadius(70);

        TapTargetSequence sequence = new TapTargetSequence(SolveEquationActivity.this);
        sequence.targets(target0, target);
        sequence.listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                editor.putBoolean(STARTED, true);
                editor.apply();
                clickEvaluate();
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                clickEvaluate();
            }
        });
        sequence.start();
    }

    /**
     * get data from activity start it
     */
    private void getIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(BasicCalculatorActivity.DATA);
        if (bundle != null) {
            String data = bundle.getString(BasicCalculatorActivity.DATA);
            if (data != null) {
                mInputFormula.setText(data);
                data = new Tokenizer().getNormalExpression(data);
                isDataNull = false;
                if (!data.isEmpty()) {
                    clickEvaluate();
                } else {
                    //
                }
            }
        }
    }

    @Override
    protected String getExpression() {
        String expr = mInputFormula.getCleanText();
        SolveItem solveItem = new SolveItem(expr);
        return solveItem.getInput();
    }

    @Override
    public Command<ArrayList<String>, String> getCommand() {
        return new Command<ArrayList<String>, String>() {
            @Override
            public ArrayList<String> execute(String input) {
                EvaluateConfig config = EvaluateConfig.loadFromSetting(getApplicationContext());
                String fraction = MathEvaluator.newInstance().solveEquation(input,
                        config.setEvalMode(EvaluateConfig.FRACTION), SolveEquationActivity.this);

                String decimal = MathEvaluator.newInstance().solveEquation(input,
                        config.setEvalMode(EvaluateConfig.DECIMAL),  SolveEquationActivity.this);

                ArrayList<String> result = new ArrayList<>();
                result.add(fraction);
                result.add(decimal);
                return result;
            }
        };
    }

}
