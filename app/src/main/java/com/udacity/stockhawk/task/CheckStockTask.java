package com.udacity.stockhawk.task;

import android.content.Context;
import android.os.AsyncTask;

import com.udacity.stockhawk.R;

import java.io.IOException;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

// Check if stock exists
public class CheckStockTask extends AsyncTask<String, Void, Boolean> {

    private Context context = null;

    public CheckStockTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... stocks) {

        Boolean stockExists = false;
        String stockSymbol = stocks[0];

        if (stockSymbol == null || stockSymbol.length() == 0)
            return false;

        try {
            Stock stock = YahooFinance.get(stockSymbol);
            if (stock.getQuote().getPrice() != null) {
                stockExists = true;
            }
        } catch (IOException exception) {
            String message = context.getString(R.string.error_invalid_stock_symbol, stockSymbol);
            Timber.e(message);
        }

        return stockExists;
    }
}
