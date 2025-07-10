package edu.cs4730.wearappvoice.utils;


import javax.naming.Context;
import javax.swing.text.View;
import java.awt.*;

public class LoadingDialog extends Dialog {


    public LoadingDialog(Frame owner, String title) {
        super(owner, title);
    }

    public static class Builder {
        private View mLayout;
        private LoadingDialog mDialog;

        public Builder(Context context) {


         }

          public LoadingDialog create() {

              return mDialog;
             }
     }
}
