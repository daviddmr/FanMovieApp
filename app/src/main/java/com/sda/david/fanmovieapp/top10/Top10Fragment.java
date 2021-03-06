package com.sda.david.fanmovieapp.top10;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sda.david.fanmovieapp.R;
import com.sda.david.fanmovieapp.api.ServiceGenerator;
import com.sda.david.fanmovieapp.api.interfaces.MovieService;
import com.sda.david.fanmovieapp.model.Movie;
import com.sda.david.fanmovieapp.model.User;
import com.sda.david.fanmovieapp.movies.MovieDetailActivity;
import com.sda.david.fanmovieapp.util.ShowMessageUtil;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by david on 29/04/2017.
 */

public class Top10Fragment extends Fragment {

    public static final String TAG = "Top10Frag";
    private static final String ARG_USER = "arg_user";

    private User user;

    RecyclerView rvMovies;

    List<Movie> movies;

    public static Top10Fragment newInstance(User user) {
        Top10Fragment fragment = new Top10Fragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_USER, user);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            user = getArguments().getParcelable(ARG_USER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top10_movies, container, false);
        initComponents(rootView);

        return rootView;
    }

    private void initComponents(View rootView) {
        rvMovies = (RecyclerView) rootView.findViewById(R.id.rv_movies);
        rvMovies.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        rvMovies.setLayoutManager(mLinearLayoutManager);

        findTop10Movies();
    }

    private void fillScreen() {
        Top10Adapter adapter = new Top10Adapter(getContext(), movies, movieClickListener());
        rvMovies.setAdapter(adapter);
    }

    private View.OnClickListener movieClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = view.getId();

                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.ARG_MOVIE, movies.get(position));
                intent.putExtra(MovieDetailActivity.ARG_USER, user);
                startActivity(intent);
            }
        };
    }

    private void findTop10Movies() {
        Call<List<Movie>> call = ServiceGenerator.createService(MovieService.class).findTop10();
        call.enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.isSuccessful()) {
                    movies = response.body();
                    movies.removeAll(Collections.<Movie>singleton(null));
                    fillScreen();
                } else {
                    ServiceGenerator.verifyErrorResponse(response.code(), rvMovies, getContext(), false, getActivity());
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                ServiceGenerator.verifyFailedConnection(t, rvMovies, getContext());
            }
        });
    }

}
