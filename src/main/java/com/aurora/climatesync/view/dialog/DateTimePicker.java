package com.aurora.climatesync.view.dialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateTimePicker extends JPanel {
    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;

    public DateTimePicker(LocalDateTime initialDateTime) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        // Date Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "EEE, MMM dd, yyyy");
        dateSpinner.setEditor(dateEditor);
        
        // Style the spinner to look a bit more modern/flat if possible, or just standard
        // Removing border to make it look cleaner?
        // dateSpinner.setBorder(BorderFactory.createEmptyBorder());

        // Time Spinner
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);

        setDateTime(initialDateTime);

        add(dateSpinner);
        add(Box.createHorizontalStrut(10));
        add(timeSpinner);
    }

    public LocalDateTime getDateTime() {
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        // Combine
        Calendar resultCal = Calendar.getInstance();
        resultCal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
        resultCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
        resultCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
        resultCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        resultCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        resultCal.set(Calendar.SECOND, 0);
        resultCal.set(Calendar.MILLISECOND, 0);

        return LocalDateTime.ofInstant(resultCal.toInstant(), ZoneId.systemDefault());
    }

    public void setDateTime(LocalDateTime dateTime) {
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(date);
        timeSpinner.setValue(date);
    }
}
