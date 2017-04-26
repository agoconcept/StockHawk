package com.udacity.stockhawk.data;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by egalsan on 26/04/2017.
 */

public class MPChartHelper {

    public static List<Entry> populateData(List<HistoricalQuote> history) {

        List<Entry> entries = new ArrayList<>();

        for (HistoricalQuote data : history) {
            // Add datapoint
            entries.add(new Entry(history.indexOf(data), data.getClose().floatValue(), data.getDate()));
        }

        return entries;
    }

    public static String[] getXLabels(List<HistoricalQuote> history) {

        java.text.DateFormat formatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.SHORT);

        List<String> labels = new ArrayList<>();

        for (HistoricalQuote data : history) {

            Calendar calendar = data.getDate();

            formatter.setTimeZone(calendar.getTimeZone());
            String formatted = formatter.format(calendar.getTime());

            // Add datapoint
            labels.add(formatted);
        }

        return labels.toArray(new String[0]);
    }
}
