package com.example.test_quiz.Attempt_Quiz_Section;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.test_quiz.Model.MetaQuestion;
import com.example.test_quiz.NotificationActivity.NotificationService;
import com.example.test_quiz.R;
import com.example.test_quiz.Model.Question;
import com.example.test_quiz.Model.Test;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Tests extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private ListView listView;
    private TestAdapter testAdapter;
    private int lastPos = -1;

//    private String client_id="23RS47";
//
//    private String authorization = "Basic MjNSUzQ3OjRkYTUzNzg3MGEwY2FjNzk3NGFlMWU3N2M5YjA0Njlm";

    //private Integer heartrate;

    ArrayList<Test> tests=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        avLoadingIndicatorView = findViewById(R.id.loader1);
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
        avLoadingIndicatorView.show();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);
        database = FirebaseDatabase.getInstance("https://dialysis-55e67-default-rtdb.firebaseio.com/");

        myRef=database.getReference();
        listView=findViewById(R.id.test_listview);
        testAdapter=new TestAdapter(Tests.this,tests);
        listView.setAdapter(testAdapter);
        getQues();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(new Intent(Tests.this, NotificationService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        int id = item.getItemId();

        if(id==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    public void getQues(){
        //addListenerForSingleValueEvent
        myRef.child("tests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tests.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Test t=new Test();
                    t.setIsQuestion(true);
                    t.setName(snapshot.getKey());
                    t.setTime(Long.parseLong(snapshot.child("Time").getValue().toString()));
                    ArrayList<Question> ques=new ArrayList<>();
                    for (DataSnapshot qSnap:snapshot.child("Questions").getChildren()){
                        DataSnapshot snapC = qSnap.child("options");
                        String question_name = qSnap.child("question").getValue(String.class);
                        Question question = new Question(question_name);
                        for(DataSnapshot metaSnap: snapC.getChildren()) {
                            String metaDescription = metaSnap.child("description").getValue(String.class);
                            int type = metaSnap.child("type").getValue(Integer.class);
                            ArrayList<String> meta_Str = new ArrayList<>();
                            if(type < 10) {
                                for (DataSnapshot metaStr : metaSnap.child("options").getChildren()) {
                                    meta_Str.add(metaStr.getValue(String.class));
                                }
                            }
                            question.addQueue(new MetaQuestion(metaDescription, meta_Str, type));
                        }
                        ques.add(question);
                    }
                    t.setQuestions(ques);
                    tests.add(t);

                }
                testAdapter.dataList=tests;
                testAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.setVisibility(View.GONE);
                avLoadingIndicatorView.hide();
                Log.e("The read success: " ,"su"+tests.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                avLoadingIndicatorView.setVisibility(View.GONE);
                avLoadingIndicatorView.hide();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });
    }

    class TestAdapter extends ArrayAdapter<Test> implements Filterable {
        private Context mContext;
        ArrayList<Test> dataList;
        public TestAdapter( Context context,ArrayList<Test> list) {
            super(context, 0 , list);
            mContext = context;
            dataList = list;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.test_item,parent,false);

            ((ImageView)listItem.findViewById(R.id.item_imageView)).
                    setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.img));

            ((TextView)listItem.findViewById(R.id.item_textView))
                    .setText(dataList.get(position).getName());

            ((Button)listItem.findViewById(R.id.item_button)).setText("Attempt");

            (listItem.findViewById(R.id.item_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext, AttemptTest.class);
                    intent.putExtra("Questions",dataList.get(position));
                    intent.putExtra("TESTNAME",dataList.get(position).getName());
                    startActivity(intent);
                }
            });

            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    (position > lastPos) ? R.anim.up_from_bottom : R.anim.down_from_top);
            (listItem).startAnimation(animation);
            lastPos = position;

            return listItem;
        }
    }


}
