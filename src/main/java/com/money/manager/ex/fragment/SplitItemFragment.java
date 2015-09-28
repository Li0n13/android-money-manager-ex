/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.money.manager.ex.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransactionsDataset;

import java.math.BigDecimal;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class SplitItemFragment
        extends Fragment
        implements IInputAmountDialogListener {

    public static SplitItemFragment newInstance(ISplitTransactionsDataset split, Integer currencyId) {
        SplitItemFragment fragment = new SplitItemFragment();
        fragment.mSplitTransaction = split;
        fragment.currencyId = currencyId;
        return fragment;
    }

    public static final String KEY_SPLIT_TRANSACTION = "SplitItemFragment:SplitTransaction";

    private static final int REQUEST_PICK_CATEGORY = 1;

    private ISplitTransactionsDataset mSplitTransaction;
    private SplitItemFragmentCallbacks mOnSplitItemCallback;
    private TextView txtAmount;
    private Spinner spinTransCode;
    private Integer currencyId;

    /**
     * @return the splitItemCallback
     */
    public SplitItemFragmentCallbacks getOnSplitItemCallback() {
        return mOnSplitItemCallback;
    }

    /**
     * @param splitItemCallback the splitItemCallback to set
     */
    public void setOnSplitItemCallback(SplitItemFragmentCallbacks splitItemCallback) {
        this.mOnSplitItemCallback = splitItemCallback;
    }

    /**
     * Returns the Split Transaction created. Called from the activity that holds multiple
     * split-fragments.
     * @param parentTransactionType Parent transaction type. Required to determine the amount sign.
     *                              If the parent is Deposit then Deposit here is +,
     *                              Withdrawal is -.
     * @return Split Transaction
     */
    public ISplitTransactionsDataset getSplitTransaction(TransactionTypes parentTransactionType) {
        Object tag = txtAmount.getTag();

        if (tag == null) {
            // handle 0 values.
            mSplitTransaction.setSplitTransAmount(MoneyFactory.fromString("0"));
            return mSplitTransaction;
        }

        // otherwise figure out which sign to use for the amount.

        Money amount = MoneyFactory.fromString(tag.toString());

        // toString takes the localized text! Use value.
        int position = spinTransCode.getSelectedItemPosition();
        TransactionTypes transactionType = TransactionTypes.values()[position];

        if(!parentTransactionType.equals(transactionType)){
            // parent transaction type is different. Invert the amount. What if the amount is already negative?
//            mSplitTransaction.setSplitTransAmount(amount.doubleValue() * -1);
            mSplitTransaction.setSplitTransAmount(amount.negate());
        } else {
            mSplitTransaction.setSplitTransAmount(amount);
        }

        return mSplitTransaction;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CATEGORY:
                TextView txtSelectCategory = (TextView) getView().findViewById(R.id.textViewCategory);
                if (txtSelectCategory != null) {
                    txtSelectCategory.setText(null);
                    if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                        mSplitTransaction.setCategId(data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, -1));
                        mSplitTransaction.setSubCategId(data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, -1));
                        txtSelectCategory.setText(new Core(getActivity().getApplicationContext()).getCategSubName(mSplitTransaction.getCategId(), mSplitTransaction.getSubCategId()));
                    }
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION)) {
            mSplitTransaction = savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION);
        }

        Core core = new Core(getActivity().getApplicationContext());

        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_splittransaction, null);
        if (layout != null) {
            // amount
            txtAmount = (TextView) layout.findViewById(R.id.editTextTotAmount);
            Money splitTransactionAmount = mSplitTransaction.getSplitTransAmount();
            if (!(splitTransactionAmount.compareTo(MoneyFactory.fromString("0")) == 0)) {
                // Change the sign to positive.
                if(splitTransactionAmount.toDouble() < 0) {
                    splitTransactionAmount = splitTransactionAmount.negate();
                }

                FormatUtilities.formatAmountTextView(getActivity(), txtAmount, splitTransactionAmount,
                        this.currencyId);
            }
            txtAmount.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    Money amount;
                    if (tag == null) {
                        amount = MoneyFactory.fromString("0");
                    } else {
                        amount = MoneyFactory.fromString(tag.toString());
                    }

                    if (getActivity() instanceof SplitTransactionsActivity) {
                        SplitTransactionsActivity activity = (SplitTransactionsActivity) getActivity();
                        activity.setFragmentInputAmountClick(SplitItemFragment.this);
                    }

                    InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(),
                            amount, SplitItemFragment.this.currencyId);
                    dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
                }
            });

            // type
            spinTransCode = (Spinner) layout.findViewById(R.id.spinnerTransCode);
            String[] transCodeItems = getResources().getStringArray(R.array.split_transcode_items);
            ArrayAdapter<String> adapterTrans = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, transCodeItems);
            adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinTransCode.setAdapter(adapterTrans);
            // find the split transaction type.
//            SpinTransCode.setSelection(mSplitTransaction.getSplitTransAmount() >= 0 ? 0 : 1, true);
            int transactionTypeSelection = getTransactionTypeSelection();
            spinTransCode.setSelection(transactionTypeSelection);

            // category and subcategory
            TextView txtSelectCategory = (TextView) layout.findViewById(R.id.textViewCategory);
            String buttonText = core.getCategSubName(mSplitTransaction.getCategId(), mSplitTransaction.getSubCategId());
            txtSelectCategory.setText(buttonText);

            txtSelectCategory.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, REQUEST_PICK_CATEGORY);
                }
            });
            // image button to remove an item
            ImageButton btnRemove = (ImageButton) layout.findViewById(R.id.imageButtonCancel);
            btnRemove.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.remove(SplitItemFragment.this);
                    transaction.commit();
                    if (getOnSplitItemCallback() != null) {
                        getOnSplitItemCallback().onRemoveItem(mSplitTransaction);
                    }
                }
            });
            // tag class
            layout.setTag(mSplitTransaction);
        }

        return layout;
    }

    private int getTransactionTypeSelection(){
        // define the transaction type based on the amount and the parent type.

        int transactionTypeSelection;

        SplitTransactionsActivity splitActivity = (SplitTransactionsActivity) getActivity();
        boolean parentIsWithdrawal = splitActivity.mParentTransactionType.equals(TransactionTypes.Withdrawal);
        Money amount = mSplitTransaction.getSplitTransAmount();
        if(parentIsWithdrawal){
            // parent is Withdrawal.
            transactionTypeSelection = amount.toDouble() >= 0
                    ? TransactionTypes.Withdrawal.getCode() // 0
                    : TransactionTypes.Deposit.getCode(); // 1;
        } else {
            // parent is Deposit.
            transactionTypeSelection = amount.toDouble() >= 0
                    ? TransactionTypes.Deposit.getCode() // 1
                    : TransactionTypes.Withdrawal.getCode(); // 0;
        }

        return transactionTypeSelection;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SPLIT_TRANSACTION, mSplitTransaction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Money amount) {
//        Core core = new Core(getActivity().getApplicationContext());
        if (txtAmount.getId() == id) {
            txtAmount.setTag(amount.toString());
            mSplitTransaction.setSplitTransAmount(amount);
            FormatUtilities.formatAmountTextView(getActivity(), txtAmount, amount, currencyId);
        }
    }

    public interface SplitItemFragmentCallbacks {
        void onRemoveItem(ISplitTransactionsDataset object);
    }
}