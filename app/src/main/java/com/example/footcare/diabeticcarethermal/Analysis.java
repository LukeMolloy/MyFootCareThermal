package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.footcare.diabeticcarethermal.R.id.container;

public class Analysis extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }



    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_analysis, container, false);
            DatabaseHelper myDB = new DatabaseHelper(getContext());

            Cursor res = myDB.getAllData(myDB.TABLE_ANALYSIS);
            final int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final ArrayList<String> SizedayS = new ArrayList<String>();

            int i = 0;
            int b = 0;

            while (res.moveToNext()) {
                if (!Objects.equals(res.getString(1), null)) {
                   if (!Objects.equals(res.getString(1), "")) {
                        SizedayS.add(String.valueOf(i));
                       i++;
                    }
                 }
            }
            final int SizeSize = SizedayS.size();

            Button calculate = (Button) rootView.findViewById(R.id.calculate);

            //Create adapter for min and max spinner
            final Spinner spinner = (Spinner) rootView.findViewById(R.id.minspinner);
            final Spinner maxspinner = (Spinner) rootView.findViewById(R.id.maxspinner);

            if (sectionNumber == 1){
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_list_item_1, SizedayS);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);
                maxspinner.setAdapter(adapter);
                maxspinner.setSelection(SizedayS.size()-1);
            }
            spinner.setSelection(0);

            //int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            if (sectionNumber == 1){
                TextView chartTitle = (TextView) rootView.findViewById(R.id.chartTitle);
                chartTitle.setText("Wound Size");
                TextView yTitle = (TextView) rootView.findViewById(R.id.yTitle);
                yTitle.setText("Wound Size (cm2)");
            }
            Log.d("CREATE SECTION NUMBER: ", "" + getArguments().getInt(ARG_SECTION_NUMBER));
            final GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

            createGraph(graph, Integer.valueOf((String) spinner.getSelectedItem()), Integer.valueOf((String) maxspinner.getSelectedItem()), SizeSize);

            calculate.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((Integer.valueOf((String) spinner.getSelectedItem())) < (Integer.valueOf((String) maxspinner.getSelectedItem()))) {
                                createGraph(graph, Integer.valueOf((String) spinner.getSelectedItem()), Integer.valueOf((String) maxspinner.getSelectedItem()), SizeSize);
                               // createGraph(graph, Integer.valueOf((String) spinner.getSelectedItem()), Integer.valueOf((String) maxspinner.getSelectedItem()));
                            }
                            if ((Integer.valueOf((String) spinner.getSelectedItem())) == (Integer.valueOf((String) maxspinner.getSelectedItem()))) {
                                showMessage("Min cannot be the same as Max","");
                                DatabaseHelper myDB = new DatabaseHelper(getContext());
                                Cursor res = myDB.getAllData(myDB.TABLE_ANALYSIS);
                                spinner.setSelection(0);
                                //maxspinner.setSelection(res.getCount()-1);
                                if (sectionNumber == 1) {
                                    maxspinner.setSelection(SizedayS.size()-1);
                                }
                                return;
                            }
                            if ((Integer.valueOf((String) spinner.getSelectedItem())) > (Integer.valueOf((String) maxspinner.getSelectedItem()))) {
                                showMessage("Min cannot be greater than Max","");
                                DatabaseHelper myDB = new DatabaseHelper(getContext());
                                Cursor res = myDB.getAllData(myDB.TABLE_ANALYSIS);
                                spinner.setSelection(0);
                               // maxspinner.setSelection(res.getCount()-1);
                                if (sectionNumber == 1) {
                                    maxspinner.setSelection(SizedayS.size()-1);
                                }
                                return;
                            }
                        }
                    }
            );

            return rootView;
    }

        public void showMessage(String title, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(true);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.show();
        }

    public void createGraph(GraphView graph, int MinX, int MaxX, int sizeSize){
        //Taken from http://www.android-graphview.org/  Documentation
        //public void createGraph(GraphView graph, int MinX, int MaxX, int tempSize, int sizeSize){
        //View rootView = inflater.inflate(R.layout.fragment_analysis, container, false);
        LineGraphSeries series;
        PointsGraphSeries series2;


        DatabaseHelper myDB = new DatabaseHelper(getContext());
        Cursor res = myDB.getAllData(myDB.TABLE_ANALYSIS);

        int i = 0;

        int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        if (sectionNumber == 1) {
            DataPoint[] dataPointsSize = new DataPoint[sizeSize];
            while (res.moveToNext()) {
                if (!Objects.equals(res.getString(1), null)) {
                    if (!Objects.equals(res.getString(1), "")) {
                        dataPointsSize[i] = new DataPoint(i, Double.valueOf(res.getString(1)));
                        i++;
                    }
                }
            }
            series = new LineGraphSeries<DataPoint>(dataPointsSize);
            graph.addSeries(series);
            series.setColor(Color.rgb(12,110,72));
            series.setThickness(16);

            series2 = new PointsGraphSeries<DataPoint>(dataPointsSize);
            graph.addSeries(series2);
            series2.setShape(PointsGraphSeries.Shape.POINT);
            series2.setColor(Color.rgb(12,110,72));
        }

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(50);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(MinX);
        graph.getViewport().setMaxX(MaxX);
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return Analysis.PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "WOUND SIZE";
                case 1:
                    return "TEMPERATURE";
                case 2:
                    return "TISSUE";
            }
            return null;
        }
    }
}
