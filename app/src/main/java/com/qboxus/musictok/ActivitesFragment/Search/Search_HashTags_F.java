package com.qboxus.musictok.ActivitesFragment.Search;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.Adapters.HashTag_Adapter;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.HashTagModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.Taged_Videos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.qboxus.musictok.ActivitesFragment.Search.Search_Main_F.search_edit;

public class Search_HashTags_F extends RootFragment {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    ProgressBar load_more_progress;

    int page_count=0;
    boolean ispost_finsh;

    ArrayList <HashTagModel>  data_list;
    HashTag_Adapter adapter;

    public Search_HashTags_F(String type) {
        this.type=type;
    }

    public Search_HashTags_F() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();


        shimmerFrameLayout =view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView=view.findViewById(R.id.recylerview);
        linearLayoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        data_list=new ArrayList<>();
        adapter = new HashTag_Adapter(context, data_list, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                switch (view.getId()){
                    case R.id.fav_btn:
                        Call_api_fav_hashtag(pos,(HashTagModel) object);
                        break;
                    default:
                        HashTagModel item = (HashTagModel) object;
                        OpenHashtag(item.name);
                        break;
                }

            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Log.d("resp",""+scrollOutitems);
                if (userScrolled && (scrollOutitems == data_list.size()-1)) {
                    userScrolled = false;

                    if(load_more_progress.getVisibility()!=View.VISIBLE && !ispost_finsh){
                        load_more_progress.setVisibility(View.VISIBLE);
                        page_count=page_count+1;

                        if(type!=null && type.equalsIgnoreCase("hashtag"))
                            Call_Api_Search();
                    }
                }


            }
        });


        load_more_progress=view.findViewById(R.id.load_more_progress);
        page_count=0;

        if(type!=null && type.equalsIgnoreCase("favourite")){
            Call_Api_get_favourite();
        }
        else
            Call_Api_Search();


        return view;
    }


    public void Call_Api_Search(){

        JSONObject params=new JSONObject();
        try {
            if(Functions.getSharedPreference(context).getString(Variables.u_id,null)!=null) {
                params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));
            }

            params.put("type",type);
            params.put("keyword", search_edit.getText().toString());
            params.put("starting_point",""+page_count);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.search, params, new Callback() {
            @Override
            public void Responce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                Parse_data(resp);
            }
        });

    }

    public void Call_Api_get_favourite(){

        JSONObject params=new JSONObject();
        try {
                params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showFavouriteHashtags, params, new Callback() {
            @Override
            public void Responce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                Parse_data(resp);
            }
        });

    }

    public void Parse_data(String responce){

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){

                JSONArray msgArray=jsonObject.getJSONArray("msg");
                ArrayList<HashTagModel> temp_list=new ArrayList<>();
                for(int i=0;i<msgArray.length();i++){
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject Hashtag=itemdata.optJSONObject("Hashtag");

                    HashTagModel item=new HashTagModel();

                    item.id=Hashtag.optString("id");
                    item.name=Hashtag.optString("name");
                    item.views=Hashtag.optString("views");
                    item.videos_count=Hashtag.optString("videos_count");

                    item.fav=Hashtag.optString("favourite","1");
                    temp_list.add(item);

                }

                if(page_count==0){
                    data_list.clear();
                }

                data_list.addAll(temp_list);
                adapter.notifyDataSetChanged();

                if(data_list.isEmpty()){
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                }
                else {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                }

            }else {
                if(data_list.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        finally {
            load_more_progress.setVisibility(View.GONE);
        }
    }

    private void OpenHashtag(String tag) {

        Taged_Videos_F taged_videos_f = new Taged_Videos_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, taged_videos_f).commit();

    }



    public void Call_api_fav_hashtag(final int pos, final HashTagModel item){

        JSONObject params=new JSONObject();
        try {
            params.put("user_id",Functions.getSharedPreference(context).getString(Variables.u_id,""));
            params.put("hashtag_id",item.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context,false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.addHashtagFavourite, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                if(item.fav!=null && item.fav.equalsIgnoreCase("0")){
                    item.fav="1";
                }
                else {
                    item.fav="0";
                }

                data_list.remove(pos);
                data_list.add(pos,item);
                adapter.notifyDataSetChanged();

            }
        });

    }


}
