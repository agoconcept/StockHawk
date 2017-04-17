package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;


public class AddStockDialog extends DialogFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }
        });
        builder.setView(custom);

        builder.setMessage(getString(R.string.dialog_title));
        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    private void addStock() {

        Activity parent = getActivity();
        String stockSymbol = stock.getText().toString();

        // Check if the stock symbol is valid in another thread, because it needs
        // to request it over the network
        AsyncTask task = new CheckStockTask().execute(stockSymbol);

        try {
            // Wait until the request finishes
            Boolean stockExists = (Boolean) task.get();
            if (stockExists) {
                if (parent instanceof MainActivity) {
                    ((MainActivity) parent).addStock(stockSymbol);
                }
            } else {
                Timber.e("Stock \"%s\" not found", stockSymbol);
                String message = getString(R.string.error_invalid_stock_symbol, stockSymbol);
                Toast.makeText(parent, message, Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException ie) {
            Timber.w("Operation interrupted while trying to retrieve stock");
        } catch (ExecutionException ee) {
            Timber.w("Operation failed while trying to retrieve stock");
        } finally {
            dismissAllowingStateLoss();
        }
    }

    // Get stock in another thread
    private class CheckStockTask extends AsyncTask<String, Void, Boolean> {

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
                String message = getString(R.string.error_invalid_stock_symbol, stockSymbol);
                Timber.e(message);
            }

            return stockExists;
        }
    }
}
