package com.bingo.sdk.fragment;

import androidx.fragment.app.Fragment;

import com.bingo.sdk.inner.interf.OnFragmentEventListener;

public class BaseFragment extends Fragment {


    public OnFragmentEventListener listener;

    public void setOnFragmentEventListener(OnFragmentEventListener listener) {
        this.listener = listener;
    }

}
