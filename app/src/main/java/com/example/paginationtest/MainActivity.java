package com.example.paginationtest;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.SignalStrength;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.ArrayList;

import static com.example.paginationtest.PaginationListener.PAGE_START;


public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    private PostRecyclerAdapter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;
    final ArrayList<PostItem> items = new ArrayList<>();
    String a[],b[],c[],d[];
    int x;
    String url_head = "https://curvy-lion-88.localtunnel.me";
    String url_tail = "/api/profile";
    String url = (url_head+url_tail);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new PostRecyclerAdapter(new ArrayList<PostItem>(),MainActivity.this);
        mRecyclerView.setAdapter(adapter);
        doApiCall();

        /**
         * add scroll listener while user reach in bottom load more will call
         */
        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                doApiCall();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    private void doApiCall() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        a = new String[response.length()];
                        b = new String[response.length()];
                        c = new String[response.length()];
                        d = new String[response.length()];
                        x = response.length();

                        for(int i = 0;i<response.length();i++){
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                a[i] = jsonObject.getString("name");
                                b[i] = jsonObject.getString("rank");
                                c[i] = jsonObject.getString("image_url");
                                d[i] = jsonObject.getString("type");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        try {
            for(int i = 0;i<x;i++){

                PostItem postItem = new PostItem();
                postItem.setTitle(a[i]);
                postItem.setDescription((b[i]));
                postItem.setImage_url(url_head+(c[i]));
                postItem.setType((d[i]));
                items.add(postItem);
            }
        }catch (Exception e){

        }

       MySingleton.getInstance(MainActivity.this).addToRequestque(jsonArrayRequest);

        if (currentPage != PAGE_START) adapter.removeLoading();
        adapter.addItems(items);
        swipeRefresh.setRefreshing(false);

        // check weather is last page or not
        if (currentPage < totalPage) {
            adapter.addLoading();
        } else {
            isLastPage = true;
        }
        isLoading = false;


    }

    @Override
    public void onRefresh() {
        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        doApiCall();
    }

}
