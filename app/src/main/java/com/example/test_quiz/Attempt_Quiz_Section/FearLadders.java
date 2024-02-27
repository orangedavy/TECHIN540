package com.example.test_quiz.Attempt_Quiz_Section;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.test_quiz.Model.MetaQuestion;
import com.example.test_quiz.Model.Question;
import com.example.test_quiz.Model.Test;
import com.example.test_quiz.NotificationActivity.NotificationService;
import com.example.test_quiz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FearLadders extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private ListView listView;

    private FirebaseAuth auth;
    private TestAdapter testAdapter;
    private int lastPos = -1;

    private String client_id="23RS47";

    private String authorization = "Basic MjNSUzQ3OjRkYTUzNzg3MGEwY2FjNzk3NGFlMWU3N2M5YjA0Njlm";

    private Integer heartrate;

    ArrayList<Test> tests=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        auth= FirebaseAuth.getInstance();
        avLoadingIndicatorView = findViewById(R.id.loader1);
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
        avLoadingIndicatorView.show();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);
        database = FirebaseDatabase.getInstance("https://dialysis-55e67-default-rtdb.firebaseio.com/");

        myRef=database.getReference();
        listView=findViewById(R.id.test_listview);
        testAdapter=new TestAdapter(FearLadders.this,tests);
        listView.setAdapter(testAdapter);
        getHeartrate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(new Intent(FearLadders.this, NotificationService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void getHeartrate() {
        myRef.child("user_auth").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String refresh_token = dataSnapshot.child("refresh_token").getValue(String.class);
                    refreshToken(authorization, client_id, refresh_token);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void refreshToken(final String authorization, final String client_id, final String refresh_token){
        RequestQueue queue = Volley.newRequestQueue(this); // 'this' is Context
        String url = "https://api.fitbit.com/oauth2/token";
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            // 解析响应中的静息心率
                            String accessToken = jsonResponse
                                    .getString("access_token");
                            System.out.println("access_token: " + accessToken);
                            String new_refresh_token = jsonResponse
                                    .getString("refresh_token");
                            //isUpdating = true;
                            myRef.child("user_auth").child(auth.getUid()).child("refresh_token").setValue(new_refresh_token);
                            fetchRestingHeartRate(accessToken);
                            // 在这里更新UI或处理数据

                        } catch (Exception e) {
                            e.printStackTrace();
                            // 处理异常
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "refresh_token");
                params.put("client_id", client_id); // 若需要，可省略此行，因为client_id在Authorization头已提供
                params.put("refresh_token", refresh_token);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("Authorization", authorization);
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void fetchRestingHeartRate(final String accessToken) {

        RequestQueue queue = Volley.newRequestQueue(this); // 'this' is Context
        String url = "https://api.fitbit.com/1/user/-/activities/heart/date/2024-02-26/1d/1min.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 解析响应中的静息心率
                            heartrate = response
                                    .getJSONArray("activities-heart")
                                    .getJSONObject(0)
                                    .getJSONObject("value")
                                    .getInt("restingHeartRate");
                            // 在这里更新UI或处理数据
                            System.out.println("Resting Heart Rate: " + heartrate);
                            myRef.child("Results").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Integer current = dataSnapshot.child("current").getValue(Integer.class);
                                    ArrayList<String> ladder = new ArrayList<>();
                                    for(DataSnapshot ladder_snap: dataSnapshot.child("items").getChildren()){
                                        ladder.add(ladder_snap.getValue(String.class));
                                    }
                                    getQues(heartrate, ladder, current);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 处理异常
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };

        // 将请求添加到请求队列
        queue.add(jsonObjectRequest);
    }

    public void getQues(final Integer heart_rate, final ArrayList<String> ladders, final Integer current){
        //addListenerForSingleValueEvent

        myRef.child("fear_ladder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tests.clear();
                for(Integer i = 0; i < ladders.size(); i ++){
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        String ladder_name = snapshot.getKey();
                        if(ladder_name.equals(ladders.get(i))) {
                            Test t = new Test();
                            t.setName(snapshot.getKey());
                            t.setIsQuestion(false);
                            t.setTime(Long.parseLong(snapshot.child("Time").getValue().toString()));
                            ArrayList<Question> ques = new ArrayList<>();
                            for (DataSnapshot qSnap : snapshot.child("Questions").getChildren()) {
                                DataSnapshot snapC = qSnap.child("options");
                                String question_name = qSnap.child("question").getValue(String.class);
                                Question question = new Question(question_name);
                                for (DataSnapshot metaSnap : snapC.getChildren()) {
                                    String metaDescription = metaSnap.child("description").getValue(String.class);
                                    int type = metaSnap.child("type").getValue(Integer.class);
                                    ArrayList<String> meta_Str = new ArrayList<>();
                                    if (type < 10) {
                                        for (DataSnapshot metaStr : metaSnap.child("options").getChildren()) {
                                            meta_Str.add(metaStr.getValue(String.class));
                                        }
                                    }
                                    question.addQueue(new MetaQuestion(metaDescription, meta_Str, type));
                                }
                                ques.add(question);
                            }
                            t.rank = i;
                            if(i < current){
                                t.setColor(0xFF00FF00); // Green for passed
                            } else if (i == current) {
                                t.setColor(0xFF0000FF); // Blue for current
                            } else{
                                t.setColor(0xFFFF0000); // Red for future
                            }
                            t.setQuestions(ques);
                            t.setHeartrate(heart_rate);
                            tests.add(t);
                        }
                    }
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
            Integer color = dataList.get(position).getColor();
            listItem.findViewById(R.id.t_layout).setBackgroundColor(color);
            ((ImageView)listItem.findViewById(R.id.item_imageView)).
                    setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.img));

            ((TextView)listItem.findViewById(R.id.item_textView))
                    .setText(dataList.get(position).getName());

            ((Button)listItem.findViewById(R.id.item_button)).setText("Attempt");
            if(color == 0xFF0000FF){
                ((Button)listItem.findViewById(R.id.item_button)).setEnabled(true);
            }
            else{
                ((Button)listItem.findViewById(R.id.item_button)).setEnabled(false);
            }
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
