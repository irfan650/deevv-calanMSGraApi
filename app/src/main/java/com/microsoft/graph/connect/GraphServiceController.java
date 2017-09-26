/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.graph.connect;

import android.app.DownloadManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.Attendee;
import com.microsoft.graph.extensions.AttendeeType;
import com.microsoft.graph.extensions.BodyType;
import com.microsoft.graph.extensions.DateTimeTimeZone;
import com.microsoft.graph.extensions.DayOfWeek;
import com.microsoft.graph.extensions.EmailAddress;
import com.microsoft.graph.extensions.Event;
import com.microsoft.graph.extensions.IEventCollectionPage;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.extensions.ItemBody;
import com.microsoft.graph.extensions.Location;
import com.microsoft.graph.extensions.Message;
import com.microsoft.graph.extensions.PatternedRecurrence;
import com.microsoft.graph.extensions.Recipient;
import com.microsoft.graph.extensions.RecurrencePattern;
import com.microsoft.graph.extensions.RecurrencePatternType;
import com.microsoft.graph.extensions.RecurrenceRange;
import com.microsoft.graph.extensions.RecurrenceRangeType;
import com.microsoft.graph.model.DateOnly;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.microsoft.graph.connect.MeetingActivity;

/**
 * Handles the creation of the message and using the GraphServiceClient to
 * send the message. The app must have connected to Office 365 before using the
 * {@link #sendMail(String, String, String, ICallback)}method.
 */
class GraphServiceController extends MeetingActivity {

    private final IGraphServiceClient mGraphServiceClient;
    public GraphServiceController() {
        mGraphServiceClient = GraphServiceClientManager.getInstance().getGraphServiceClient();

    }


    /**
     * Sends an email message using the Microsoft Graph API on Office 365. The mail is sent
     * from the address of the signed in user.
     *
     * @param emailAddress The recipient email address.
     * @param subject      The subject to use in the mail message.
     * @param body         The body of the message.
     */
    public void sendMail(
            final String emailAddress,
            final String subject,
            final String body,
            final ICallback<Void> callback
    ) {
        // create the email message
        Message message = createMessage(subject, body, emailAddress);
        mGraphServiceClient.getMe().getSendMail(message, true).buildRequest().post(callback);
}


    public void CreateMeeting(String Subject,String start, String end, final ICallback<JsonObject> callback) throws ParseException {

        Event event = createEventObject(Subject, start, end);

        mGraphServiceClient
                .getMe().getCalendar()
                .getEvents()
                .buildRequest()
                .post(event, new ICallback<Event>() {
                    @Override
                    public void success(Event event) {
                        Log.d("EVENT data", event.getRawObject().toString());

                        callback.success(event.getRawObject());
                    }

                    @Override
                    public void failure(ClientException ex) {
                        callback.failure(ex);
                    }
                });


}

    public void FindMeeting(final MeetingActivity m, final String Subject, String start, String end, final ICallback<Void> callback) {

        //Event event = createEventObject(Subject, start, end);
        //final IEventCollectionPage eventRequest;
        final List<Option> options = new LinkedList<>();

        String start1 = "'" + new StringBuilder(start).insert(start.length(), "Z'").toString();
        String end1 = "'" + new StringBuilder(end).insert(end.length(), "Z'").toString();
        //options.add(new QueryOption("$filter", "Start/DateTime ge '2017-09-18T00:00:00Z' and End/DateTime lt '2017-9-30T23:00:00Z'"));
        //options.add(new QueryOption("$filter", "Start/DateTime" + start1 + "End/DateTime" + end1));
        options.add(new QueryOption("startDateTime", start));
        options.add(new QueryOption("endDateTime", end));


        final ICallback<IEventCollectionPage> callback_ = new ICallback<IEventCollectionPage> () {
            @Override
            public void success(IEventCollectionPage result) {


                final  List<Event> ev = result.getCurrentPage();
                for (int i=0; i <= ev.size() - 1; i++ ) {
                    String id = ev.get(i).id;
                    String sbjct = ev.get(i).subject;

                    m.subjct_id.put(sbjct, id);
                    m.id_subject.put(id,sbjct);
                    Log.d("Subject, ID", sbjct + "    " + id);
                    Log.d("Sorted by", ev.get(i).start.dateTime);
                    Log.d("Sorted by", ev.get(i).end.dateTime);
                    m.categoryList.add(sbjct);  }

                // If there was more pages retrieve them too
                if (result.getNextPage() != null) {
                    result.getNextPage()
                            .buildRequest()
                            .get(new ICallback<IEventCollectionPage>() {


                                @Override
                                public void success(IEventCollectionPage result) {
                                    //JsonObject ie = iEventCollectionPage.getRawObject();
                                    final  List<Event> ev = result.getCurrentPage();
                                    for (int i=0; i <= ev.size() - 1; i++ ) {
                                        String id = ev.get(i).id;
                                        String sbjct = ev.get(i).subject;

                                        m.subjct_id.put(sbjct, id);
                                        m.id_subject.put(id,sbjct);
                                        Log.d("Subject, ID", sbjct + "    " + id);
                                        Log.d("Sorted by", ev.get(i).start.dateTime);
                                        Log.d("Sorted by", ev.get(i).end.dateTime);
                                        m.categoryList.add(sbjct);

                                    }
                                }

                                @Override
                                public void failure(ClientException ex) {
                                }
                            });
                }
                m.create_spinner(m.categoryList);
                callback.success(null);

            }

            @Override
            public void failure(ClientException ex) {

            }


        };

        mGraphServiceClient.getMe().getCalendarView().buildRequest(options).get(callback_);


//             mGraphServiceClient.getMe().getCalendarView().buildRequest(options).get(new ICallback<IEventCollectionPage>() {
//            @Override
//            public void success(IEventCollectionPage iEventCollectionPage) {
//                    //JsonObject ie = iEventCollectionPage.getRawObject();
//                    final  List<Event> ev = iEventCollectionPage.getCurrentPage();
//                    for (int i=0; i <= ev.size() - 1; i++ ) {
//                    String id = ev.get(i).id;
//                    String sbjct = ev.get(i).subject;
//
//                    m.subjct_id.put(sbjct, id);
//                    m.id_subject.put(id,sbjct);
//                    Log.d("Subject, ID", sbjct + "    " + id);
//                    Log.d("Sorted by", ev.get(i).start.dateTime);
//                    Log.d("Sorted by", ev.get(i).end.dateTime);
//                    m.categoryList.add(sbjct);
//
//                }
//
//
//
//                m.create_spinner(m.categoryList);
//                callback.success(null);
//            }
//
//            @Override
//            public void failure(ClientException ex) {
//
//            }
//        });


    }

    public void DeleteMeeting(String subject, String id, final ICallback<JsonObject> callback) {

                        mGraphServiceClient
                                .getMe().getCalendar()
                                .getEvents()
                                .byId(id)
                                .buildRequest()
                                .delete(new ICallback<Void>() {
                                    @Override
                                    public void success(Void aVoid) {
                                        callback.success(null);
                                    }

                                    @Override
                                    public void failure(ClientException ex) {
                                        callback.failure(ex);
                                    }
                                });
                    }
    public void UpdateMeeting(final String Subject, final String id, final ICallback<JsonObject> callback) {
        final List<Option> options = new LinkedList<>();
        //options.add(new QueryOption("$select", "startdatetime=2017-09-15T21:24:06.836Z &enddatetime=2017-09-25T21:24:06.836Z"));
        mGraphServiceClient.getMe().getCalendar().getEvents().byId(id).buildRequest().get(new ICallback<Event>() {
//            @Override
//            public void success(IEventCollectionPage iEventCollectionPage) {
//
//                JsonObject ie = iEventCollectionPage.getRawObject();
//                final  List<Event> ev = iEventCollectionPage.getCurrentPage();
//
//                for (int i=0; i <= ev.size() - 1; i++ ) {
//                    String id=  ev.get(i).id;
//                    String sbjct = ev.get(i).subject;
//                    Log.d("Subject, ID", sbjct + "    " + id);
//
//                    if (Subject.equals(sbjct)) {
//                        String dt= ev.get(i).end.dateTime;
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        dt = dt.substring(0, 10) + " " + dt.substring(11, 19);
//                        Date endTime = null;
//                        try {
//                            endTime = dateFormat.parse(dt);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        Calendar cal = Calendar.getInstance();
//                        cal.setTime(endTime);
//                        cal.add(Calendar.MINUTE, 30);
//                        endTime = cal.getTime();
//                        dt= dateFormat.format(endTime);
//                        dt = dt.substring(0, 10) + "T" + dt.substring(11, dt.length()) + ".0000";
//
//                        ev.get(i).end.dateTime = dt;
//                        mGraphServiceClient
//                                .getMe()
//                                .getEvents()
//                                .byId(id)
//                                .buildRequest()
//                                .patch(ev.get(i), new ICallback<Event>() {
//                                    @Override
//                                    public void success(Event event) {
//                                        callback.success(event.getRawObject());
//                                    }
//
//                                    @Override
//                                    public void failure(ClientException ex) {
//                                        callback.failure(ex);
//                                    }
//                                });
//                    }
//                }
//
//
//
//            }

            @Override
            public void success(Event ev) {


                        String dt= ev.end.dateTime;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dt = dt.substring(0, 10) + " " + dt.substring(11, 19);
                        Date endTime = null;
                        try {
                            endTime = dateFormat.parse(dt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endTime);
                        cal.add(Calendar.MINUTE, 30);
                        endTime = cal.getTime();
                        dt= dateFormat.format(endTime);
                        dt = dt.substring(0, 10) + "T" + dt.substring(11, dt.length()) + ".0000";

                        ev.end.dateTime = dt;
                        mGraphServiceClient
                                .getMe().getCalendar()
                                .getEvents()
                                .byId(id)
                                .buildRequest()
                                .patch(ev, new ICallback<Event>() {
                                    @Override
                                    public void success(Event event) {
                                        callback.success(event.getRawObject());
                                    }

                                    @Override
                                    public void failure(ClientException ex) {
                                        callback.failure(ex);
                                    }
                                });

            }

            @Override
            public void failure(ClientException ex) {

            }
        });


    }

    private static Event createEventObject(String Subject,String start, String end) throws ParseException {
        Event event = new Event();
        event.subject = Subject;
        // set start time to now


        Boolean Recurrence = true;
        if (Recurrence) {
            /// Creare reaccrence pattren
            PatternedRecurrence ptrec =  new PatternedRecurrence();
            RecurrencePattern  reacptrn = new RecurrencePattern();
            RecurrenceRange  reacrange = new RecurrenceRange();

            //// List of days in week pattren
            List <DayOfWeek> dofw = new LinkedList<>();
            dofw.add(DayOfWeek.monday);
            dofw.add(DayOfWeek.tuesday);
            dofw.add(DayOfWeek.wednesday);
            dofw.add(DayOfWeek.thursday);
            dofw.add(DayOfWeek.friday);
            dofw.add(DayOfWeek.saturday);
            dofw.add(DayOfWeek.sunday);
            reacptrn.daysOfWeek = dofw;
            reacptrn.type = RecurrencePatternType.daily;
            reacrange.recurrenceTimeZone = "Europe/Berlin";

            reacrange.startDate = DateOnly.parse(start.substring(0, 10));
            reacrange.endDate = DateOnly.parse(end.substring(0,10));
            reacrange.type = RecurrenceRangeType.endDate;

            reacptrn.interval = 1;
             // define range
            ptrec.range= reacrange;
            ptrec.pattern = reacptrn;
            event.recurrence = ptrec;


//            DateTimeTimeZone start_zone = new DateTimeTimeZone();
//            start_zone.dateTime = String.valueOf(DateTime.parse(start));
//            event.start = start_zone;
//
//            DateTimeTimeZone end_zone = new DateTimeTimeZone();
//            end_zone.dateTime = String.valueOf(DateTime.parse(end));
//            event.end = end_zone;
        }
        else {
            DateTimeTimeZone start_zone = new DateTimeTimeZone();
            start_zone.dateTime = String.valueOf(DateTime.parse(start));
            event.start = start_zone;

            DateTimeTimeZone end_zone = new DateTimeTimeZone();
            end_zone.dateTime = String.valueOf(DateTime.parse(end));
            event.end = end_zone;
            // set the timezone
            start_zone.timeZone = end_zone.timeZone = "Europe/Berlin";
        }

        // set a location
        Location location = new Location();
        location.displayName = "room1";
        event.location = location;

        // add attendees
        Attendee attendee = new Attendee();
        attendee.type = AttendeeType.required;
        attendee.emailAddress = new EmailAddress();
        //attendee.emailAddress.address = "irfan.ifi650@gmail.com";
        //attendee.emailAddress.address = "meetingroom@scheduledisplay.com";
        attendee.emailAddress.address = "irfanulhaqqureshi@outlook.com";
        event.attendees = Collections.singletonList(attendee);
        // add a msg
        ItemBody msg = new ItemBody();
        msg.content = "Discussin Graph SDK.";
        msg.contentType = BodyType.text;
        event.body = msg;
       // Log.d("EVENT BODY", event.getRawObject().toString());
        return event;
    }


    @VisibleForTesting
    Message createMessage(
            String subject,
            String body,
            String address) {

        if(address == null || address.isEmpty()) {
            throw new IllegalArgumentException("The address parameter can't be null or empty.");
        } else {
            // perform a simple validation of the email address
            String addressParts[] = address.split("@");
            if(addressParts.length != 2 || addressParts[0].length() == 0 || addressParts[1].indexOf('.') == -1) {
                throw new IllegalArgumentException(
                    String.format("The address parameter must be a valid email address {0}", address)
                );
            }
        }

        Message message = new Message();

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = address;

        Recipient recipient = new Recipient();
        recipient.emailAddress = emailAddress;

        message.toRecipients = Collections.singletonList(recipient);

        ItemBody itemBody = new ItemBody();
        itemBody.content = body;
        itemBody.contentType = BodyType.html;

        message.body = itemBody;

        message.subject = subject;

        return message;
    }

}
