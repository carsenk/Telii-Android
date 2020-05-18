package org.denarius.telii.wallet;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.annimon.stream.Stream;
import com.dd.CircularProgressButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.denarius.telii.ApplicationPreferencesActivity;
import org.denarius.telii.BuildConfig;
import org.denarius.telii.R;
import org.denarius.telii.components.emoji.EmojiImageView;
import org.denarius.telii.logging.Log;
import org.denarius.telii.util.CommunicationActions;
import org.denarius.telii.util.IntentUtils;
import org.denarius.telii.util.text.AfterTextChanged;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// JSON WEBSOCKET RPC IMPORTS
// The Client sessions package
import io.crossbar.autobahn.*;
import io.crossbar.autobahn.websocket.WebSocketConnection;
import io.crossbar.autobahn.websocket.WebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.types.ConnectionResponse;

import com.thetransactioncompany.jsonrpc2.client.*;

// The Base package for representing JSON-RPC 2.0 messages
import com.thetransactioncompany.jsonrpc2.*;

// The JSON Smart package for JSON encoding/decoding (optional)
import net.minidev.json.*;

// For creating URLs
import java.net.*;

public class WalletFragment extends Fragment {

  private View                   faq;
  private WalletViewModel        walletViewModel;
  private TextView               ViewBlockHeightText;
  private TextView               ViewCurrentHash;
  private TextView               ViewSupply;
  private TextView               ViewDiff;
  private TextView               ViewSize;
  private TextView               ViewUSD;
  private TextView               ViewBTC;
  private TextView               ViewVol;

  private volatile JsonObjectRequest      objectRequest;
  private volatile RequestQueue           requestQueue;

  private volatile JsonObjectRequest      objectRequest1;
  private volatile RequestQueue           requestQueue1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Fetch Denarius Block Height with Volley
    String URL="https://www.coinexplorer.net/api/v1/D/block/latest";

    //Denarius Market Data
    String URL2="https://api.coingecko.com/api/v3/coins/denarius?tickers=true&market_data=true&developer_data=true";

    //Create Request
    this.requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

    this.requestQueue1 = Volley.newRequestQueue(getActivity().getApplicationContext());

    this.objectRequest1 = new JsonObjectRequest(
            Request.Method.GET,
            URL2,
            null,
            new Response.Listener<JSONObject>() {
              @Override
              public void onResponse(JSONObject response) {
                //Get the Response
                double usdprice = 0;
                double btcprice = 0;
                double tvol = 0;
                //int blocksize = 0;
                try {
                  JSONObject jsonobj = response.getJSONObject("market_data").getJSONObject("current_price");

                  JSONObject jsonobj2 = response.getJSONObject("market_data").getJSONObject("total_volume");

                  usdprice = jsonobj.getDouble("usd");
                  btcprice = jsonobj.getDouble("btc");

                  tvol = jsonobj2.getDouble("usd");

                  NumberFormat df = DecimalFormat.getInstance();
                  df.setMinimumFractionDigits(2);
                  df.setMaximumFractionDigits(2);
                  df.setRoundingMode(RoundingMode.HALF_UP);

                  NumberFormat df8 = DecimalFormat.getInstance();
                  df8.setMinimumFractionDigits(8);
                  df8.setMaximumFractionDigits(8);
                  df8.setRoundingMode(RoundingMode.DOWN);

                  NumberFormat dfv = DecimalFormat.getInstance();
                  dfv.setMinimumFractionDigits(2);
                  dfv.setMaximumFractionDigits(2);
                  dfv.setRoundingMode(RoundingMode.HALF_UP);


                  updateBH2(df.format(usdprice), df8.format(btcprice), dfv.format(tvol)); //Forces update on UI Thread

                  Log.i("CoinGecko DENARIUS USD Price Response", df.format(usdprice));

                } catch (JSONException e) {
                  e.printStackTrace();
                }

                //Log.e("Rest Response", response.toString()); //this works
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                //Get the error response
                Log.e("CoinGecko Rest Error Response", error.toString());
              }
            }
    );

    //JsonObjectRequest with Response Handling
    this.objectRequest = new JsonObjectRequest(
            Request.Method.GET,
            URL,
            null,
            new Response.Listener<JSONObject>() {
              @Override
              public void onResponse(JSONObject response) {
                //Get the Response
                int blockcount = 0;
                int blocksize = 0;
                try {
                  JSONObject jsonobj = response.getJSONObject("result");

                  String currenthash = jsonobj.getString("hash");
                  blockcount = jsonobj.getInt("height");
                  String supply = jsonobj.getString("supplyOnBlock");
                  String diff = jsonobj.getString("difficulty");
                  blocksize = jsonobj.getInt("size");

                  updateBH(blockcount, currenthash, supply, diff, blocksize); //Forces update on UI Thread

                  Log.i("CoinExplorer.net Rest Block Count Response", String.valueOf(blockcount));

                } catch (JSONException e) {
                  e.printStackTrace();
                }

                //Log.i("CoinExplorer.net Rest Response", response.toString()); //this works
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                //Get the error response
                Log.e("CoinExplorer.net Rest Error Response", error.toString());
              }
            }
    );
    bhRunning = true;
    Thread t = new Thread(refresh);
    t.start();
  }
  private volatile boolean bhRunning = false; //Control Boolean for thread

  //Create Thread Runnable for data update
  public Runnable refresh = new Runnable(){
    public void run(){
      while(bhRunning){ //Refreshing loop
        requestQueue.add(objectRequest); // Adds a request to Queue
        requestQueue1.add(objectRequest1); // Adds a request1 to Queue
        try{
          Thread.sleep(15000); // Delays next request by 15 seconds
        } catch(Exception e){
          e.printStackTrace();
        }
      }
    }
  };

  //Method to interface with UI Thread
  private void updateBH(final int bh, final String hash, final String supply, final String diff, final int size){
    Runnable up = new Runnable(){
      public void run(){ //looks like the logs show its working
        ViewBlockHeightText.setText(String.valueOf(bh));
        ViewCurrentHash.setText(hash);
        ViewSupply.setText(supply);
        ViewDiff.setText(diff);
        ViewSize.setText(String.valueOf(size));
      }
    };
    if(getActivity() != null) {
      getActivity().runOnUiThread(up); // Runs above code on UI Thread
    }
  }

  private void updateBH2(final String price, final String btc, final String vol){
    Runnable up = new Runnable(){
      public void run(){ //looks like the logs show its working
        ViewUSD.setText("$" + price);
        ViewBTC.setText(btc + " BTC");
        ViewVol.setText("$" + vol);
      }
    };
    if(getActivity() != null) {
      getActivity().runOnUiThread(up); // Runs above code on UI Thread
    }
  }

  @Override
  public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    ConstraintLayout l = (ConstraintLayout) inflater.inflate(R.layout.wallet_fragment, container, false);
    ViewBlockHeightText = l.findViewById(R.id.d_blockheight);
    ViewCurrentHash = l.findViewById(R.id.d_hash);
    ViewSupply = l.findViewById(R.id.d_supply);
    ViewDiff = l.findViewById(R.id.d_diff);
    ViewSize = l.findViewById(R.id.d_size);
    ViewUSD = l.findViewById(R.id.d_usd);
    ViewBTC = l.findViewById(R.id.d_btc);
    ViewVol = l.findViewById(R.id.d_vol);
    return l;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    initializeViewModels();
    initializeViews(view);
    initializeListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) getActivity()).getSupportActionBar().setTitle(R.string.preferences__dstats);

  }

  private void initializeViewModels() {
    walletViewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
  }

  private void initializeViews(@NonNull View view) {
    faq              = view.findViewById(R.id.wallet_fragment_faq);
  }

  private void initializeListeners() {
    faq.setOnClickListener(v -> launchFaq());
  }

  private void launchFaq() {
    Uri    data   = Uri.parse("https://denarius.io");
    Intent intent = new Intent(Intent.ACTION_VIEW, data);

    startActivity(intent);
  }

}

