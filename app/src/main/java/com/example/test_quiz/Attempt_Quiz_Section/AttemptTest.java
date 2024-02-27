package com.example.test_quiz.Attempt_Quiz_Section;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_quiz.Model.MetaQuestion;
import com.example.test_quiz.Model.User;
import com.example.test_quiz.NotificationActivity.NotificationService;
import com.example.test_quiz.R;
import com.example.test_quiz.Model.Question;
import com.example.test_quiz.Model.Test;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class AttemptTest extends AppCompatActivity {
    ArrayList<Question> questions;
    ArrayList<HashSet<String>> answers;

    Toolbar toolbar;
    DiscreteScrollView scrollView;
    LinearLayout indexLayout;
    GridView quesGrid;
    ArrayList<String> list;
    // arrayList;
    int flag_controller = 1;
    long timer;// =((Test) getIntent().getExtras().get("Questions")).getTime()*60*1000;
    popGridAdapter popGrid;
    Button next,prev;
    TextView textView;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String TESTNAME;
    private RadioGroup group;
    private int countPaused = 0;

    private boolean isQuestion = true;

    private Integer rank = 0;

    private Integer heartRate;

    private Integer current_heartRate = 0;
    private final long ONE_MEGABYTE = 1024 * 1024;

    private FirebaseStorage storage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance("https://dialysis-55e67-default-rtdb.firebaseio.com/").getReference();
        storage = FirebaseStorage.getInstance("gs://dialysis-55e67.appspot.com/");
        auth= FirebaseAuth.getInstance();
        setContentView(R.layout.activity_attempt);
        Test t = (Test) getIntent().getExtras().get("Questions");
        rank = t.rank;
        questions = t.getQuestions();
        TESTNAME = (String) getIntent().getExtras().get("TESTNAME");
        isQuestion = t.isQuestion();
        heartRate = t.getHeartrate();
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        answers= new ArrayList<>();
        for(int i = 0; i < questions.size(); i ++){
            answers.add(new HashSet<String>());
        }
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        scrollView = findViewById(R.id.discrete);
        final QuestionAdapter questionAdapter=new QuestionAdapter(questions);
        scrollView.setAdapter(questionAdapter);

        next=findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scrollView.getCurrentItem()==questions.size()-1){
                   showPopUp();
                }else {
                    //setNextPrevButton(scrollView.getCurrentItem() + 1);
                    scrollView.smoothScrollToPosition(scrollView.getCurrentItem() + 1);
                }
            }
        });


        prev=findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scrollView.getCurrentItem()!=0){
                    //setNextPrevButton(scrollView.getCurrentItem()-1);
                    scrollView.smoothScrollToPosition(scrollView.getCurrentItem()-1);
                }
            }
        });

        setNextPrevButton(scrollView.getCurrentItem());
        indexLayout=findViewById(R.id.index_layout);
        indexLayout.setAlpha(.5f);
        quesGrid=findViewById(R.id.pop_grid);
        popGrid=new popGridAdapter(AttemptTest.this);
        quesGrid.setAdapter(popGrid);
        quesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                scrollView.smoothScrollToPosition(i+1);
                slideUp(indexLayout);
            }
        });
        scrollView.addScrollListener(new DiscreteScrollView.ScrollListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {
                setNextPrevButton(newPosition);
            }
        });

        timer=((Test) getIntent().getExtras().get("Questions")).getTime()*60*1000;

    }


    void showPopUp(){
        AlertDialog.Builder builder=new AlertDialog.Builder(AttemptTest.this);
        builder.setMessage("Do you want to submit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                submit();
//                setAlertDialog(answerText);
                dialogStart();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();

    }

    /*submit result to database**/
    void submit(){
        if(isQuestion){
            flag_controller = 0;
            list = new ArrayList<>();
            ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<Boolean>> tempList = new ArrayList<ArrayList<Boolean>>();
            for(int i = 0; i < 3; i ++){
                arrayList.add(new ArrayList<String>());
                tempList.add(new ArrayList<Boolean>());
            }
            arrayList.get(0).add("Know Needles");
            arrayList.get(0).add("Visualize Needles");
            arrayList.get(0).add("Insert Needles to an object");
            arrayList.get(1).add("Know Blood");
            arrayList.get(1).add("Overcome the fear of blood");
            arrayList.get(1).add("Safe Vascular access");
            arrayList.get(2).add("Self-cannulation process");
            arrayList.get(2).add("Tips for caring");
            tempList.get(0).add(false);
            tempList.get(0).add(false);
            tempList.get(0).add(false);
            tempList.get(1).add(false);
            tempList.get(1).add(false);
            tempList.get(1).add(false);
            tempList.get(2).add(false);
            tempList.get(2).add(false);
            for(int i=0;i<answers.size();i++){
//            if(answers[i]!=null&&answers[i].equals(questions.get(i).getAnswer())){
//                score++;
//            }
                for(String option: answers.get(i)){
                    option = option.toLowerCase();
                    if(option.contains("needles")){
                        tempList.get(0).set(0,true);
                        tempList.get(0).set(1,true);
                        tempList.get(0).set(2,true);
                    }
                    if(option.contains("blood")){
                        tempList.get(1).set(0,true);
                    }
                    if(option.contains("dependent") || option.contains("pain")){
                        tempList.get(1).set(1,true);
                    }
                    if(option.contains("infections") || option.contains("injuries")){
                        tempList.get(1).set(2,true);
                    }
                    if(option.contains("unfamiliar") || option.contains("vascular") || option.contains("Medical")){
                        tempList.get(2).set(0,true);
                    }
                    if(option.contains("care")){
                        tempList.get(2).set(1,true);
                    }
                }
            }


            for(int i = 0; i < 3; i ++){
                for(int j = 0; j < tempList.get(i).size(); j ++){
                    if(tempList.get(i).get(j) == true){
                        list.add(arrayList.get(i).get(j));
                    }
                }
            }
            try {
                Map<Integer, String> listMap = new HashMap<>();
                for(Integer i = 0; i < list.size(); i ++){
                    listMap.put(i,list.get(i));
                }
                mDatabase.child("Results").child(auth.getUid()).child("items").setValue(list);
                mDatabase.child("Results").child(auth.getUid()).child("current").setValue(0);
            }catch (Exception e){
                Log.e("Result Update Failed " ,e.getMessage());
            }
        }else{
            Float heart_rate = current_heartRate.floatValue()/heartRate.floatValue();
            if(heart_rate < 1.3){
                mDatabase.child("Results").child(auth.getUid()).child("current").setValue(rank + 1);
            }
        }
    }

    void dialogStart() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(AttemptTest.this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setCancelable(false);
        builderSingle.setNegativeButton("Done!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });
        if (isQuestion) {
            builderSingle.setTitle("Your new fear ladder is");


            String message = "";
            for (String y : list) {
                message += y + "\n";
            }
            builderSingle.setMessage(message);

//            builderSingle.setAdapter(arrayAdapter1, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String strName = arrayAdapter.getItem(which);
//                    AlertDialog.Builder builderInner = new AlertDialog.Builder(AttemptTest.this);
//                    builderInner.setMessage(strName);
//                    builderInner.setCancelable(false);
//                    builderInner.setTitle("Your Selected Course is");
//                    builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
////                        finish();
//                            builderSingle.show();
////                        dialog.dismiss();
//                        }
//                    });
//                    builderInner.show();
//                }
//            });
        }else{
            Float heart_rate = current_heartRate.floatValue()/heartRate.floatValue();
            builderSingle.setMessage("Your current heartrate is "+current_heartRate.toString()+"\nYour ground truth heartrate today is "+heartRate);
            if(heart_rate < 1.3){
                builderSingle.setTitle("Congratulations! You've passed this task!");
            }else{
                builderSingle.setTitle("Hmmm it seems that you need to try again!");
            }
        }
        builderSingle.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(countPaused<=2 && countPaused >=0 && flag_controller == 1)
            startService(new Intent(AttemptTest.this,
                    NotificationService.class));
        countPaused++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(AttemptTest.this, NotificationService.class));
        if(countPaused>2) {
            Toasty.success(AttemptTest.this,"Thank you! Your response has been submitted.",
                    Toasty.LENGTH_SHORT).show();
            countPaused = -1000;
            submit();
            dialogStart();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(new Intent(AttemptTest.this, NotificationService.class));
}

    void setNextPrevButton(int pos){
        if(pos==0){
//            prev.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            prev.setText("");
        }else {
//            prev.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            prev.setText("Previous");
        }
        if(pos==questions.size()-1){
            next.setText("Submit");
//            next.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }else {
            next.setText("Next");
//            next.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onBackPressed() {
        showPopUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.attempt_menu, menu);
        final MenuItem  counter = menu.findItem(R.id.counter);

        new CountDownTimer(timer, 1000) {
            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                long hr=TimeUnit.MILLISECONDS.toHours(millis),mn=(TimeUnit.MILLISECONDS.toMinutes(millis)-
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
                        sc=TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));


                String  hms =format(hr)+":"+format(mn)+":"+format(sc) ;
                counter.setTitle(hms);
                timer = millis;
            }
            String format(long n){
                if(n<10)
                    return "0"+n;
                else return ""+n;
            }

            public void onFinish() {
                submit();
                dialogStart();
            }
        }.start();

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.submit){
            showPopUp();

            return true;
        }else if(id==R.id.info){
            togglePopUp();
        }
        return super.onOptionsItemSelected(item);
    }


    void togglePopUp(){
        if(indexLayout.getVisibility()==View.GONE){
            slideDown(indexLayout);
        }else slideUp(indexLayout);
    }

    class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

        private int itemHeight;
        private ArrayList<Question> data;

        QuestionAdapter(ArrayList<Question> data) {
            this.data = data;
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            Activity context = (Activity) recyclerView.getContext();
            Point windowDimensions = new Point();
            context.getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            itemHeight = Math.round(windowDimensions.y * 0.6f);
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.frag_test, parent, false);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    itemHeight);
            v.setLayoutParams(params);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Question thisQuestion = data.get(position);
            holder.dynamicContainer.removeAllViews();
            holder.lastChecker.clear();
            holder.questionText.setText(thisQuestion.getQuestion());
            int datasize = thisQuestion.getMeta();
            for(int i = 0; i < datasize; i ++){
                MetaQuestion thisMeta = thisQuestion.getOpt(i);
                int strType = thisMeta.getType();
                if(strType <= 10) {
                    TextView metaTitle = new TextView(holder.itemView.getContext());
                    metaTitle.setText(thisMeta.getDescription());
                    metaTitle.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    holder.dynamicContainer.addView(metaTitle);
                }
                if(strType == 0){
                    RadioGroup radioGroup = new RadioGroup(holder.itemView.getContext());
                    holder.lastChecker.put(radioGroup, -1);
                    for(String option: thisMeta.getOpts()){
                        RadioButton radioButton = new RadioButton(holder.itemView.getContext());
                        radioButton.setText(option);
                        // 配置radioButton属性...
                        radioGroup.addView(radioButton);
                        int id = radioButton.getId();
                        if(answers.get(position).contains(option)){
                            radioGroup.check(id);
                            holder.lastChecker.put(radioGroup, id);
                        }
                    }
                    if(holder.lastChecker.get(radioGroup) == -1){
                        radioGroup.clearCheck();
                    }
                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                            final int selectedId = radioGroup.getCheckedRadioButtonId();
                            final int lastId = holder.lastChecker.get(radioGroup);
                            if(lastId > -1){
                                RadioButton lastCheckedRadioButton = radioGroup.findViewById(lastId);
                                answers.get(position).remove(lastCheckedRadioButton.getText().toString());
                            }
                            RadioButton checkedRadioButton = radioGroup.findViewById(checkedId);
                            answers.get(position).add(checkedRadioButton.getText().toString());
                            holder.lastChecker.put(radioGroup, checkedId);
                            popGrid.notifyDataSetChanged();
                        }
                    });
                    holder.dynamicContainer.addView(radioGroup);
                } else if (strType == 1) {
                    for(String option: thisMeta.getOpts()){
                        CheckBox checkBox = new CheckBox(holder.itemView.getContext());
                        checkBox.setText(option);
                        if(answers.get(position).contains(option)){
                            checkBox.setChecked(true);
                        }else{
                            checkBox.setChecked(false);
                        }
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked) {
                                    answers.get(position).add(buttonView.getText().toString());
                                } else {
                                    answers.get(position).remove(buttonView.getText().toString());
                                }
                            }
                        });
                        holder.dynamicContainer.addView(checkBox);
                    }
                } else if(strType == 11) { //Image
                    final ImageView imageView = new ImageView(holder.itemView.getContext());
                    storage.getReference(thisMeta.getDescription()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            DisplayMetrics dm = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(dm);

                            imageView.setMinimumHeight(250);
                            imageView.setMinimumWidth(150);
                            imageView.setMaxHeight(250);
                            imageView.setMaxWidth(150);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageView.setImageBitmap(bm);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                    holder.dynamicContainer.addView(imageView);
                } else if(strType == 12) {
                    final TextView textView = new TextView(holder.itemView.getContext());
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    textView.setText("Your current heartrate is: ");
                    SeekBar seekBar = new SeekBar(holder.itemView.getContext());
                    seekBar.setMin(40);
                    seekBar.setMax(130);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            // Toast.makeText(this, "Your current heartrate is: " + progress, Toast.LENGTH_SHORT).show();
                            textView.setText("Your current heartrate is: " + progress);
                            current_heartRate = progress;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    holder.dynamicContainer.addView(textView);
                    holder.dynamicContainer.addView(seekBar);
                }
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            // private View overlay;
            public TextView questionText;
            public ViewGroup dynamicContainer;

            public HashMap<ViewGroup, Integer> lastChecker;

            ViewHolder(View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.questionTextView);
                dynamicContainer = itemView.findViewById(R.id.dynamic_container);
                lastChecker = new HashMap<>();
            }

//            public void setOverlayColor(@ColorInt int color) {
//                overlay.setBackgroundColor(color);
//            }

//            public void unCheck() {
//
//                int selectedId = radioGroup.getCheckedRadioButtonId();
//                if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton) {
//                    r1.setChecked(true);
//                }
//                else if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton2) {
//                    r2.setChecked(true);
//                }
//                else if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton3) {
//                    r3.setChecked(true);
//                }
//                else if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton4) {
//                    r4.setChecked(true);
//                }
//                else if(radioGroup.getCheckedRadioButtonId() ==R.id.radioButton5) {
//                    r5.setChecked(true);
//                }
//            }
        }
    }

    class popGridAdapter extends BaseAdapter{
        Context mContext;
        popGridAdapter(Context context){
            mContext=context;
        }
        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return questions.size();
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View convertView;
            if(view==null){
                convertView=new Button(mContext);
            }else convertView=view;
            (convertView).setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            ((Button)convertView).setText(""+(i+1));

            (convertView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //setNextPrevButton(i);
                    scrollView.smoothScrollToPosition(i);
                }
            });
            return convertView;
        }
    }

    public void slideUp(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,  // fromYDelta
               -view.getHeight());                // toYDelta
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -view.getHeight(),                 // fromYDelta
                0); // toYDelta
        animate.setDuration(500);
        view.startAnimation(animate);

    }

    @Override
    protected void onDestroy() {
        submit();
        super.onDestroy();
    }

    private static class SeekBarHolder{
        TextView username;
        SeekBar seekBar;
        // ImageView circleImageView;
        // TextView text_time;
    }
}

