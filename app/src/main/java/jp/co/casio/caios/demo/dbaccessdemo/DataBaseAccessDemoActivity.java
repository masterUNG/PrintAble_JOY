package jp.co.casio.caios.demo.dbaccessdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import jp.co.casio.caios.framework.device.LinePrinter;
import jp.co.casio.caios.framework.device.lineprintertools.BuildinEx840;
import jp.co.casio.caios.framework.device.lineprintertools.LinePrinterDeviceBase;

@SuppressWarnings("ALL")
public class DataBaseAccessDemoActivity extends Activity implements View.OnClickListener {

    //Explicit
    private Handler handler = new Handler();
    private final static String PROVIDER = "jp.co.casio.caios.framework.database";
    private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
    private String ItemName, QTY, AMT;
    private String[] ITEMNAMEStrings;
    private String[] qty;
    private String[] amt;
    private int intItemname, intQty, intAmt;
    private CheckBox myCheckBox;
    public boolean bolStatusCheck = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        myCheckBox = (CheckBox) findViewById(R.id.checkBox);

        Resources resources = getResources();

        EditText et;

        // 検索時間 クリア
        TextView textView_time;
        textView_time = (TextView) findViewById(R.id.textView_time);
        textView_time.setText("?" + resources.getString(R.string.add_time));

        // 検索件数 クリア
        TextView textView_cnt;
        textView_cnt = (TextView) findViewById(R.id.textView_count);
        textView_cnt.setText("?" + resources.getString(R.string.add_count));

        // 各ボタンにリスナーを設定
        Button button;
        button = (Button) findViewById(R.id.button_finish);
        button.setOnClickListener(this);

        button = (Button) findViewById(R.id.button_execution);
        button.setOnClickListener(this);

        //**************************************************************************
        //*******  Print Test Button
        //**************************************************************************

        Button btnTestPrint = (Button) findViewById(R.id.button);
        btnTestPrint.setOnClickListener(this);

    }//main method


    @Override
    public void onClick(View v) {
        // ソフトキーボード非表示
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean r = inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        switch (v.getId()) {
            case R.id.button_finish:
                if (!r) {
                    // ソフトキーボードが非表示状態なら、デモアプリを終了
                    finish();
                }
                break;
            case R.id.button_execution:

                checkStatusCheckBox();

                new queryTask(this).execute();
                break;
            case R.id.button:
                Log.d("joy", "Click testprint");


//  ========= ปริ้นจากตรงนี้
//  testPrintBuildIn(myResult); // Print Head ==============================

                TextView masterTextView = (TextView) findViewById(R.id.txtMaster);
                String myResult = masterTextView.getText().toString();  //Get Date Bill
                Log.d("myMaster", "myResult  ++++++> " + myResult);

//                PrintHeader("Products Report");
//                PrintHeader("======================================");
//                PrintHeader(myResult);
//                PrintHeader("======================================");
//  ================================================================================

// ====================  Printer data (myValue);=================================
                TextView valueTextView = (TextView) findViewById(R.id.txtValue);
                String myValue = valueTextView.getText().toString();
                TextView qtyTextView = (TextView) findViewById(R.id.txtQty);
                String qtyResult = qtyTextView.getText().toString();
                Log.d("28Dec", "myResult  ++++++> " + qtyResult);

                testPrintBuildIn("======================================" + "\n" +
                        myResult + "\n" +
                        "======================================" + "\n"  +
                        myValue + "\n" +
                        "       ...............................   "); //print data

                break;
        }   // switch
    }  // onClick

    public boolean checkStatusCheckBox() {

        //เมธอท ตรวจสอบการ CheckBox
        if (myCheckBox.isChecked()) {
            bolStatusCheck = true;
        } else {
            bolStatusCheck = false;
        }

        Log.d("CheckBox", "Status ==> " + bolStatusCheck);

        return bolStatusCheck;
    }

    private void PrintHeader(String strHead) {
        LinePrinterDeviceBase linePrinterDevice = new BuildinEx840();
        linePrinterDevice.setCharacterSet(LinePrinterDeviceBase.MULTICHARMODE_TIS620);
        linePrinterDevice.setPageMode(LinePrinterDeviceBase.PAGEMODE_TIS620);
        linePrinterDevice.setMulticharMode(LinePrinterDeviceBase.MULTICHARMODE_TIS620);


        LinePrinter linePrinter = new LinePrinter();
        linePrinter.open(linePrinterDevice);
        Log.d("print ", "open");

//        linePrinter.printNormal("\u001b|2C");// Vertical x 2, Holizontal x 2 ตัวหนา
        linePrinter.printNormal(strHead + "\n");


//        linePrinter.printNormal("\u001b|N\r\n");// return to normal คืนค่า ตัวปกติ
        linePrinter.close();

    }

    private void testPrintBuildIn(String strPrint) {

        LinePrinterDeviceBase device = new BuildinEx840();
        device.setCharacterSet(LinePrinterDeviceBase.MULTICHARMODE_TIS620);
        // International character set.
        device.setPageMode(LinePrinterDeviceBase.PAGEMODE_TIS620);
        // Character table.
        device.setMulticharMode(LinePrinterDeviceBase.MULTICHARMODE_TIS620);


        LinePrinter linePrinter = new LinePrinter();
        linePrinter.open(device);


        linePrinter.printNormal("\u001b|lF");// Feed 1 line

        linePrinter.printNormal(strPrint+ "\u001B|tbC"+ "\n"); //print data

        linePrinter.printNormal("\u001b|2lF");//Feed 4line

        linePrinter.printNormal("\u001b|fP");//Paper cut

        linePrinter.close();


    }   // TestPrintBuidIN


    private static class queryTask extends AsyncTask<Void, Integer, TableLayout> {
        private Activity mActivity;
        private ProgressDialog mProgressDialog = null;
        private String myHeadResultString, myValueFinalString,myValueFinalString2,myValueFinalString3,
                finalResulttoPrintString,qtyfinalprintString,amtfinalprintString;
        private boolean bolMyStatus = false;

        public queryTask(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            Resources resources = mActivity.getResources();

            // 検索時間 クリア
            TextView textView_time;
            textView_time = (TextView) mActivity.findViewById(R.id.textView_time);
            textView_time.setText("?" + resources.getString(R.string.add_time));

            // 検索件数 クリア
            TextView textView_cnt;
            textView_cnt = (TextView) mActivity.findViewById(R.id.textView_count);
            textView_cnt.setText("?" + resources.getString(R.string.add_count));

            // 検索結果 クリア
            LinearLayout linearLayout_result;
            linearLayout_result = (LinearLayout) mActivity.findViewById(R.id.LinearLayout_result);
            linearLayout_result.removeAllViews();
        }

        @Override
        protected TableLayout doInBackground(Void... arg0) {
            String db = "SUMMARYBACK.DB";
            String table = "CSS034";
            String projection[] = {"ITEMNAME", "QTY", "AMT"};
            String selection = null; // BIZDATE='20151211" บังคับใส่แบบนี้
            String selectionArgs[] = null;
            String sortOrder = null;

            // 検索結果格納用テーブル
            TableLayout tableLayout_result;
            tableLayout_result = new TableLayout(mActivity);

            // データベース名 取得
            EditText et;

            //My CheckBox
           CheckBox myCheckBox = (CheckBox) mActivity.findViewById(R.id.checkBox);
            if (myCheckBox.isChecked()) {
                bolMyStatus = true;
            }


            // EditText รับค่า บิลจากวันเดือนปี
            et = (EditText) mActivity.findViewById(R.id.editText_selection);
            selection = "BIZDATE= '" + et.getText().toString() + "'";
            if (selection.length() == 0) {
                selection = null;
            }

            // URI作成
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("content");
            builder.authority(PROVIDER);
            builder.appendPath("SUMMARYBACK");
            builder.appendPath("CSS034");
            Uri uri = builder.build();

            // 検索してカーソルを取得する
            long time = 0;
            Cursor cursor = null;
            try {
                // 検索開始時間
                time = System.currentTimeMillis();

                //-------------------------------------------Start
                // コンテンツプロバイダーからデータを取得する
                cursor = mActivity.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                //-------------------------------------------End


                // 検索終了時間
                time = System.currentTimeMillis() - time;
            } catch (Exception e) {
                // 検索失敗
            }
            if (cursor == null) {
                return null;
            }

            // 検索結果を表示領域に設定
            int countMax = cursor.getCount();
            publishProgress(0, countMax, (int) time);
            if (countMax > 0) {
                int columnCount = cursor.getColumnCount();
                // 先頭の５列までを表示する
                if (columnCount > 3) {
                    columnCount = 3;
                }
                tableLayout_result.setColumnStretchable(columnCount - 1, true);

                String[] columnName = new String[columnCount];
                int[] columnIndex = new int[columnCount];
                TableRow tableRow = new TableRow(mActivity);
                String[] itemname = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    columnName[i] = cursor.getColumnName(i);
                    columnIndex[i] = cursor.getColumnIndex(columnName[i]);

                    //   #####################################################
                    //        Get String Heading
                    //   #####################################################

                    myHeadResultString = "     " + columnName[0] + "     " + columnName[1] + "     " + columnName[2];

                    Log.d("28Dec", "myResultString ==> " + myHeadResultString);

                    TextView str = new TextView(mActivity);
                    str.setText(columnName[i] + " ");
                    str.setTextSize(24.0f);
                    str.setTextColor(Color.WHITE);
                    str.setBackgroundColor(Color.RED);
                    tableRow.addView(str);
                }
                tableLayout_result.addView(tableRow, new TableLayout.LayoutParams(FP, WC));

                int count = 0;

                String[] itemnameStrings = new String[countMax]; //ค่าของ itemname
                String[] qtyStrings = new String[countMax];
                String[] amtStrings = new String[countMax];
                String[] valueOfs = new String[columnCount];

                if (cursor.moveToFirst()) {
                    do {
                        count++;
                        publishProgress(count, countMax);
                        tableRow = new TableRow(mActivity);

                        for (int i = 0; i < columnCount; i++) {
                            String s = cursor.getString(columnIndex[i]);

                            //   #####################################################
                            //        Get String Value
                            //   #####################################################

                            valueOfs[i] = s;

                            //   #####################################################
                            //        ค่าที่ต้องการ พิมพ์ ออกบิล
                            //   #####################################################

                            myValueFinalString = createSpace(5) + valueOfs[0] + createSpace(20 - (valueOfs[0].length())) + createSpaceString(valueOfs[1], 5) +
                                    valueOfs[1] + createSpace(3) + createSpaceString(checkDecimal(valueOfs[2]), 10) +
                                    checkDecimal(valueOfs[2]);




                            if (i == 2) {

                                itemnameStrings[count - 1] = myValueFinalString;
                                Log.d("8Feb", "valueStrings[" + Integer.toString(count - 1) + "]" + itemnameStrings[count - 1]);
                                Log.d("8Feb", "Lengh = " + valueOfs[0].length());
                            }

//
//==================================================================================================================================
                            TextView str = new TextView(mActivity);
                            str.setText(s + " ");
                            str.setTextSize(24.0f);
                            tableRow.addView(str);
                        }   // for
                        tableLayout_result.addView(tableRow, new TableLayout.LayoutParams(FP, WC));
                    } while (cursor.moveToNext());

                    //Create itemname
                    StringBuilder objStringBuilder = new StringBuilder();
                    for (int i = 0; i < itemnameStrings.length; i++) {
                        objStringBuilder.append(itemnameStrings[i] + "\n");
                    }
                    finalResulttoPrintString = objStringBuilder.toString();
                    Log.d("29Dec", "Finial ==>>>> " + finalResulttoPrintString);

//                    for (int ii = 0; ii < qtyStrings.length; ii++) {
//                        objStringBuilder.append(qtyStrings[ii] + "\n");
//                    }
//                    qtyfinalprintString = objStringBuilder.toString();
//                    Log.d("29Dec", "Finialqty ==>>>> " + qtyfinalprintString);


                }   // if

            }//if
            cursor.close();

            return tableLayout_result;
        }//TableLayout doInBackground

        private String checkDecimal(String valueOf2) {

            String result = null;

            if (bolMyStatus) {
                if (valueOf2 != null) {
                    double douValueOf = Double.parseDouble(valueOf2);
                    double douAnswer = douValueOf / 100;

                    //result = Double.toString(douAnswer);
                    result = String.format("%.2f", douAnswer);

                    Log.d("result1", "result == " + result);
                } else {
                    Log.d("result1", "result == " + "Null");
                }   // if
            } else {
                result = valueOf2;
            }


            return result;
        }

        private String createSpaceString(String strWord, int intSpace) {

            String strResult = " ";

            if (strWord != null) {
                strResult = createSpace(intSpace - strWord.length());
            } else {
                strResult = createSpace(intSpace);
            }


            return strResult;
        }

        private String myStringBuilder(String valueOf, String valueOf1, String valueOf2) {

            String strResult = null;

            strResult = createSpace(5) + valueOf + createSpace(30-valueOf.length()) + valueOf1 + createSpace(5);

            return strResult;
        }

        private String createSpace(int intSpace) {

            String strSpce = null;
            StringBuilder objStringBuilder = new StringBuilder();

            for (int i=0;i<intSpace;i++) {
                objStringBuilder.append(" ");
            }

            strSpce = objStringBuilder.toString();

            return strSpce;
        }


        @Override
        protected void onPostExecute(TableLayout result) {
            if (result != null) {
                // 検索結果 表示
                LinearLayout linearLayout_result;
                linearLayout_result = (LinearLayout) mActivity.findViewById(R.id.LinearLayout_result);
                linearLayout_result.addView(result);
// ข้อมูลที่เอามาปริ้น header
                Log.d("28Dec", "onPost Header ==> " + myHeadResultString);
                TextView masterTextView = (TextView) mActivity.findViewById(R.id.txtMaster);
                masterTextView.setText(myHeadResultString);
//itemname
                TextView valueFinialTextView = (TextView) mActivity.findViewById(R.id.txtValue);
                valueFinialTextView.setText(finalResulttoPrintString);
                Log.d("28Dec", "onPost Data ==> " + finalResulttoPrintString);
//qty
//                TextView qtyTextView = (TextView) mActivity.findViewById(R.id.txtQty);
//                qtyTextView.setText(qtyfinalprintString);
//                Log.d("28Dec", "onPost Data ==> " + qtyfinalprintString);



            }

            // プログレスバー 非表示
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            mActivity = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mProgressDialog == null) {
                Resources resources = mActivity.getResources();

                // 検索時間 表示
                TextView textView_time;
                textView_time = (TextView) mActivity.findViewById(R.id.textView_time);
                textView_time.setText(String.valueOf(values[2]) + resources.getString(R.string.add_time));

                // 検索件数 表示
                TextView textView_cnt;
                textView_cnt = (TextView) mActivity.findViewById(R.id.textView_count);
                textView_cnt.setText(String.valueOf(values[1]) + resources.getString(R.string.add_count));

                // プログレスバー 表示
                String message;
                message = resources.getString(R.string.message1);
                message += " " + String.valueOf(values[2]) + resources.getString(R.string.add_time);
                message += " " + String.valueOf(values[1]) + resources.getString(R.string.add_count) + "\n";
                message += resources.getString(R.string.message2);
                mProgressDialog = new ProgressDialog(mActivity);
                mProgressDialog.setMessage(message);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setProgress(0);
                mProgressDialog.setMax(values[1]);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            } else {
                // プログレスバー 更新
                mProgressDialog.setProgress(values[0]);
            }
        }
    }


}