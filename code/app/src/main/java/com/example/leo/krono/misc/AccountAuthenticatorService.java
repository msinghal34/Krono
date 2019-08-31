package com.example.leo.krono.misc;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.example.leo.krono.activities.MainActivity;

/**
 * Created by guptaji on 2/10/17.
 */
//authenticates app account, generic class
public class AccountAuthenticatorService extends Service {
    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    public AccountAuthenticatorService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            ret = getAuthenticator().getIBinder();
        }
        return ret;
    }

    private AccountAuthenticatorImpl getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        return sAccountAuthenticator;
    }

    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
        private Context mContext;

        AccountAuthenticatorImpl(Context context) {
            super(context);
            mContext = context;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#addAccount(android.
         * accounts.AccountAuthenticatorResponse, java.lang.String, java.lang.String,
         * java.lang.String[], android.os.Bundle)
         */
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                                 String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            Bundle result = new Bundle();
            Intent i = new Intent(mContext, MainActivity.class);
            result.putParcelable(AccountManager.KEY_INTENT, i);
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#confirmCredentials(
         * android.accounts.AccountAuthenticatorResponse, android.accounts.Account,
         * android.os.Bundle)
         */
        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                         Bundle options) {
//            android.util.Log.e(Constants.TAG, "confirmCredentials not implemented!");
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#editProperties(android
         * .accounts.AccountAuthenticatorResponse, java.lang.String)
         */
        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
//            android.util.Log.e(Constants.TAG, "editProperties not implemented!");
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#getAuthToken(android
         * .accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String,
         * android.os.Bundle)
         */
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                                   String authTokenType, Bundle options) throws NetworkErrorException {
//            android.util.Log.e(Constants.TAG, "getAuthToken not implemented!");
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(java .lang.String)
         */
        @Override
        public String getAuthTokenLabel(String authTokenType) {
//            android.util.Log.e(Constants.TAG, "getAuthTokenLabel not implemented!");
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#hasFeatures(android
         * .accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String[])
         */
        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                                  String[] features) throws NetworkErrorException {
//            android.util.Log.e(Constants.TAG, "hasFeatures: " + features);
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.accounts.AbstractAccountAuthenticator#updateCredentials(android
         * .accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String,
         * android.os.Bundle)
         */
        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                        String authTokenType, Bundle options) {
//            android.util.Log.e(Constants.TAG, "updateCredentials not implemented!");
            return null;
        }
    }
}
