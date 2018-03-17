package edu.ucsd.team6flashbackplayer;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DateTimeSetterDialogFragment extends DialogFragment {

    private int year, month, day; // cached time

    public DateTimeSetterDialogFragment() {
        // Required empty public constructor
    }

    public interface DateTimeSetterClosedListener {
        public void onDialogClosed();
    }

    /**
     * Pick a date
     * @return the date picker dialog created.
     */
    private DatePickerDialog getDatePicker(){

        final ZonedDateTime currSetting = AppTime.getInstance();

        return new DatePickerDialog(this.getContext(),
                (v, y, m, d) -> {

                    // cache the date info and call timePicker to pick time
                    year = y;
                    month = m;
                    day = d;

                    getTimePicker().show();
                }, currSetting.getYear(), currSetting.getMonthValue() - 1, currSetting.getDayOfMonth());
    }

    /**
     * Pick a time
     * @return the time picker dialog created
     */
    private TimePickerDialog getTimePicker() {
        final ZonedDateTime currSetting = AppTime.getInstance();

        return new TimePickerDialog(this.getContext(),
                (v, h, m) -> {

                    // set the Apptime
                    AppTime.setUseFixedTime(
                            ZonedDateTime.of(year, month, day,
                                    h, m, 0, 0, ZoneId.systemDefault()
                            ));

                }, currSetting.getHour(), currSetting.getMinute(), false);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getDatePicker();
    }

    @Override
    public void onDestroy() {
        if (getActivity() instanceof DateTimeSetterClosedListener)
            ((DateTimeSetterClosedListener) getActivity()).onDialogClosed();
        super.onDestroy();
    }
}
