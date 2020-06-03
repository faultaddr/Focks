package cn.panyunyi.focks.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.panyunyi.focks.R;

public class FinishDialogFragment extends DialogFragment {


    public FinishDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DialogFragment.
     */
    public static FinishDialogFragment newInstance() {
        return new FinishDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog,null);
    }
}
