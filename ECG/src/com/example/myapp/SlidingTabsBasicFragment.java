package com.example.myapp;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.myapp.DataModel.ISettingsProvider;
import com.example.myapp.DropBox.IDropboxSession;
import com.example.myapp.ViewModel.BluetoothFragment;
import com.example.myapp.ViewModel.EcgFragment;
import com.example.myapp.ViewModel.HistoryFragment;
import com.example.myapp.ViewModel.PatientFragment;

import javax.inject.Inject;

/**
 * A basic sample which shows how to use {@link com.example.android.common.view.SlidingTabLayout}
 * to display a custom {@link android.support.v4.view.ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsBasicFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasicFragment";


    @Inject
    ISettingsProvider patientProvider;
    @Inject
    IDropboxSession dropboxSession;

    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
             EcgApplication app = (EcgApplication)activity.getApplication();
             app.getObjectGraph().inject(this);

        } catch (ClassCastException ignore) {
            throw new IllegalStateException("Activity " + activity + " must implement ITaskListManager");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 5;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        String[] titles = new String[]{"ECG  Monitor", "Settings", "Upload History", "Bluetooth settings", "About"};
        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            // Inflate a new layout from our resources
            View view = getActivity().getLayoutInflater().inflate(R.layout.pager_item,
                    container, false);

            if (position == 0)
            {
                final EcgFragment swapFrag = new EcgFragment();
                swapFrag.onAttach(getActivity());
                view = swapFrag.onCreateView(getActivity().getLayoutInflater(),container,getArguments());
                container.addView(view);
                return view;
            }


            if (position == 1)
            {

                final PatientFragment swapFrag = new PatientFragment();
                swapFrag.onAttach(getActivity());
                view = swapFrag.onCreateView(getActivity().getLayoutInflater(),container,getArguments());
                container.addView(view);
                return view;

            }


            if (position == 2)
            {
                final HistoryFragment swapFrag = new HistoryFragment();
                swapFrag.onAttach(getActivity());
                view = swapFrag.onCreateView(getActivity().getLayoutInflater(),container,getArguments());
                container.addView(view);
                return view;
            }

            //http://developer.android.com/training/basics/fragments/index.html
            if (position == 3)
            {
                final BluetoothFragment swapFrag = new BluetoothFragment();
                swapFrag.onAttach(getActivity());
                view = swapFrag.onCreateView(getActivity().getLayoutInflater(),container,getArguments());
                container.addView(view);
                return view;
            }

            if (position == 4)
            {
                view = getActivity().getLayoutInflater().inflate(R.layout.about_tab,
                        container, false);
                container.addView(view);
                return view;
            }

//
//            if (position == 6)
//            {
//
//
////                FragmentTransaction transaction =  getFragmentManager().beginTransaction();
////                final PatientFragment swapFrag = new PatientFragment();
////                transaction.replace(R.layout.pager_item, swapFrag);
////                transaction.commit();
////                container.addView(swapFrag.getView());
////                return swapFrag.getView();
//
//
//                final PatientFragment swapFrag = new PatientFragment();
//                //swapFrag.patientProvider = patientProvider;
//                //swapFrag.dropboxSession = dropboxSession;
//                swapFrag.onAttach(getActivity());
//                view = swapFrag.onCreateView(getActivity().getLayoutInflater(),container,getArguments());
//                container.addView(view);
//                return view;
//            }



            // Add the newly created View to the ViewPager
            container.addView(view);

            // Retrieve a TextView from the inflated View, and update it's text
            TextView title = (TextView) view.findViewById(R.id.item_title);
            title.setText(String.valueOf(position + 1));

         //   Log.i(LOG_TAG, "instantiateItem() [position: " + position + "]");

            // Return the View
            return view;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position == 1)
                dropboxSession.removeUpdater(null);
            container.removeView((View) object);
       //     Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
        }

    }
}
