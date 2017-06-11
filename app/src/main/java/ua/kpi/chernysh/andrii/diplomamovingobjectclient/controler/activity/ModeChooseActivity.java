package ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ua.kpi.chernysh.andrii.diplomamovingobjectclient.R;

import static ua.kpi.chernysh.andrii.diplomamovingobjectclient.R.id.btnAutoMode;
import static ua.kpi.chernysh.andrii.diplomamovingobjectclient.R.id.btnManualMode;

public class ModeChooseActivity extends AppCompatActivity implements
        View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_choose);

        findViewById(btnManualMode).setOnClickListener(this);
        findViewById(btnAutoMode).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case btnManualMode:
                startActivity(new Intent(this,ManualModeActivity.class));
                break;
            case btnAutoMode:
                startActivity(new Intent(this,AutomaticalModeActivity.class));
                break;
            default:
                Log.d("LOG_TAG","Error while choosing control mode");
        }
    }
}
