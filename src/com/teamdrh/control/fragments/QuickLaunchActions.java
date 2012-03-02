
package com.teamdrh.control.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.teamdrh.control.R;

public class QuickLaunchActions extends Fragment {
    private static final String DEFAULT_ADD_ACTION = "nothing";

    private ButtonOnClickListener mButtonOnClickListener;
    private ListView mActionsListView;
    private ArrayList<String> mActions;
    private ActionsAdapter mActionsAdapter;
    private String[] mActionActivities;
    private String[] mActionActivitiesActions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtonOnClickListener = new ButtonOnClickListener();
        mActions = new ArrayList<String>();
        mActionsAdapter = new ActionsAdapter(getActivity(),
                R.layout.drh_interface_lockscreen_actions_action,
                R.id.drh_lockscreen_actions_action_text, mActions);
        String actions = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.DRH_SYSTEMUI_QUICKLAUNCH_ACTIONS);
        if (actions == null) {
            actions = "";
        }
        mActionsAdapter.addAll(Arrays.asList(actions.split("\\|")));

        PackageManager pm = getActivity().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);

        TreeMap<String, String> mActivityMap = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        for (ResolveInfo info : activities) {
            ComponentName component = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            String label = info.activityInfo.loadLabel(pm).toString();
            if (!(label == null || label.equals("")) && component != null) {
                mActivityMap.put(label, "app:" + component.flattenToString());
            }
        }

        int size = mActivityMap.size(); // Plus two for unlock & nothing.
        mActionActivities = new String[size];
        mActionActivitiesActions = new String[size];

        int i = 0;
        for (String activityName : mActivityMap.navigableKeySet()) {
            mActionActivities[i] = activityName;
            mActionActivitiesActions[i] = mActivityMap.get(activityName);
            i++;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.drh_interface_lockscreen_actions, container, false);
        ((Button) v.findViewById(R.id.drh_lockscreen_actions_add))
                .setOnClickListener(mButtonOnClickListener);
        ((Button) v.findViewById(R.id.drh_lockscreen_actions_save))
                .setOnClickListener(mButtonOnClickListener);
        mActionsListView = (ListView) v.findViewById(R.id.drh_lockscreen_actions_list);
        mActionsListView.setAdapter(mActionsAdapter);
        return v;
    }

    private class ButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.drh_lockscreen_actions_save:
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < mActions.size(); i++) {
                        stringBuilder.append(mActions.get(i));
                        if (i + 1 < mActions.size()) stringBuilder.append("|");
                    }
                    Settings.System
                            .putString(getActivity().getContentResolver(),
                                    Settings.System.DRH_SYSTEMUI_QUICKLAUNCH_ACTIONS,
                                    stringBuilder.toString());
                    return;
                case R.id.drh_lockscreen_actions_add:
                    mActionsAdapter.add(DEFAULT_ADD_ACTION);
                    return;
            }
        }

    }

    private class ActionsAdapter extends ArrayAdapter<String> {
        private Activity mContext;
        private int mLayoutId;
        private int mTextViewResourcesId;
        private List<String> mSequences;
        private PackageManager mPackageManager;

        public ActionsAdapter(Activity context, int layout, int textViewResourceId,
                List<String> objects) {
            super(context, textViewResourceId, objects);
            mContext = context;
            mLayoutId = layout;
            mTextViewResourcesId = textViewResourceId;
            mSequences = objects;
            mPackageManager = mContext.getPackageManager();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View newView = null;
            if (convertView != null)
                newView = convertView;
            else {
                newView = ((LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(mLayoutId, parent, false);
            }

            ((Button) newView.findViewById(R.id.drh_lockscreen_actions_action_remove))
                    .setOnClickListener(new OnRemoveClick(position));
            ((Button) newView.findViewById(R.id.drh_lockscreen_actions_action_add))
                    .setOnClickListener(new OnAddClick(position));
            newView.setOnClickListener(new OnItemClick(position));

            String action = getItem(position);
            String text = null;
            Drawable drawable = null;
            if (action.startsWith("app:")) {
                String data = action.substring(4);
                ComponentName component = ComponentName.unflattenFromString(data);
                if (component != null) {
                    try {
                        ActivityInfo info = mPackageManager.getActivityInfo(component, 0);
                        text = info.loadLabel(mPackageManager).toString();
                        Intent intent = new Intent();
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setComponent(component);
                        drawable = mPackageManager.getActivityIcon(intent);
                    } catch (PackageManager.NameNotFoundException e) {
                        mSequences.remove(position);
                    }
                }
            } else if (action == null || action.equals("")) {
                mSequences.remove(position);
            }

            if (text == null || text.equals("")) text = "Select an action...";

            ((TextView) newView.findViewById(mTextViewResourcesId)).setText(text);
            ((ImageView) newView.findViewById(R.id.drh_lockscreen_actions_action_icon))
                    .setImageDrawable(drawable);
            return newView;
        }

        private class OnRemoveClick implements View.OnClickListener {
            private int mPosition;

            public OnRemoveClick(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                mSequences.remove(mPosition);
                notifyDataSetChanged();
            }

        }

        private class OnAddClick implements View.OnClickListener {
            private int mPosition;

            public OnAddClick(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                mSequences.add(mPosition, DEFAULT_ADD_ACTION);
                notifyDataSetChanged();
            }
        }

        private class OnItemClick implements View.OnClickListener {
            private int mPosition;

            public OnItemClick(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                String content = getItem(mPosition);
                int index = -1;
                if (content == null || content.equals("") || content.equals("null")) {
                    index = 0;
                } else {
                    index = Arrays.binarySearch(mActionActivitiesActions, content);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle("Select Activity")
                        .setSingleChoiceItems(mActionActivities, index,
                                new Dialog.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSequences.remove(mPosition);
                                        mSequences.add(mPosition, mActionActivitiesActions[which]);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                })
                        .create().show();
            }
        }
    }
}
