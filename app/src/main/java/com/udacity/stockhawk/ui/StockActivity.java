package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.MPChartHelper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class StockActivity extends AppCompatActivity {

    public static final String STOCK_SYMBOL = "STOCK_SYMBOL";
    public static final String STOCK_PRICE = "STOCK_PRICE";
    public static final String STOCK_ABSOLUTE_CHANGE = "STOCK_ABSOLUTE_CHANGE";
    public static final String STOCK_PERCENTAGE_CHANGE = "STOCK_PERCENTAGE_CHANGE";

    @BindView(R.id.stock_constraint_layout)
    ConstraintLayout constraintLayout;

    @BindView(R.id.tv_stock_symbol)
    TextView tvStockSymbol;

    @BindView(R.id.tv_stock_price)
    TextView tvStockPrice;

    @BindView(R.id.tv_stock_absolute_change)
    TextView tvStockAbsoluteChange;

    @BindView(R.id.tv_stock_percentage_change)
    TextView tvStockPercentageChange;

    @BindView(R.id.lc_stock_historic_graph)
    LineChart lcStockHistoricGraph;

    @BindView(R.id.tv_stock_open)
    TextView tvStockOpen;

    @BindView(R.id.tv_stock_high)
    TextView tvStockHigh;

    @BindView(R.id.tv_stock_low)
    TextView tvStockLow;

    @BindView(R.id.tv_stock_avg_volume)
    TextView tvStockAvgVolume;

    @BindView(R.id.tv_stock_52wk_high)
    TextView tvStock52wkHigh;

    @BindView(R.id.tv_stock_52wk_low)
    TextView tvStock52wkLow;

    private String symbol;
    private String price;
    private String absoluteChange;
    private String percentageChange;

    private final DecimalFormat dollarFormat;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat percentageFormat;

    public StockActivity() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        ButterKnife.bind(this);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        symbol = intent.getStringExtra(STOCK_SYMBOL);
        price = intent.getStringExtra(STOCK_PRICE);
        absoluteChange = intent.getStringExtra(STOCK_ABSOLUTE_CHANGE);
        percentageChange = intent.getStringExtra(STOCK_PERCENTAGE_CHANGE);

        setStockData();
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void setStockData() {

        tvStockSymbol.setText(symbol);

        tvStockPrice.setText(price);

        float fAbsoluteChange = Float.parseFloat(absoluteChange);
        float fPercentageChange = Float.parseFloat(percentageChange);

        if (fAbsoluteChange > 0.0f) {
            tvStockAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            tvStockAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
        tvStockAbsoluteChange.setText(dollarFormatWithPlus.format(fAbsoluteChange));

        if (fPercentageChange > 0.0f) {
            tvStockPercentageChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            tvStockPercentageChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
        tvStockPercentageChange.setText(percentageFormat.format(fPercentageChange/100.0f));

        if (!networkUp()) {
            Snackbar.make(constraintLayout, getString(R.string.toast_no_connectivity), Snackbar.LENGTH_LONG).show();
        } else {
            // Get the rest of the data
            new GetStockInfoTask().execute(symbol);
        }
    }

    // Get stock information
    // Move it to a separate file?
    private class GetStockInfoTask extends AsyncTask<String, Void, Boolean> {

        String stockSymbol = null;
        Stock stock = null;

        @Override
        protected Boolean doInBackground(String... stocks) {

            Boolean stockExists = false;
            stockSymbol = stocks[0];

            if (stockSymbol == null || stockSymbol.length() == 0)
                return false;

            try {
                // Retrieve stock information with historical data
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();
                from.add(Calendar.YEAR, -1); // 1 year

                stock = YahooFinance.get(stockSymbol, from, to, Interval.WEEKLY);
                if (stock.getQuote().getPrice() != null) {
                    stockExists = true;
                }
            } catch (IOException exception) {
                String message = getString(R.string.error_invalid_stock_symbol, stockSymbol);
                Timber.e(message);
            }

            return stockExists;
        }

        @Override
        protected void onPostExecute(Boolean stockExists) {
            super.onPostExecute(stockExists);

            if (! stockExists || stock == null) {
                Timber.e("Quote not found: ", stockSymbol);
                String message = getString(R.string.error_invalid_stock_symbol, stockSymbol);
                Toast.makeText(StockActivity.this, message, Toast.LENGTH_SHORT).show();
                return;
            }

            StockQuote stockQuote = stock.getQuote();

            float open = stockQuote.getOpen().floatValue();
            float high = stockQuote.getDayHigh().floatValue();
            float low = stockQuote.getDayLow().floatValue();
            Long avgVolume = stockQuote.getAvgVolume();
            float yearHigh = stockQuote.getYearHigh().floatValue();
            float yearLow = stockQuote.getYearLow().floatValue();

            // Set data
            tvStockOpen.setText(dollarFormat.format(open));
            tvStockHigh.setText(dollarFormat.format(high));
            tvStockLow.setText(dollarFormat.format(low));
            tvStockAvgVolume.setText(avgVolume.toString());
            tvStock52wkHigh.setText(dollarFormat.format(yearHigh));
            tvStock52wkLow.setText(dollarFormat.format(yearLow));

            // Set chart data
            try {

                // Get historical data and reverse it
                List<HistoricalQuote> list = stock.getHistory();
                Collections.reverse(list);

                setChart(list);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void setChart(List<HistoricalQuote> list) {

            List<Entry> entries = MPChartHelper.populateData(list);
            String [] labels = MPChartHelper.getXLabels(list);

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stock_price));

            final String[] xLabels = labels;

            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return xLabels[(int) value];
                }
            };

            XAxis xAxis = lcStockHistoricGraph.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);
            xAxis.setTextColor(getColor(R.color.white));

            YAxis leftAxis = lcStockHistoricGraph.getAxisLeft();
            leftAxis.setTextColor(getColor(R.color.white));

            YAxis rightAxis = lcStockHistoricGraph.getAxisRight();
            rightAxis.setTextColor(getColor(R.color.white));

            dataSet.setColor(getColor(R.color.colorAccent));
            dataSet.setCircleColor(getColor(R.color.colorAccent));
            dataSet.setValueTextColor(getColor(R.color.white));
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineData lineData = new LineData(dataSet);
            lcStockHistoricGraph.setData(lineData);

            Description description = new Description();
            description.setText(getString(R.string.chart_history));
            description.setTextColor(getColor(R.color.white));
            lcStockHistoricGraph.setDescription(description);

            Legend legend = lcStockHistoricGraph.getLegend();
            legend.setTextColor(getColor(R.color.white));

            lcStockHistoricGraph.invalidate();
        }
    }
}
