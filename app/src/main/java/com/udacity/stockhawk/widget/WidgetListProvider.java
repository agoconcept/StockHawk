package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by egalsan on 01/05/2017.
 */

public class WidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;

    private List<List> stocksList;

    WidgetListProvider(Context context, Intent intent) {
        this.context = context;

        stocksList = new ArrayList<>();
    }

    private void initData() {
        stocksList.clear();

        Cursor cursor = context.getContentResolver().query(
                Contract.Quote.URI,
                null,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);

        // Iterate for all elements retrieved from the DB and add them to the list of movies
        while (cursor.moveToNext()) {

            List<String> item = new ArrayList<>();

            String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            item.add(symbol);

            String price = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            item.add(price);

            String absoluteChange = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
            item.add(absoluteChange);

            String percentageChange = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
            item.add(percentageChange);

            stocksList.add(item);
        }

        cursor.close();
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {
        // Empty
    }

    @Override
    public int getCount() {
        return stocksList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);

        // Fill in the data
        List<String> item = stocksList.get(i);

        String symbol = item.get(0);
        String price = item.get(1);
        String absoluteChange = item.get(2);
        String percentageChange = item.get(3);

        views.setTextViewText(R.id.symbol, symbol);
        views.setTextViewText(R.id.price, price);
        views.setTextViewText(R.id.change, absoluteChange);

        views.setTextColor(R.id.symbol, context.getResources().getColor(R.color.white));
        views.setTextColor(R.id.price, context.getResources().getColor(R.color.white));
        views.setTextColor(R.id.change, context.getResources().getColor(R.color.white));

        // Add intent info
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(StockActivity.STOCK_SYMBOL, symbol);
        fillInIntent.putExtra(StockActivity.STOCK_PRICE, price);
        fillInIntent.putExtra(StockActivity.STOCK_ABSOLUTE_CHANGE, absoluteChange);
        fillInIntent.putExtra(StockActivity.STOCK_PERCENTAGE_CHANGE, percentageChange);
        views.setOnClickFillInIntent(R.id.quote_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}
