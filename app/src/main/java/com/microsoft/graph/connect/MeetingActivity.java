package com.microsoft.graph.connect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.thirdparty.publicsuffix.PublicSuffixPatterns;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by irfan on 18.09.17.
 */

public class MeetingActivity extends AppCompatActivity implements
        View.OnClickListener{


        // arguments for this activity
        public static final String ARG_GIVEN_NAME = "givenName";
        public static final String ARG_DISPLAY_ID = "displayableId";

        // views
        private EditText TmeetingSubject;
        private Button BcreateMeeting;
        private ProgressBar MeetingrogressBar;
        private String mGivenName;
        private TextView mConclusionTextView;
        public  Spinner spinner1, spinner2, spinner3;
        Context context;
         Map<String,String> subjct_id =  new HashMap<String,String>();
    Map<String,String> id_subject =  new HashMap<String,String>();

         ArrayList<String> categoryList = new ArrayList<String>();

    Button btnDatePicker, btnTimePicker, btnCreate, btnCancel, btnUpdate;
    EditText txtDate, txtTime, txtCreate, txtCancel, txtUpdate;
    String St_Date_time = "";
    String End_Date_time = "";
    String[] Meeting_prams = {"2018-09-18T19:40:00", "2018-09-18T20:40:00", "Default Subject"};
    private int mYear, mMonth, mDay, mHour, mMinute;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker_spinner);


        // find the views
        TextView mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        TmeetingSubject = (EditText) findViewById(R.id.subj_create);
        BcreateMeeting = (Button) findViewById(R.id.btn_create);
        MeetingrogressBar = (ProgressBar) findViewById(R.id.meetingProgressBar);
        mConclusionTextView = (TextView) findViewById(R.id.conclusionTextView);

        /// Spinner

          spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner3 = (Spinner) findViewById(R.id.spinner3);
//        ArrayList<String> categoryList = new ArrayList<String>();
//        categoryList.add("1");
//        categoryList.add("2");
//
//
//        ArrayAdapter<String> madapter = new ArrayAdapter<String>(MeetingActivity.this
//        , android.R.layout.simple_list_item_1, categoryList);
//         madapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinner1.setAdapter(madapter);

        // Extract the givenName and displayableId and use it in the UI.
        mGivenName = getIntent().getStringExtra(ARG_GIVEN_NAME);
        mTitleTextView.append(mGivenName + "!");
        TmeetingSubject.setText(getIntent().getStringExtra(ARG_DISPLAY_ID));


            btnDatePicker=(Button)findViewById(R.id.btn_date);
            btnTimePicker=(Button)findViewById(R.id.btn_time);
            btnCreate=(Button)findViewById(R.id.btn_create);
            btnCancel=(Button)findViewById(R.id.btn_cancel);
            btnUpdate=(Button)findViewById(R.id.btn_update);

            txtDate=(EditText)findViewById(R.id.in_date);
            txtTime=(EditText)findViewById(R.id.in_time);
            txtCreate=(EditText)findViewById(R.id.subj_create);
//            txtCancel=(EditText)findViewById(R.id.subj_cancel);
//            txtUpdate=(EditText)findViewById(R.id.subj_update);

            txtCreate.setText("TEST MEETING");
//            txtCancel.setText("TEST MEETING");
//            txtUpdate.setText("TEST MEETING");

            btnDatePicker.setOnClickListener((View.OnClickListener) this);
            btnTimePicker.setOnClickListener((View.OnClickListener) this);
        //    btnCreate.setOnClickListener(this);
          //  btnCancel.setOnClickListener(this);
            //btnUpdate.setOnClickListener(this);
             context = MeetingActivity.this;


        }

    @Override
    public void onClick(View v) {

        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);


            // Launch Time Picker Dialog


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            //txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);


                            St_Date_time = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;


                        }
                    }, mYear, mMonth, mDay);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            //2017-09-19T19:00:00
                            String datetime = formatedate(St_Date_time + " " + hourOfDay + ":" + minute + ":00");
                            St_Date_time = datetime.substring(0, 10) + "T" + datetime.substring(11, datetime.length());
                            Meeting_prams[0] = St_Date_time;
                            txtDate.setText(St_Date_time);
                        }
                    }, mHour, mMinute, false);

            timePickerDialog.show();
            datePickerDialog.show();


        }

        if (v == btnTimePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            End_Date_time = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                        }
                    }, mYear, mMonth, mDay);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            String datetime = formatedate(End_Date_time + " " + hourOfDay + ":" + minute + ":00");
                            End_Date_time = datetime.substring(0, 10) + "T" + datetime.substring(11, datetime.length());
                            Meeting_prams[1] = End_Date_time;
                            txtTime.setText(End_Date_time);


                        }
                    }, mHour, mMinute, false);

            timePickerDialog.show();
            datePickerDialog.show();

        }



    }


    public String formatedate(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Calendar c = Calendar.getInstance();
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(dateTime);

        dateTime = c.getTime();
        String fdt= dateFormat.format(dateTime);
        return fdt;


    }

    public void  clear_spiner () {

        ArrayList<String> categoryList_emty = new ArrayList<String>();

        categoryList.clear();

        spinner1.setAdapter(new ArrayAdapter<String>(MeetingActivity.this, android.R.layout.simple_spinner_item, categoryList_emty));
        spinner2.setAdapter(new ArrayAdapter<String>(MeetingActivity.this, android.R.layout.simple_spinner_item, categoryList_emty));
        spinner3.setAdapter(new ArrayAdapter<String>(MeetingActivity.this, android.R.layout.simple_spinner_item, categoryList_emty));
    }


    public void create_spinner(ArrayList<String> categoryList){

        ArrayAdapter<String> madapter = new ArrayAdapter<String>(MeetingActivity.this
                , android.R.layout.simple_list_item_1, categoryList);
        madapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(madapter);
        spinner2.setAdapter(madapter);
        spinner3.setAdapter(madapter);
    }




    /**
         * Handler for the onclick event of the send mail button. It uses the GraphServiceController to
         * send an email. When the call is completed, the call will return to either the success()
         * or failure() methods in this class which will then take the next steps on the UI.
         * This method sends the email using the address stored in the mEmailEditText view.
         * The subject and body of the message is stored in the strings.xml file.
         *
         * @param v The view.
         */
    public void onCreateMeetingButtonClick(View v) throws ParseException {
        resetUIForMeeting();

        //Prepare body message and insert name of sender
        //String body = getString(R.string.mail_body_text);

        //body = body.replace("{0}", mGivenName);
        //2017-09-19T19:00:00

        //18-9-2017 18:49
        Meeting_prams[2]= String.valueOf(txtCreate.getText());

        new GraphServiceController()
                .CreateMeeting(
                        Meeting_prams[2],Meeting_prams[0] , Meeting_prams[1],
                        new ICallback<JsonObject>() {
//                            @Override
//                            public void success(Void aVoid) {
//                                showSendMailSuccessUI();
//                            }

                            @Override
                            public void success(JsonObject jsonObject) {

                                MeetingCreateSuccessUI();

                            }

                            @Override
                            public void failure(ClientException ex) {
                                showCreatMeetingErrorUI();
                            }
                        }
                );

    }




    public void onFindMeetingButtonClick(View v) {
        resetUIForMeeting();
        clear_spiner();


        //18-9-2017 18:49
        //Meeting_prams[2]= String.valueOf(txtUpdate.getText());




        new GraphServiceController()
                .FindMeeting(
                        MeetingActivity.this, Meeting_prams[2],Meeting_prams[0] , Meeting_prams[1],
                        new ICallback<Void>() {

//                            @Override
//                            public void success(ICallback result) {
//
//                                MeetingDeleteSuccessUI();
//
//                            }

                            @Override
                            public void success(Void aVoid) {


                                Log.d("Calback Find", "Success");
                                MeetingFindSuccessUI();

                            }

                            @Override
                            public void failure(ClientException ex) {

                                showCreatMeetingErrorUI();
                            }
                        }
                );

    }


    public void onDeleteMeetingButtonClick(View v) {
        resetUIForMeeting();

        //18-9-2017 18:49
       // Meeting_prams[2]= String.valueOf(txtCancel.getText());
       String subject = spinner2.getSelectedItem().toString();
        String id = (String) subjct_id.get(subject);

        new GraphServiceController()
                .DeleteMeeting(
                          subject, id,
                        new ICallback<JsonObject>() {

                            @Override
                            public void success(JsonObject jsonObject) {

                                MeetingDeleteSuccessUI();

                            }

                            @Override
                            public void failure(ClientException ex) {
                                showCreatMeetingErrorUI();
                            }
                        }
                );

    }


    public void onUpdateMeetingButtonClick(View v) {
        resetUIForMeeting();

        String subject = spinner3.getSelectedItem().toString();
        String id = (String) subjct_id.get(subject);

        new GraphServiceController()
                .UpdateMeeting(
                        subject,id ,
                        new ICallback<JsonObject>() {

                            @Override
                            public void success(JsonObject jsonObject) {

                                MeetingUpdateSuccessUI();

                            }

                            @Override
                            public void failure(ClientException ex) {
                                showCreatMeetingErrorUI();
                            }
                        }
                );

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnectMenuItem:
                AuthenticationManager.getInstance().disconnect();
                Intent connectIntent = new Intent(this, ConnectActivity.class);
                startActivity(connectIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetUIForMeeting() {
        BcreateMeeting.setVisibility(View.GONE);
        mConclusionTextView.setVisibility(View.GONE);
        MeetingrogressBar.setVisibility(View.VISIBLE);
    }

//    private void showSendMailSuccessUI() {
//        MeetingrogressBar.setVisibility(View.GONE);
//        BcreateMeeting.setVisibility(View.VISIBLE);
//        mConclusionTextView.setText(R.string.conclusion_text);
//        mConclusionTextView.setVisibility(View.VISIBLE);
//        Toast.makeText(
//                .this,
//                R.string.send_mail_toast_text,
//                Toast.LENGTH_SHORT).show();
//    }

    private void MeetingCreateSuccessUI() {
        MeetingrogressBar.setVisibility(View.GONE);
        BcreateMeeting.setVisibility(View.VISIBLE);
        mConclusionTextView.setText("Meeting has been created successfully");
        mConclusionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(MeetingActivity.this,
                "Meeting Created",
                Toast.LENGTH_SHORT).show();
    }

    private void MeetingDeleteSuccessUI() {
        MeetingrogressBar.setVisibility(View.GONE);
        BcreateMeeting.setVisibility(View.VISIBLE);
        mConclusionTextView.setText("Meeting has been deleted successfully");
        mConclusionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(MeetingActivity.this,
                "Meeting Deleted",
                Toast.LENGTH_SHORT).show();
    }

    private void MeetingUpdateSuccessUI() {
        MeetingrogressBar.setVisibility(View.GONE);
        BcreateMeeting.setVisibility(View.VISIBLE);
        mConclusionTextView.setText("Meeting has been UPDATE successfully");
        mConclusionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(MeetingActivity.this,
                "Meeting UPDATED",
                Toast.LENGTH_SHORT).show();

    }
    private void MeetingFindSuccessUI() {
        MeetingrogressBar.setVisibility(View.GONE);
        BcreateMeeting.setVisibility(View.VISIBLE);
        mConclusionTextView.setText("Meeting Find successfully");
        mConclusionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(MeetingActivity.this,
                "Meeting Find",
                Toast.LENGTH_SHORT).show(); }

    private void showCreatMeetingErrorUI() {
        MeetingrogressBar.setVisibility(View.GONE);
        BcreateMeeting.setVisibility(View.VISIBLE);
        mConclusionTextView.setText("Sorry, Cannot perform Action");
        mConclusionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(
                MeetingActivity.this,
                "Error Meeting Request",
                Toast.LENGTH_LONG).show();
    }



}
