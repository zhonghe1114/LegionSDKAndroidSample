package legionrealm.legionsdksample.elite;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lenovo.legionrealm.opensdk.LegionOpenSdk;
import com.lenovo.legionrealm.opensdk.common.LegionCallback;
import com.lenovo.legionrealm.opensdk.common.PaymentCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private View mLayoutInit;
    private View mLayoutProducts;

    private TextView mTvTips;
    private Button mBtnVerify;
    private Button mBtnProducts;
    private Button mBtnPurchased;
    private RecyclerView mRvProducts;
    //The LegionOpenSdk created a singleton
    private List<Product> mProductsList = new ArrayList<>();
    private List<OrderBean> mOrdersList = new ArrayList<>();
    private ProgressDialog progressDialog;

    private final int TYPE_ERROR = 0;

    private final int TYPE_LOGIN = 1;

    private final int TYPE_VERIFY = 2;

    private final int TYPE_QUERY_PRODUCT = 3;

    private final int TYPE_QUERY_RECORD = 4;

    private LegionCallback mInitCallback = new LegionCallback() {
        @Override
        public void onSuccess(String response) {
            displayResult(TYPE_LOGIN, response);
            hideProgress();
        }

        @Override
        public void onError(String error) {
            displayResult(TYPE_ERROR, error);
            hideProgress();
        }
    };

    private PaymentCallback mPaymentCallback = new PaymentCallback() {
        @Override
        public void onComplete(String json) {
            //Handle success case
            try {
                /**
                 * 1、purchase success，then please distribute goods to player.
                 * 2、When player get product, you must call consumePurchase();
                 */
                JSONObject obj = new JSONObject(json);
                JSONObject jsonObject = obj.getJSONObject("data");
                Gson gson = new Gson();
                OrderBean orderBean = gson.fromJson(jsonObject.toString(), OrderBean.class);
                showToast(MainActivity.this, "Purchasing successful，Consuming...[purchase_id:" + orderBean.getPurchase_id() + ",token:" + orderBean.getToken());
                showPaymentDialog((dialog, which) -> {
                    showProgress();
                    consumePurchase(orderBean.getPurchase_id(), orderBean.getToken());
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            showToast(MainActivity.this, "Error:" + error);
        }

        @Override
        public void onCancel() {
            showToast(MainActivity.this, "Be canceled");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("LegionOpenSdk: Initialize  v"+ BuildConfig.VERSION_NAME);

        initView();
    }

    private void initView() {
        Button btnInit = this.findViewById(R.id.btn_init);
        mBtnProducts = this.findViewById(R.id.btn_products);
        mLayoutInit = this.findViewById(R.id.layout_init);
        mTvTips = this.findViewById(R.id.tv_tips);
        mBtnVerify = this.findViewById(R.id.btn_verify);
        mBtnPurchased = this.findViewById(R.id.btn_purchased);
        mRvProducts = this.findViewById(R.id.rv_products);
        mLayoutProducts = this.findViewById(R.id.ly_function);
        mBtnPurchased = this.findViewById(R.id.btn_purchased);
        mRvProducts = this.findViewById(R.id.rv_products);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvProducts.setLayoutManager(layoutManager);

        btnInit.setOnClickListener(v -> {

            showProgress();
            // you can use this way to init LegionOpenSdk.
            // you must provide params in AndroidManifest.xml
            LegionOpenSdk.initialize(mInitCallback, this);

        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            try {
                LegionOpenSdk.getInstance().logout(new LegionCallback() {
                    @Override
                    public void onSuccess(String response) {
                        showToast(MainActivity.this, response);
                    }

                    @Override
                    public void onError(String error) {
                        showToast(MainActivity.this, error);

                    }
                }, MainActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(MainActivity.this, e.getMessage());
            }
        });

        findViewById(R.id.btn_verify).setOnClickListener(v -> {
            showProgress();
            LegionOpenSdk.getInstance().checkLicense(new LegionCallback() {

                @Override
                public void onSuccess(String info) {
                    hideProgress();
                    // verification succeed
                    displayResult(TYPE_VERIFY, info);
                }

                @Override
                public void onError(String error) {
                    hideProgress();
                    // For unknown reason, verification cannot be done.
                    // Please disallow access for proper protection.
                    displayResult(TYPE_VERIFY, error);
                }
            });
        });

        findViewById(R.id.btn_products).setOnClickListener(v -> {
            showProgress();
            LegionOpenSdk.getInstance().queryProducts(new LegionCallback() {
                @Override
                public void onSuccess(String result) {
                    hideProgress();
                    displayResult(TYPE_QUERY_PRODUCT, result);
                }

                @Override
                public void onError(String error) {
                    displayResult(TYPE_ERROR, error);
                    hideProgress();
                }
            });
        });

        findViewById(R.id.btn_purchased).setOnClickListener(v -> {
            showProgress();
            LegionOpenSdk.getInstance().restorePurchases(new LegionCallback() {
                @Override
                public void onSuccess(String result) {
                    hideProgress();
                    displayResult(TYPE_QUERY_RECORD, result);
                }

                @Override
                public void onError(String error) {
                    hideProgress();
                    displayResult(TYPE_ERROR, error);
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void consumePurchase(String purchase_id, String token) {
        LegionOpenSdk.getInstance().consume(new LegionCallback() {
            @Override
            public void onSuccess(String response) {
                hideProgress();
                showToast(MainActivity.this, "Consumption successful!");
                Log.d("mLegionOpenSdk", "response = "+response);
                LegionOpenSdk.getInstance().closePaymentUI();
            }

            @Override
            public void onError(String error) {
                hideProgress();
                Log.e("mLegionOpenSdk", "error = "+error);
                showToast(MainActivity.this, "Consuming error:" + error);
            }
        }, purchase_id, token);
    }

    /**
     * show toast info
     * @param context
     * @param text
     */
    private void showToast(Context context, CharSequence text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * @param type which type info
     * @param result
     */
    private void displayResult(int type, final String result) {
        Log.d(TAG,"type = "+type+", result = "+result);
        showDialog(result, (dialog, which) -> {
            switch (type) {
                case TYPE_LOGIN:
                    showInitSuccessView();
                    break;
                case TYPE_QUERY_PRODUCT:
                    parseProducts(result);
                    break;
                case TYPE_QUERY_RECORD:
                    parseRecords(result);
                    break;
            }
        });
    }

    /**
     * parse products list info
     * @param result
     */
    private void parseProducts(String result) {
        Log.d(TAG, "result："+result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray != null) {
                mProductsList.clear();
                mProductsList = parseString2List(dataArray.toString(), Product.class);
                ProductsAdapter adapter = new ProductsAdapter(this, mProductsList);
                mRvProducts.setAdapter(adapter);
                adapter.setOnItemClickListener(position -> {
                    final Product product = mProductsList.get(position);
                    LegionOpenSdk.getInstance().purchase(mPaymentCallback, MainActivity.this, product.getProduct_id(), "cporderid----", "dev-0110");
                });

                showProducts("Products");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, e.getMessage());
        }
    }

    /**
     * parse records list
     * @param result
     */
    private void parseRecords(String result) {
        try {

            JSONObject obj = new JSONObject(result);
            JSONArray dataArray = obj.getJSONArray("data");

            if (dataArray != null) {
                mOrdersList = parseString2List(dataArray.toString(), OrderBean.class);
                OrdersAdapter adapter = new OrdersAdapter(this, mOrdersList);
                mRvProducts.setAdapter(adapter);
                adapter.setOnItemClickListener(position -> {
                    showProgress();
                    final OrderBean orderBean = mOrdersList.get(position);
                    showDialog("consume this order? ",  (dialog, which) -> consumePurchase(orderBean.getPurchase_id(), orderBean.getToken()));
                });
                showProducts("Records");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, e.getMessage());
        }
    }

    private void showInitView() {
        setTitle("Legion Initialize: V"+ BuildConfig.VERSION_NAME);
        mLayoutInit.setVisibility(View.VISIBLE);
        mLayoutProducts.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void showInitSuccessView() {
        setTitle("functions");
        mRvProducts.setVisibility(View.GONE);
        mLayoutInit.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.VISIBLE);
        mTvTips.setVisibility(View.VISIBLE);
        mBtnVerify.setVisibility(View.VISIBLE);
        mBtnProducts.setVisibility(View.VISIBLE);
        mBtnPurchased.setVisibility(View.VISIBLE);
    }

    private void showProducts(String title) {
        setTitle(title);
        mTvTips.setVisibility(View.GONE);
        mBtnVerify.setVisibility(View.GONE);
        mBtnProducts.setVisibility(View.GONE);
        mBtnPurchased.setVisibility(View.GONE);
        mRvProducts.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayoutProducts.getVisibility() == View.VISIBLE) {
            if (mRvProducts.getVisibility() == View.VISIBLE) {
                showInitSuccessView();
            } else {
                showInitView();
            }
        } else {
            finish();
        }
    }

    private void showDialog(String title, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        if (title != null) {
            builder.setMessage(title);
        }
        builder.setPositiveButton("Ok", listener);
        AlertDialog dialog = builder.create();
        builder.setCancelable(false);
        dialog.show();
    }

    private void showPaymentDialog(DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("After the payment is successful, the goods need to be distributed. After the goods are successfully distributed, LegionOpenSdk.getInstance().consume() needs to be called");
        builder.setPositiveButton("consume", listener);
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        builder.setCancelable(false);
        dialog.show();
    }

    /**
     * @return
     */
    private  <T> List<T> parseString2List(String json, Class clazz) {
        Type type = new ParameterizedTypeImpl(clazz);
        List<T> list =  new Gson().fromJson(json, type);
        return list;
    }

    class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}