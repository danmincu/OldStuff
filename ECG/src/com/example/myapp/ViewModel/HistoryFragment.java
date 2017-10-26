package com.example.myapp.ViewModel;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.EcgApplication;
import com.example.myapp.IUpdater;
import com.example.myapp.R;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment implements IUpdater, IDisplayList {

    @Inject
    IDropboxSession dropboxSession;

    ListView historyView;
    private ProgressBar progressBar;
    Activity activity;
    List<String> displayList;
    List<String> urlList;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            //    this.currentPosition = savedInstanceState.getInt(NoteFragment.ARG_POSITION);
        }
        View view = inflater.inflate(R.layout.history_tab, container, false);

        dropboxSession.listAsync(this);

        this.historyView = (ListView) view.findViewById(R.id.historyList);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        this.showProgress();

        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        EcgApplication app = (EcgApplication) activity.getApplication();
        app.getObjectGraph().inject(this);
    }


    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public void Update() {

    }

    public void displayList(List<String> list) {
        this.hideProgress();
        if (list == null)
            return;
        displayList = new ArrayList<String>();
        urlList = new ArrayList<String>();
        int i = 0;
        for (String s : list) {
            if (i % 2 == 0)
                displayList.add(s);
            else
                urlList.add(s);
            i++;
        }


        this.historyView.setAdapter(new ArrayAdapter<String>(this.activity,
                android.R.layout.simple_list_item_1, displayList.toArray(new String[]{})));

        this.historyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent,
                                    final View view, final int position, final long id) {

                final TextView v = (TextView) view;
                String url = urlList.get(position);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                activity.startActivity(i);
            }
        });


    }
}

